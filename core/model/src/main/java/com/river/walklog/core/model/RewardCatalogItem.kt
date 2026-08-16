package com.river.walklog.core.model

/**
 * 리워드 스토어 상품. Firestore `rewardCatalog` 컬렉션에서 읽어와 가격·판매 여부를
 * 앱 업데이트 없이 관리자가 Firebase 콘솔에서 직접 바꿀 수 있게 한다.
 */
data class RewardCatalogItem(
    val id: String,
    val emoji: String,
    val title: String,
    val description: String,
    val cost: Int,
    val isActive: Boolean,
)
