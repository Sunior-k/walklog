package com.river.walklog.core.model

data class Coupon(
    val code: String,
    val rewardId: String,
    val pointsCost: Int,
    val status: CouponStatus,
    val createdAtEpochMillis: Long,
    val usedAtEpochMillis: Long?,
)
