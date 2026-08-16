package com.river.walklog.core.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.river.walklog.core.common.dispatcher.WalkLogDispatchers
import com.river.walklog.core.model.PromoCodeRedeemResult
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * `promoCodes/{code}`는 관리자 전용 쓰기(redemptionCount만 트랜잭션으로 +1 허용), `users/{uid}/data/promoCodeRedemptions/entries/{code}`는
 * 문서ID=코드라서 트랜잭션 내 존재 확인만으로 동시 중복 등록도 안전하게 막는다.
 */
@Singleton
class FirestorePromoCodeDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val dispatchers: WalkLogDispatchers,
) {
    suspend fun redeem(userId: String, code: String): PromoCodeRedeemResult = withContext(dispatchers.io) {
        val normalizedCode = code.trim().uppercase()
        if (normalizedCode.isEmpty()) return@withContext PromoCodeRedeemResult.InvalidCode

        val promoCodeRef = firestore.collection(COLLECTION_PROMO_CODES).document(normalizedCode)
        val userRedemptionRef = firestore.collection("users").document(userId)
            .collection("data").document("promoCodeRedemptions")
            .collection("entries").document(normalizedCode)

        runCatching {
            firestore.runTransaction { transaction ->
                if (transaction.get(userRedemptionRef).exists()) {
                    return@runTransaction TxOutcome.AlreadyRedeemed
                }

                val promoSnap = transaction.get(promoCodeRef)
                if (!promoSnap.exists()) {
                    return@runTransaction TxOutcome.Invalid
                }

                val isActive = promoSnap.getBoolean(FIELD_IS_ACTIVE) ?: false
                val pointsReward = (promoSnap.getLong(FIELD_POINTS_REWARD) ?: 0L).toInt()
                val maxRedemptions = (promoSnap.getLong(FIELD_MAX_REDEMPTIONS) ?: 0L).toInt()
                val redemptionCount = (promoSnap.getLong(FIELD_REDEMPTION_COUNT) ?: 0L).toInt()
                val expiresAt = promoSnap.getLong(FIELD_EXPIRES_AT) ?: 0L

                val isExpired = expiresAt > 0 && System.currentTimeMillis() > expiresAt
                val isFull = maxRedemptions > 0 && redemptionCount >= maxRedemptions

                if (!isActive || isExpired || isFull) {
                    return@runTransaction TxOutcome.Invalid
                }

                transaction.update(promoCodeRef, FIELD_REDEMPTION_COUNT, redemptionCount + 1)
                transaction.set(
                    userRedemptionRef,
                    mapOf(
                        "code" to normalizedCode,
                        "pointsAwarded" to pointsReward,
                        "redeemedAtEpochMillis" to System.currentTimeMillis(),
                    ),
                )

                TxOutcome.Success(pointsReward)
            }.await()
        }.fold(
            onSuccess = { outcome ->
                when (outcome) {
                    is TxOutcome.Success -> PromoCodeRedeemResult.Success(outcome.pointsAwarded)
                    TxOutcome.AlreadyRedeemed -> PromoCodeRedeemResult.AlreadyRedeemed
                    TxOutcome.Invalid -> PromoCodeRedeemResult.InvalidCode
                }
            },
            onFailure = { PromoCodeRedeemResult.UnknownError },
        )
    }

    private sealed interface TxOutcome {
        data class Success(val pointsAwarded: Int) : TxOutcome
        data object AlreadyRedeemed : TxOutcome
        data object Invalid : TxOutcome
    }

    private companion object {
        const val COLLECTION_PROMO_CODES = "promoCodes"
        const val FIELD_IS_ACTIVE = "isActive"
        const val FIELD_POINTS_REWARD = "pointsReward"
        const val FIELD_MAX_REDEMPTIONS = "maxRedemptions"
        const val FIELD_REDEMPTION_COUNT = "redemptionCount"
        const val FIELD_EXPIRES_AT = "expiresAtEpochMillis"
    }
}
