package com.river.walklog.core.data.datasource

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.river.walklog.core.common.dispatcher.WalkLogDispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * `users/{uid}/data/rewardRedemptions/{autoId}` — 뱃지 보유·테마 팩 보유·기부 총액의 근거가 되는
 * 교환 기록을 클라우드에 백업. 재설치·기기 변경 후 재로그인해도 소유 상태가 복원되도록
 * [com.river.walklog.core.data.repository.RoomRewardRedemptionRepository]가 로컬 기록과 함께 사용.
 */
@Singleton
class FirestoreRewardRedemptionDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val dispatchers: WalkLogDispatchers,
) {
    private fun collectionRef(userId: String) =
        firestore.collection("users").document(userId)
            .collection("data").document("rewardRedemptions")
            .collection("entries")

    /**
     * 실패를 [Result]로 반환 — 호출부([RoomRewardRedemptionRepository.sync])가 실패를 구분해
     * WorkManager 재시도를 정상적으로 트리거할 수 있도록 삼키지 않는다.
     */
    suspend fun upload(
        userId: String,
        rewardId: String,
        pointsCost: Int,
        redeemedAtEpochMillis: Long,
        couponCode: String?,
    ): Result<String> = withContext(dispatchers.io) {
        runCatching {
            val data = hashMapOf(
                FIELD_REWARD_ID to rewardId,
                FIELD_POINTS_COST to pointsCost,
                FIELD_REDEEMED_AT to redeemedAtEpochMillis,
                FIELD_COUPON_CODE to couponCode,
                FIELD_CREATED_AT to FieldValue.serverTimestamp(),
            )
            val docRef = collectionRef(userId).add(data).await()
            docRef.id
        }
    }

    suspend fun fetchAll(userId: String): List<RemoteRewardRedemption> = withContext(dispatchers.io) {
        collectionRef(userId).get().await().documents.mapNotNull { doc ->
            val rewardId = doc.getString(FIELD_REWARD_ID) ?: return@mapNotNull null
            RemoteRewardRedemption(
                remoteId = doc.id,
                rewardId = rewardId,
                pointsCost = (doc.getLong(FIELD_POINTS_COST) ?: 0L).toInt(),
                redeemedAtEpochMillis = doc.getLong(FIELD_REDEEMED_AT) ?: 0L,
                couponCode = doc.getString(FIELD_COUPON_CODE),
            )
        }
    }

    private companion object {
        const val FIELD_REWARD_ID = "rewardId"
        const val FIELD_POINTS_COST = "pointsCost"
        const val FIELD_REDEEMED_AT = "redeemedAtEpochMillis"
        const val FIELD_COUPON_CODE = "couponCode"
        const val FIELD_CREATED_AT = "createdAt"
    }
}

data class RemoteRewardRedemption(
    val remoteId: String,
    val rewardId: String,
    val pointsCost: Int,
    val redeemedAtEpochMillis: Long,
    val couponCode: String?,
)
