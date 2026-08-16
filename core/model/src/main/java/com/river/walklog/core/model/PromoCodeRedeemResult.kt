package com.river.walklog.core.model

/**
 * 이벤트/프로모션 코드 등록(리워드 스토어 교환과는 별개 — 외부에서 받은 코드를 직접 입력해 등록).
 */
sealed interface PromoCodeRedeemResult {
    data class Success(val pointsAwarded: Int) : PromoCodeRedeemResult
    data object AlreadyRedeemed : PromoCodeRedeemResult
    data object InvalidCode : PromoCodeRedeemResult
    data object SignInRequired : PromoCodeRedeemResult
    data object UnknownError : PromoCodeRedeemResult
}
