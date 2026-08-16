package com.river.walklog.core.data.repository

import com.river.walklog.core.model.RewardCatalogItem
import kotlinx.coroutines.flow.Flow

interface RewardCatalogRepository {
    fun observeCatalog(): Flow<List<RewardCatalogItem>>
}
