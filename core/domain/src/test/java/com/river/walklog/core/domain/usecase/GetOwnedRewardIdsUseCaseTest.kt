package com.river.walklog.core.domain.usecase

import app.cash.turbine.test
import com.river.walklog.core.model.RewardCatalogIds
import com.river.walklog.core.testing.repository.FakeRewardRedemptionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class GetOwnedRewardIdsUseCaseTest {

    private val repository = FakeRewardRedemptionRepository()
    private val useCase = GetOwnedRewardIdsUseCase(repository)

    @Test
    fun `returns empty set when nothing redeemed`() = runTest {
        useCase().test {
            assertEquals(emptySet(), awaitItem())
        }
    }

    @Test
    fun `includes one-time reward ids`() = runTest {
        repository.recordRedemption(RewardCatalogIds.BADGE_GOLD, 200, couponCode = null)
        repository.recordRedemption(RewardCatalogIds.THEME_PACK, 800, couponCode = null)

        useCase().test {
            assertEquals(
                setOf(RewardCatalogIds.BADGE_GOLD, RewardCatalogIds.THEME_PACK),
                awaitItem(),
            )
        }
    }

    @Test
    fun `excludes repeatable reward ids`() = runTest {
        repository.recordRedemption(RewardCatalogIds.COFFEE_COUPON, 500, couponCode = null)
        repository.recordRedemption(RewardCatalogIds.DONATION, 500, couponCode = null)

        useCase().test {
            assertEquals(emptySet(), awaitItem())
        }
    }
}
