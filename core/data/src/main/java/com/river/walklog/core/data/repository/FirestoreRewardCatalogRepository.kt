package com.river.walklog.core.data.repository

import com.river.walklog.core.data.datasource.FirestoreRewardCatalogDataSource
import com.river.walklog.core.model.RewardCatalogItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRewardCatalogRepository @Inject constructor(
    private val dataSource: FirestoreRewardCatalogDataSource,
) : RewardCatalogRepository {

    override fun observeCatalog(): Flow<List<RewardCatalogItem>> = dataSource.observeCatalog()
}
