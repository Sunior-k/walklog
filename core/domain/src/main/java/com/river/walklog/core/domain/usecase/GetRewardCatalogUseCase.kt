package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.RewardCatalogRepository
import com.river.walklog.core.model.RewardCatalogItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetRewardCatalogUseCase @Inject constructor(
    private val rewardCatalogRepository: RewardCatalogRepository,
) {
    operator fun invoke(): Flow<List<RewardCatalogItem>> =
        rewardCatalogRepository.observeCatalog().map { items -> items.filter { it.isActive } }
}
