package com.river.walklog.feature.onboarding

import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.testing.MainDispatcherRule
import com.river.walklog.core.testing.repository.FakeUserSettingsRepository
import com.river.walklog.core.testing.repository.defaultUserSettings
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var userSettingsRepository: FakeUserSettingsRepository
    private lateinit var crashReporter: CrashReporter
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        userSettingsRepository = FakeUserSettingsRepository(defaultUserSettings(isOnboardingCompleted = false))
        crashReporter = mockk(relaxed = true)
        viewModel = OnboardingViewModel(userSettingsRepository, crashReporter)
    }

    // 초기 상태

    @Test
    fun `initial page is 0`() {
        assertEquals(0, viewModel.state.value.currentPage)
    }

    @Test
    fun `initial navigation destination is null`() {
        assertEquals(null, viewModel.state.value.navigationDestination)
    }

    // 페이지 이동

    @Test
    fun `OnClickNext advances to page 1`() {
        viewModel.advancePage()
        assertEquals(1, viewModel.state.value.currentPage)
    }

    @Test
    fun `OnClickNext on page 1 advances to page 2`() {
        viewModel.advancePage()
        viewModel.advancePage()
        assertEquals(2, viewModel.state.value.currentPage)
    }

    @Test
    fun `OnClickBack on page 1 returns to page 0`() {
        viewModel.advancePage()
        viewModel.retreatPage()
        assertEquals(0, viewModel.state.value.currentPage)
    }

    @Test
    fun `OnClickBack on page 0 stays at page 0`() {
        viewModel.retreatPage()
        assertEquals(0, viewModel.state.value.currentPage)
    }

    @Test
    fun `OnPermissionResult advances page same as OnClickNext`() {
        viewModel.advancePage()
        assertEquals(1, viewModel.state.value.currentPage)
    }

    // 상태 업데이트

    @Test
    fun `OnStepGoalChanged updates dailyStepGoal`() {
        viewModel.updateStepGoal(8_000)
        assertEquals(8_000, viewModel.state.value.dailyStepGoal)
    }

    @Test
    fun `OnNotificationsToggled updates notificationsEnabled to false`() {
        viewModel.updateNotifications(false)
        assertFalse(viewModel.state.value.notificationsEnabled)
    }

    @Test
    fun `OnNotificationsToggled updates notificationsEnabled to true`() {
        viewModel.updateNotifications(false)
        viewModel.updateNotifications(true)
        assertTrue(viewModel.state.value.notificationsEnabled)
    }

    // 완료

    @Test
    fun `OnClickComplete saves dailyStepGoal to repository`() = runTest {
        viewModel.updateStepGoal(9_000)
        viewModel.complete()
        advanceUntilIdle()

        assertEquals(9_000, userSettingsRepository.settings.value.dailyStepGoal)
    }

    @Test
    fun `OnClickComplete saves notificationsEnabled to repository`() = runTest {
        viewModel.updateNotifications(false)
        viewModel.complete()
        advanceUntilIdle()

        assertFalse(userSettingsRepository.settings.value.notificationsEnabled)
    }

    @Test
    fun `OnClickComplete completes onboarding in repository`() = runTest {
        viewModel.complete()
        advanceUntilIdle()

        assertTrue(userSettingsRepository.settings.value.isOnboardingCompleted)
    }

    @Test
    fun `OnClickComplete sets navigation destination to Home`() = runTest {
        viewModel.complete()
        advanceUntilIdle()

        assertEquals(OnboardingNavigationDestination.Home, viewModel.state.value.navigationDestination)
    }

    @Test
    fun `OnNavigationHandled clears navigation destination`() = runTest {
        viewModel.complete()
        advanceUntilIdle()

        viewModel.clearNavigationDestination()

        assertEquals(null, viewModel.state.value.navigationDestination)
    }

    @Test
    fun `OnClickNext on last page triggers complete`() = runTest {
        // 페이지 0 → 1 → 2 → 3 → complete (TOTAL_PAGES = 4)
        repeat(4) { viewModel.advancePage() }
        advanceUntilIdle()

        assertTrue(userSettingsRepository.settings.value.isOnboardingCompleted)
        assertEquals(OnboardingNavigationDestination.Home, viewModel.state.value.navigationDestination)
    }

    @Test
    fun `isCompleting is true after OnClickComplete`() = runTest {
        viewModel.complete()
        assertTrue(viewModel.state.value.isCompleting)
    }
}
