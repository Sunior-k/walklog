package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.RewardRedemptionRepository
import com.river.walklog.core.model.RewardRedemption
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRewardRedemptionsUseCase @Inject constructor(
    private val rewardRedemptionRepository: RewardRedemptionRepository,
) {
    operator fun invoke(rewardId: String): Flow<List<RewardRedemption>> =
        rewardRedemptionRepository.observeByRewardId(rewardId)
}
