package com.river.walklog.core.data.repository

import com.river.walklog.core.data.datasource.FirestoreRewardRedemptionDataSource
import com.river.walklog.core.data.sync.Syncable
import com.river.walklog.core.database.dao.RewardRedemptionDao
import com.river.walklog.core.database.entity.RewardRedemptionEntity
import com.river.walklog.core.database.entity.asExternalModel
import com.river.walklog.core.model.RewardRedemption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 뱃지 보유·테마 팩 보유·기부 총액의 근거가 되는 교환 기록. Room을 진실의 원천으로 쓰되,
 * 로그인 상태([UserSettingsRepository]의 userId)면 [FirestoreRewardRedemptionDataSource]에도
 * 백업해서 재설치·기기 변경 후 재로그인해도 소유 상태가 복원되도록 한다.
 */
@Singleton
class RoomRewardRedemptionRepository @Inject constructor(
    private val dao: RewardRedemptionDao,
    private val firestoreDataSource: FirestoreRewardRedemptionDataSource,
    private val userSettingsRepository: UserSettingsRepository,
) : RewardRedemptionRepository, Syncable {

    override fun observeAll(): Flow<List<RewardRedemption>> =
        dao.observeAll().map { entities -> entities.map { it.asExternalModel() } }

    override fun observeByRewardId(rewardId: String): Flow<List<RewardRedemption>> =
        dao.observeByRewardId(rewardId).map { entities -> entities.map { it.asExternalModel() } }

    override suspend fun recordRedemption(
        rewardId: String,
        pointsCost: Int,
        couponCode: String?,
    ): RewardRedemption {
        val entity = RewardRedemptionEntity(
            rewardId = rewardId,
            pointsCost = pointsCost,
            redeemedAtEpochMillis = System.currentTimeMillis(),
            couponCode = couponCode,
        )
        val id = dao.insert(entity)

        val userId = userSettingsRepository.settings.first().userId
        if (userId.isNotBlank()) {
            // 즉시 업로드는 최선의 노력(best-effort) — 실패해도 [sync]가 remoteId == null 항목을
            // 다음 WorkManager 주기에 재시도하므로 여기서는 실패를 삼켜도 데이터 손실이 없다.
            firestoreDataSource.upload(
                userId = userId,
                rewardId = entity.rewardId,
                pointsCost = entity.pointsCost,
                redeemedAtEpochMillis = entity.redeemedAtEpochMillis,
                couponCode = entity.couponCode,
            ).onSuccess { remoteId -> dao.setRemoteId(id, remoteId) }
        }

        return entity.copy(id = id).asExternalModel()
    }

    /**
     * 1. 아직 업로드 안 된 로컬 기록(remoteId == null)을 Firestore에 올림 — 오프라인 교환 등으로
     *    업로드가 누락된 경우 복구.
     * 2. Firestore에는 있지만 로컬에는 없는 기록(재설치·기기 변경)을 Room에 복원.
     */
    override suspend fun sync(): Boolean = runCatching {
        val userId = userSettingsRepository.settings.first().userId
        if (userId.isBlank()) return true

        dao.getUnsyncedOnce().forEach { entity ->
            val remoteId = firestoreDataSource.upload(
                userId = userId,
                rewardId = entity.rewardId,
                pointsCost = entity.pointsCost,
                redeemedAtEpochMillis = entity.redeemedAtEpochMillis,
                couponCode = entity.couponCode,
            ).getOrThrow()
            dao.setRemoteId(entity.id, remoteId)
        }

        val localRemoteIds = dao.getAllOnce().mapNotNull { it.remoteId }.toSet()
        firestoreDataSource.fetchAll(userId)
            .filter { it.remoteId !in localRemoteIds }
            .forEach { remote ->
                dao.insert(
                    RewardRedemptionEntity(
                        rewardId = remote.rewardId,
                        pointsCost = remote.pointsCost,
                        redeemedAtEpochMillis = remote.redeemedAtEpochMillis,
                        couponCode = remote.couponCode,
                        remoteId = remote.remoteId,
                    ),
                )
            }

        true
    }.getOrDefault(false)
}
