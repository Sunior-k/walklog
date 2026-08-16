package com.river.walklog.core.domain.usecase

import app.cash.turbine.test
import com.river.walklog.core.model.RewardCatalogItem
import com.river.walklog.core.testing.repository.FakeRewardCatalogRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class GetRewardCatalogUseCaseTest {

    private val repository = FakeRewardCatalogRepository()
    private val useCase = GetRewardCatalogUseCase(repository)

    @Test
    fun `filters out inactive items`() = runTest {
        repository.setCatalog(
            listOf(
                RewardCatalogItem("a", "🅰️", "A", "desc", 100, isActive = true),
                RewardCatalogItem("b", "🅱️", "B", "desc", 200, isActive = false),
            ),
        )

        useCase().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("a", items.single().id)
        }
    }

    @Test
    fun `returns empty list when catalog is empty`() = runTest {
        useCase().test {
            assertEquals(emptyList(), awaitItem())
        }
    }
}
