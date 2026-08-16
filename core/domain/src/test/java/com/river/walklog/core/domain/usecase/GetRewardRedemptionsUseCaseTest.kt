package com.river.walklog.core.domain.usecase

import com.river.walklog.core.model.RewardCatalogIds
import com.river.walklog.core.testing.repository.FakeRewardRedemptionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetRewardRedemptionsUseCaseTest {

    private lateinit var rewardRedemptionRepository: FakeRewardRedemptionRepository
    private lateinit var useCase: GetRewardRedemptionsUseCase

    @Before
    fun setUp() {
        rewardRedemptionRepository = FakeRewardRedemptionRepository()
        useCase = GetRewardRedemptionsUseCase(rewardRedemptionRepository)
    }

    @Test
    fun `returns only redemptions matching the requested rewardId`() = runTest {
        rewardRedemptionRepository.recordRedemption(RewardCatalogIds.BADGE_GOLD, 200, null)
        rewardRedemptionRepository.recordRedemption(RewardCatalogIds.DONATION, 500, null)

        val redemptions = useCase(RewardCatalogIds.BADGE_GOLD).first()

        assertEquals(1, redemptions.size)
        assertEquals(RewardCatalogIds.BADGE_GOLD, redemptions.single().rewardId)
    }

    @Test
    fun `returns empty list when reward never redeemed`() = runTest {
        val redemptions = useCase(RewardCatalogIds.BADGE_GOLD).first()

        assertTrue(redemptions.isEmpty())
    }
}
