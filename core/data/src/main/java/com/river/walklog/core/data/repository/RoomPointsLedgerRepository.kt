package com.river.walklog.core.data.repository

import com.river.walklog.core.data.datasource.FirestorePointsLedgerDataSource
import com.river.walklog.core.data.sync.Syncable
import com.river.walklog.core.database.dao.PointsLedgerDao
import com.river.walklog.core.database.entity.PointsLedgerEntity
import com.river.walklog.core.database.entity.asExternalModel
import com.river.walklog.core.model.PointsLedgerEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * "포인트 적립" 화면에 표시되는 개별 적립/차감 내역. Room을 진실의 원천으로 쓰되, 로그인
 * 상태([UserSettingsRepository]의 userId)면 [FirestorePointsLedgerDataSource]에도 백업해서
 * 재설치·기기 변경 후 재로그인해도 내역이 복원되도록 한다.
 */
@Singleton
class RoomPointsLedgerRepository @Inject constructor(
    private val dao: PointsLedgerDao,
    private val firestoreDataSource: FirestorePointsLedgerDataSource,
    private val userSettingsRepository: UserSettingsRepository,
) : PointsLedgerRepository, Syncable {

    override fun observeEntries(): Flow<List<PointsLedgerEntry>> =
        dao.observeAll().map { entities -> entities.map { it.asExternalModel() } }

    override suspend fun record(deltaPoints: Int, reason: String) {
        val entity = PointsLedgerEntity(
            deltaPoints = deltaPoints,
            reason = reason,
            createdAtEpochMillis = System.currentTimeMillis(),
        )
        val id = dao.insert(entity)

        val userId = userSettingsRepository.settings.first().userId
        if (userId.isNotBlank()) {
            // 즉시 업로드는 최선의 노력(best-effort) — 실패해도 [sync]가 remoteId == null 항목을
            // 다음 WorkManager 주기에 재시도하므로 여기서는 실패를 삼켜도 데이터 손실이 없다.
            firestoreDataSource.upload(
                userId = userId,
                deltaPoints = entity.deltaPoints,
                reason = entity.reason,
                createdAtEpochMillis = entity.createdAtEpochMillis,
            ).onSuccess { remoteId -> dao.setRemoteId(id, remoteId) }
        }
    }

    /**
     * 1. 아직 업로드 안 된 로컬 기록(remoteId == null)을 Firestore에 올림.
     * 2. Firestore에는 있지만 로컬에는 없는 기록(재설치·기기 변경)을 Room에 복원.
     */
    override suspend fun sync(): Boolean = runCatching {
        val userId = userSettingsRepository.settings.first().userId
        if (userId.isBlank()) return true

        dao.getUnsyncedOnce().forEach { entity ->
            val remoteId = firestoreDataSource.upload(
                userId = userId,
                deltaPoints = entity.deltaPoints,
                reason = entity.reason,
                createdAtEpochMillis = entity.createdAtEpochMillis,
            ).getOrThrow()
            dao.setRemoteId(entity.id, remoteId)
        }

        val localRemoteIds = dao.getAllOnce().mapNotNull { it.remoteId }.toSet()
        firestoreDataSource.fetchAll(userId)
            .filter { it.remoteId !in localRemoteIds }
            .forEach { remote ->
                dao.insert(
                    PointsLedgerEntity(
                        deltaPoints = remote.deltaPoints,
                        reason = remote.reason,
                        createdAtEpochMillis = remote.createdAtEpochMillis,
                        remoteId = remote.remoteId,
                    ),
                )
            }

        true
    }.getOrDefault(false)
}
