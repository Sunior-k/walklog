package com.river.walklog.core.testing.repository

import com.river.walklog.core.data.repository.RewardRedemptionRepository
import com.river.walklog.core.model.RewardRedemption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class FakeRewardRedemptionRepository : RewardRedemptionRepository {

    private val _redemptions = MutableStateFlow<List<RewardRedemption>>(emptyList())
    val redemptions: StateFlow<List<RewardRedemption>> = _redemptions

    override fun observeAll(): StateFlow<List<RewardRedemption>> = _redemptions

    override fun observeByRewardId(rewardId: String) =
        _redemptions.map { list -> list.filter { it.rewardId == rewardId } }

    override suspend fun recordRedemption(
        rewardId: String,
        pointsCost: Int,
        couponCode: String?,
    ): RewardRedemption {
        val redemption = RewardRedemption(
            id = (_redemptions.value.size + 1).toLong(),
            rewardId = rewardId,
            pointsCost = pointsCost,
            redeemedAtEpochMillis = System.currentTimeMillis(),
            couponCode = couponCode,
        )
        _redemptions.value = _redemptions.value + redemption
        return redemption
    }
}
