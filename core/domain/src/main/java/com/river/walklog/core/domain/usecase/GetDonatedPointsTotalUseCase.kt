package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.RewardRedemptionRepository
import com.river.walklog.core.model.RewardCatalogIds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetDonatedPointsTotalUseCase @Inject constructor(
    private val rewardRedemptionRepository: RewardRedemptionRepository,
) {
    operator fun invoke(): Flow<Int> =
        rewardRedemptionRepository.observeByRewardId(RewardCatalogIds.DONATION)
            .map { redemptions -> redemptions.sumOf { it.pointsCost } }
}
