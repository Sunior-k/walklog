package com.river.walklog.feature.onboarding

import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.data.repository.UserSettingsRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: UserSettingsRepository
    private lateinit var crashReporter: CrashReporter
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        crashReporter = mockk(relaxed = true)
        viewModel = OnboardingViewModel(repository, crashReporter)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── 초기 상태 ─────────────────────────────────────────────────────────

    @Test
    fun `initial page is 0`() {
        assertEquals(0, viewModel.state.value.currentPage)
    }

    @Test
    fun `initial navigateToHome is false`() {
        assertFalse(viewModel.navigateToHome.value)
    }

    // ─── 페이지 이동 ───────────────────────────────────────────────────────

    @Test
    fun `OnClickNext advances to page 1`() {
        viewModel.handleIntent(OnboardingIntent.OnClickNext)
        assertEquals(1, viewModel.state.value.currentPage)
    }

    @Test
    fun `OnClickNext on page 1 advances to page 2`() {
        viewModel.handleIntent(OnboardingIntent.OnClickNext)
        viewModel.handleIntent(OnboardingIntent.OnClickNext)
        assertEquals(2, viewModel.state.value.currentPage)
    }

    @Test
    fun `OnClickBack on page 1 returns to page 0`() {
        viewModel.handleIntent(OnboardingIntent.OnClickNext)
        viewModel.handleIntent(OnboardingIntent.OnClickBack)
        assertEquals(0, viewModel.state.value.currentPage)
    }

    @Test
    fun `OnClickBack on page 0 stays at page 0`() {
        viewModel.handleIntent(OnboardingIntent.OnClickBack)
        assertEquals(0, viewModel.state.value.currentPage)
    }

    @Test
    fun `OnPermissionResult advances page same as OnClickNext`() {
        viewModel.handleIntent(OnboardingIntent.OnPermissionResult(granted = true))
        assertEquals(1, viewModel.state.value.currentPage)
    }

    // ─── 상태 업데이트 ─────────────────────────────────────────────────────

    @Test
    fun `OnStepGoalChanged updates dailyStepGoal`() {
        viewModel.handleIntent(OnboardingIntent.OnStepGoalChanged(8_000))
        assertEquals(8_000, viewModel.state.value.dailyStepGoal)
    }

    @Test
    fun `OnNotificationsToggled updates notificationsEnabled to false`() {
        viewModel.handleIntent(OnboardingIntent.OnNotificationsToggled(false))
        assertFalse(viewModel.state.value.notificationsEnabled)
    }

    @Test
    fun `OnNotificationsToggled updates notificationsEnabled to true`() {
        viewModel.handleIntent(OnboardingIntent.OnNotificationsToggled(false))
        viewModel.handleIntent(OnboardingIntent.OnNotificationsToggled(true))
        assertTrue(viewModel.state.value.notificationsEnabled)
    }

    // ─── 완료 처리 ─────────────────────────────────────────────────────────

    @Test
    fun `OnClickComplete calls setDailyStepGoal with current goal`() = runTest {
        viewModel.handleIntent(OnboardingIntent.OnStepGoalChanged(9_000))
        viewModel.handleIntent(OnboardingIntent.OnClickComplete)
        advanceUntilIdle()

        coVerify { repository.setDailyStepGoal(9_000) }
    }

    @Test
    fun `OnClickComplete calls setNotificationsEnabled with current value`() = runTest {
        viewModel.handleIntent(OnboardingIntent.OnNotificationsToggled(false))
        viewModel.handleIntent(OnboardingIntent.OnClickComplete)
        advanceUntilIdle()

        coVerify { repository.setNotificationsEnabled(false) }
    }

    @Test
    fun `OnClickComplete calls setOnboardingCompleted`() = runTest {
        viewModel.handleIntent(OnboardingIntent.OnClickComplete)
        advanceUntilIdle()

        coVerify { repository.setOnboardingCompleted() }
    }

    @Test
    fun `OnClickComplete sets navigateToHome to true`() = runTest {
        viewModel.handleIntent(OnboardingIntent.OnClickComplete)
        advanceUntilIdle()

        assertTrue(viewModel.navigateToHome.value)
    }

    @Test
    fun `OnClickNext on last page triggers complete`() = runTest {
        // 페이지 0 → 1 → 2 → complete (TOTAL_PAGES = 3)
        repeat(3) { viewModel.handleIntent(OnboardingIntent.OnClickNext) }
        advanceUntilIdle()

        coVerify { repository.setOnboardingCompleted() }
        assertTrue(viewModel.navigateToHome.value)
    }

    @Test
    fun `isCompleting is true after OnClickComplete`() = runTest {
        // UnconfinedTestDispatcher로 즉시 실행되므로 완료 후 상태 확인
        viewModel.handleIntent(OnboardingIntent.OnClickComplete)
        // isCompleting은 coroutine 시작 전 set → 완료 후에도 true 유지
        assertTrue(viewModel.state.value.isCompleting)
    }
}
