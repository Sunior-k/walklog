package com.river.walklog.core.domain.usecase

import com.river.walklog.core.auth.AuthRepository
import com.river.walklog.core.data.repository.PointsLedgerRepository
import com.river.walklog.core.data.repository.PromoCodeRepository
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.model.PromoCodeRedeemResult
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * 이벤트/프로모션 코드 등록. 리워드 스토어 교환([RedeemRewardUseCase])과는 별개로, 외부에서
 * 받은 코드를 입력해 포인트를 받는다. 비로그인 상태면 Firestore 호출 전에 [PromoCodeRedeemResult.SignInRequired]로 걸러낸다.
 */
class RedeemPromoCodeUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val promoCodeRepository: PromoCodeRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val pointsLedgerRepository: PointsLedgerRepository,
) {
    suspend operator fun invoke(code: String): PromoCodeRedeemResult {
        val userId = authRepository.currentUserIdOrNull
        if (userId.isNullOrBlank()) return PromoCodeRedeemResult.SignInRequired

        val result = promoCodeRepository.redeem(userId, code)
        if (result is PromoCodeRedeemResult.Success && result.pointsAwarded > 0) {
            applyPointsWithRetry(result.pointsAwarded, code)
        }
        return result
    }

    /**
     * [promoCodeRepository.redeem]이 성공하는 순간 서버에는 "이미 등록됨" 마커가 확정되어,
     * 이 로컬 반영이 실패하면 같은 코드로 재시도할 방법이 없다(포인트 영구 손실). 로컬(Room/
     * DataStore) 쓰기는 네트워크 호출보다 실패 확률이 훨씬 낮으므로 짧은 재시도로 대부분의
     * 일시적 실패를 흡수하고, 그래도 실패하면 예외를 그대로 전파해 상위 크래시 리포팅으로
     * 드러나게 한다(삼키지 않음 — 수동 포인트 보정이 필요하다는 신호).
     */
    private suspend fun applyPointsWithRetry(points: Int, code: String) {
        var lastError: Throwable? = null
        repeat(LOCAL_APPLY_MAX_ATTEMPTS) { attempt ->
            val outcome = runCatching {
                userSettingsRepository.addPoints(points)
                pointsLedgerRepository.record(points, "PROMO_CODE_${code.trim().uppercase()}")
            }
            if (outcome.isSuccess) return
            lastError = outcome.exceptionOrNull()
            if (attempt < LOCAL_APPLY_MAX_ATTEMPTS - 1) delay(LOCAL_APPLY_RETRY_DELAY_MS)
        }
        throw lastError ?: IllegalStateException("promo code local point apply failed: $code")
    }

    private companion object {
        const val LOCAL_APPLY_MAX_ATTEMPTS = 3
        const val LOCAL_APPLY_RETRY_DELAY_MS = 200L
    }
}
