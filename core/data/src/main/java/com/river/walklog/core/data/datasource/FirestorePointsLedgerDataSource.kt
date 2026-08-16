package com.river.walklog.core.data.datasource

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.river.walklog.core.common.dispatcher.WalkLogDispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * `users/{uid}/data/pointsLedger/{autoId}` — "포인트 적립" 화면에 표시되는 개별 적립/차감
 * 내역을 클라우드에 백업. 재설치·기기 변경 후 재로그인해도 내역이 복원되도록
 * [com.river.walklog.core.data.repository.RoomPointsLedgerRepository]가 로컬 기록과 함께 사용.
 */
@Singleton
class FirestorePointsLedgerDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val dispatchers: WalkLogDispatchers,
) {
    private fun collectionRef(userId: String) =
        firestore.collection("users").document(userId)
            .collection("data").document("pointsLedger")
            .collection("entries")

    /**
     * 실패를 [Result]로 반환 — 호출부([RoomPointsLedgerRepository.sync])가 실패를 구분해
     * WorkManager 재시도를 정상적으로 트리거할 수 있도록 삼키지 않는다.
     */
    suspend fun upload(
        userId: String,
        deltaPoints: Int,
        reason: String,
        createdAtEpochMillis: Long,
    ): Result<String> = withContext(dispatchers.io) {
        runCatching {
            val data = hashMapOf(
                FIELD_DELTA_POINTS to deltaPoints,
                FIELD_REASON to reason,
                FIELD_CREATED_AT_EPOCH to createdAtEpochMillis,
                FIELD_CREATED_AT to FieldValue.serverTimestamp(),
            )
            val docRef = collectionRef(userId).add(data).await()
            docRef.id
        }
    }

    suspend fun fetchAll(userId: String): List<RemotePointsLedgerEntry> = withContext(dispatchers.io) {
        collectionRef(userId).get().await().documents.mapNotNull { doc ->
            val reason = doc.getString(FIELD_REASON) ?: return@mapNotNull null
            RemotePointsLedgerEntry(
                remoteId = doc.id,
                deltaPoints = (doc.getLong(FIELD_DELTA_POINTS) ?: 0L).toInt(),
                reason = reason,
                createdAtEpochMillis = doc.getLong(FIELD_CREATED_AT_EPOCH) ?: 0L,
            )
        }
    }

    private companion object {
        const val FIELD_DELTA_POINTS = "deltaPoints"
        const val FIELD_REASON = "reason"
        const val FIELD_CREATED_AT_EPOCH = "createdAtEpochMillis"
        const val FIELD_CREATED_AT = "createdAt"
    }
}

data class RemotePointsLedgerEntry(
    val remoteId: String,
    val deltaPoints: Int,
    val reason: String,
    val createdAtEpochMillis: Long,
)
