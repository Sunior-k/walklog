package com.river.walklog.feature.reward

import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.testing.MainDispatcherRule
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class RewardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val crashReporter: CrashReporter = mockk(relaxed = true)

    @Test
    fun `initial state is default RewardState`() {
        assertEquals(RewardState(), RewardViewModel(crashReporter).state.value)
    }

    @Test
    fun `init sets SCREEN crash key to REWARD`() {
        RewardViewModel(crashReporter)
        verify { crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.REWARD) }
    }

    @Test
    fun `onStoreCardClicked sets navigationDestination to Store`() {
        val viewModel = RewardViewModel(crashReporter)

        viewModel.onStoreCardClicked()

        assertEquals(RewardDest.Store, viewModel.state.value.navigationDestination)
    }

    @Test
    fun `clearNavigationDestination resets navigationDestination to null`() {
        val viewModel = RewardViewModel(crashReporter)
        viewModel.onStoreCardClicked()

        viewModel.clearNavigationDestination()

        assertEquals(null, viewModel.state.value.navigationDestination)
    }

    @Test
    fun `onPointsHistoryCardClicked sets navigationDestination to PointsHistory`() {
        val viewModel = RewardViewModel(crashReporter)

        viewModel.onPointsHistoryCardClicked()

        assertEquals(RewardDest.PointsHistory, viewModel.state.value.navigationDestination)
    }

    @Test
    fun `onBadgeCollectionCardClicked sets navigationDestination to BadgeCollection`() {
        val viewModel = RewardViewModel(crashReporter)

        viewModel.onBadgeCollectionCardClicked()

        assertEquals(RewardDest.BadgeCollection, viewModel.state.value.navigationDestination)
    }
}
