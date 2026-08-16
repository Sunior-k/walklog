package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.RewardRedemptionRepository
import com.river.walklog.core.model.RewardCatalogIds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetOwnedRewardIdsUseCase @Inject constructor(
    private val rewardRedemptionRepository: RewardRedemptionRepository,
) {
    operator fun invoke(): Flow<Set<String>> =
        rewardRedemptionRepository.observeAll()
            .map { redemptions -> redemptions.map { it.rewardId }.toSet() - RewardCatalogIds.REPEATABLE_IDS }
}
