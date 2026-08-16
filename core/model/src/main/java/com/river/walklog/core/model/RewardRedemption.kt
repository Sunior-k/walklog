package com.river.walklog.core.model

data class RewardRedemption(
    val id: Long,
    val rewardId: String,
    val pointsCost: Int,
    val redeemedAtEpochMillis: Long,
    val couponCode: String?,
)
