package com.river.walklog.core.data.repository

import com.river.walklog.core.model.RewardRedemption
import kotlinx.coroutines.flow.Flow

interface RewardRedemptionRepository {
    fun observeAll(): Flow<List<RewardRedemption>>
    fun observeByRewardId(rewardId: String): Flow<List<RewardRedemption>>
    suspend fun recordRedemption(rewardId: String, pointsCost: Int, couponCode: String?): RewardRedemption
}
