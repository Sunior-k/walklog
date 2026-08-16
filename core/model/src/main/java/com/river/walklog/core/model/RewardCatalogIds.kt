package com.river.walklog.core.model

/**
 * 리워드 스토어 상품 ID. reward-store-rn/src/data/rewardCatalog.ts의 id와 반드시 일치해야 함
 * (공유 스키마 수단이 없어 양쪽을 수동으로 동기화).
 */
object RewardCatalogIds {
    const val COFFEE_COUPON = "coffee-coupon"
    const val BADGE_GOLD = "walk-badge-gold"
    const val DONATION = "donation-500"
    const val THEME_PACK = "theme-pack"

    /** 몇 번을 교환해도 각각 새 효과가 있는 상품(쿠폰은 매번 새 코드, 기부는 누적) — 그 외는 1회성. */
    val REPEATABLE_IDS = setOf(COFFEE_COUPON, DONATION)
}
