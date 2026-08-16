package com.river.walklog.core.domain.usecase

import com.river.walklog.core.auth.AuthRepository
import com.river.walklog.core.data.repository.PointsLedgerRepository
import com.river.walklog.core.data.repository.RewardRedemptionRepository
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.model.RewardCatalogIds
import kotlinx.coroutines.flow.first
import javax.inject.Inject

sealed interface RedeemResult {
    data object Success : RedeemResult
    data object InsufficientBalance : RedeemResult
    data object SignInRequired : RedeemResult
    data object AlreadyOwned : RedeemResult

    /**
     * 포인트는 차감됐지만 상품 지급(쿠폰 발급 등)이 실패해 차감분을 환불한 상태.
     * 사용자에게는 실패로 안내하고 재시도를 유도한다.
     */
    data object RedemptionFailed : RedeemResult
}

/**
 * 비로그인(게스트) 상태면 잔액과 무관하게 [RedeemResult.SignInRequired], 잔액 부족 시 차감 없이
 * [RedeemResult.InsufficientBalance].
 */
class RedeemRewardUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val pointsLedgerRepository: PointsLedgerRepository,
    private val rewardRedemptionRepository: RewardRedemptionRepository,
    private val issueCouponUseCase: IssueCouponUseCase,
) {
    suspend operator fun invoke(rewardId: String, cost: Int): RedeemResult {
        val userId = authRepository.currentUserIdOrNull
        if (userId.isNullOrBlank()) return RedeemResult.SignInRequired

        if (rewardId !in RewardCatalogIds.REPEATABLE_IDS) {
            val alreadyOwned = rewardRedemptionRepository.observeByRewardId(rewardId).first().isNotEmpty()
            if (alreadyOwned) return RedeemResult.AlreadyOwned
        }

        if (!userSettingsRepository.trySpendPoints(cost)) return RedeemResult.InsufficientBalance
        pointsLedgerRepository.record(-cost, "REDEEM_$rewardId")

        val grantResult = runCatching {
            if (rewardId == RewardCatalogIds.COFFEE_COUPON) {
                issueCouponUseCase(userId, rewardId, cost)
            }
            rewardRedemptionRepository.recordRedemption(rewardId, cost, couponCode = null)

            if (rewardId == RewardCatalogIds.THEME_PACK) {
                userSettingsRepository.setActiveThemePack(true)
            }
        }

        if (grantResult.isFailure) {
            // 상품 지급 실패 — 이미 차감한 포인트를 환불해 소실을 막는다.
            userSettingsRepository.addPoints(cost)
            pointsLedgerRepository.record(cost, "REDEEM_REFUND_$rewardId")
            return RedeemResult.RedemptionFailed
        }

        return RedeemResult.Success
    }
}
