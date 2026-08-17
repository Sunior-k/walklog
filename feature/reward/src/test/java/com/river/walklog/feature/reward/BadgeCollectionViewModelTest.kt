package com.river.walklog.feature.reward

import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.GetRewardRedemptionsUseCase
import com.river.walklog.core.model.RewardCatalogIds
import com.river.walklog.core.testing.MainDispatcherRule
import com.river.walklog.core.testing.repository.FakeRewardRedemptionRepository
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BadgeCollectionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var rewardRedemptionRepository: FakeRewardRedemptionRepository
    private val crashReporter: CrashReporter = mockk(relaxed = true)

    private fun createViewModel() =
        BadgeCollectionViewModel(GetRewardRedemptionsUseCase(rewardRedemptionRepository), crashReporter)

    @Test
    fun `init sets SCREEN crash key to BADGE_COLLECTION`() {
        rewardRedemptionRepository = FakeRewardRedemptionRepository()

        createViewModel()

        verify { crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.BADGE_COLLECTION) }
    }

    @Test
    fun `isGoldBadgeOwned is false when never redeemed`() {
        rewardRedemptionRepository = FakeRewardRedemptionRepository()

        val viewModel = createViewModel()

        assertFalse(viewModel.state.value.isGoldBadgeOwned)
    }

    @Test
    fun `isGoldBadgeOwned is true after gold badge redeemed`() = runTest {
        rewardRedemptionRepository = FakeRewardRedemptionRepository()
        rewardRedemptionRepository.recordRedemption(RewardCatalogIds.BADGE_GOLD, 200, null)

        val viewModel = createViewModel()

        assertTrue(viewModel.state.value.isGoldBadgeOwned)
    }

    @Test
    fun `isGoldBadgeOwned is false when a different reward redeemed`() = runTest {
        rewardRedemptionRepository = FakeRewardRedemptionRepository()
        rewardRedemptionRepository.recordRedemption(RewardCatalogIds.DONATION, 500, null)

        val viewModel = createViewModel()

        assertFalse(viewModel.state.value.isGoldBadgeOwned)
    }
}
