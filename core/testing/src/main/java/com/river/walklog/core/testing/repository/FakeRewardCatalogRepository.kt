package com.river.walklog.core.testing.repository

import com.river.walklog.core.data.repository.RewardCatalogRepository
import com.river.walklog.core.model.RewardCatalogItem
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRewardCatalogRepository : RewardCatalogRepository {

    private val _catalog = MutableStateFlow<List<RewardCatalogItem>>(emptyList())

    override fun observeCatalog() = _catalog

    fun setCatalog(items: List<RewardCatalogItem>) {
        _catalog.value = items
    }
}
