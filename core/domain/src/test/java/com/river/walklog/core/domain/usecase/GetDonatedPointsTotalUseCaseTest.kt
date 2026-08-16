package com.river.walklog.core.domain.usecase

import com.river.walklog.core.model.RewardCatalogIds
import com.river.walklog.core.testing.repository.FakeRewardRedemptionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetDonatedPointsTotalUseCaseTest {

    private lateinit var rewardRedemptionRepository: FakeRewardRedemptionRepository
    private lateinit var useCase: GetDonatedPointsTotalUseCase

    @Before
    fun setUp() {
        rewardRedemptionRepository = FakeRewardRedemptionRepository()
        useCase = GetDonatedPointsTotalUseCase(rewardRedemptionRepository)
    }

    @Test
    fun `sums points across multiple donation redemptions`() = runTest {
        rewardRedemptionRepository.recordRedemption(RewardCatalogIds.DONATION, 500, null)
        rewardRedemptionRepository.recordRedemption(RewardCatalogIds.DONATION, 500, null)
        rewardRedemptionRepository.recordRedemption(RewardCatalogIds.BADGE_GOLD, 200, null)

        assertEquals(1000, useCase().first())
    }

    @Test
    fun `returns zero when no donations recorded`() = runTest {
        assertEquals(0, useCase().first())
    }
}
