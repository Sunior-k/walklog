package com.river.walklog.core.data.datasource

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.river.walklog.core.common.dispatcher.WalkLogDispatchers
import com.river.walklog.core.model.Coupon
import com.river.walklog.core.model.CouponStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * `couponRedemptions/{code}` 컬렉션 — 문서 ID가 곧 쿠폰 코드라서 Firestore가 같은 코드로의
 * 중복 create를 자연스럽게 막아준다(보안 규칙과 함께 동작). Cloud Functions가 없어 서버가
 * 코드를 서명해 발급하는 완전한 위조 방지는 아니지만, "본인 소유로만 생성/전이" 정도의
 * 방어선은 이 구조 + firestore.rules로 확보한다.
 */
@Singleton
class FirestoreCouponDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val dispatchers: WalkLogDispatchers,
) {
    private val couponsRef get() = firestore.collection(COLLECTION)

    fun observeCoupons(userId: String): Flow<List<Coupon>> = callbackFlow {
        val query = couponsRef
            .whereEqualTo(FIELD_USER_ID, userId)
            .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val coupons = snapshot?.documents?.mapNotNull { it.toCouponOrNull() } ?: emptyList()
            trySend(coupons)
        }
        awaitClose { registration.remove() }
    }

    suspend fun issueCoupon(userId: String, rewardId: String, pointsCost: Int): Coupon =
        withContext(dispatchers.io) {
            repeat(MAX_CODE_ATTEMPTS) {
                val code = generateCode()
                val docRef = couponsRef.document(code)
                val data = hashMapOf(
                    FIELD_USER_ID to userId,
                    FIELD_REWARD_ID to rewardId,
                    FIELD_POINTS_COST to pointsCost,
                    FIELD_STATUS to CouponStatus.ISSUED.name,
                    FIELD_CREATED_AT to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    FIELD_USED_AT to null,
                )
                val created = runCatching {
                    firestore.runTransaction { transaction ->
                        if (transaction.get(docRef).exists()) {
                            error("Coupon code collision: $code")
                        }
                        transaction.set(docRef, data)
                    }.await()
                }.isSuccess

                if (created) {
                    return@withContext Coupon(
                        code = code,
                        rewardId = rewardId,
                        pointsCost = pointsCost,
                        status = CouponStatus.ISSUED,
                        createdAtEpochMillis = System.currentTimeMillis(),
                        usedAtEpochMillis = null,
                    )
                }
            }
            error("Failed to issue coupon after $MAX_CODE_ATTEMPTS attempts")
        }

    suspend fun markUsed(code: String, userId: String): Boolean = withContext(dispatchers.io) {
        runCatching {
            val docRef = couponsRef.document(code)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val ownerId = snapshot.getString(FIELD_USER_ID)
                val status = snapshot.getString(FIELD_STATUS)
                if (ownerId != userId || status != CouponStatus.ISSUED.name) {
                    error("Coupon not owned by user or already used")
                }
                transaction.update(
                    docRef,
                    mapOf(
                        FIELD_STATUS to CouponStatus.USED.name,
                        FIELD_USED_AT to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    ),
                )
            }.await()
        }.isSuccess
    }

    private fun DocumentSnapshot.toCouponOrNull(): Coupon? {
        val rewardId = getString(FIELD_REWARD_ID) ?: return null
        val statusName = getString(FIELD_STATUS) ?: return null
        val status = runCatching { CouponStatus.valueOf(statusName) }.getOrNull() ?: return null
        return Coupon(
            code = id,
            rewardId = rewardId,
            pointsCost = (getLong(FIELD_POINTS_COST) ?: 0L).toInt(),
            status = status,
            createdAtEpochMillis = getTimestamp(FIELD_CREATED_AT)?.toDate()?.time ?: 0L,
            usedAtEpochMillis = getTimestamp(FIELD_USED_AT)?.toDate()?.time,
        )
    }

    private fun generateCode(): String {
        val suffix = (1..CODE_SUFFIX_LENGTH)
            .map { CODE_CHARS[Random.nextInt(CODE_CHARS.length)] }
            .joinToString("")
        return "WALK-$suffix"
    }

    private companion object {
        const val COLLECTION = "couponRedemptions"
        const val FIELD_USER_ID = "userId"
        const val FIELD_REWARD_ID = "rewardId"
        const val FIELD_POINTS_COST = "pointsCost"
        const val FIELD_STATUS = "status"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_USED_AT = "usedAt"
        const val MAX_CODE_ATTEMPTS = 5
        const val CODE_SUFFIX_LENGTH = 6
        const val CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }
}
