package com.river.walklog.feature.onboarding

import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.SignInWithGoogleUseCase
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
    private lateinit var signInWithGoogle: SignInWithGoogleUseCase
    private lateinit var crashReporter: CrashReporter
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        userSettingsRepository = FakeUserSettingsRepository(defaultUserSettings(isOnboardingCompleted = false))
        signInWithGoogle = mockk(relaxed = true)
        crashReporter = mockk(relaxed = true)
        viewModel = OnboardingViewModel(userSettingsRepository, signInWithGoogle, crashReporter)
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
    fun `confirmSkipSignIn advances from page 0 to page 1`() {
        viewModel.confirmSkipSignIn()
        assertEquals(1, viewModel.state.value.currentPage)
    }

    @Test
    fun `advancePage on page 1 advances to page 2`() {
        viewModel.confirmSkipSignIn()
        viewModel.advancePage()
        assertEquals(2, viewModel.state.value.currentPage)
    }

    @Test
    fun `retreatPage on page 1 returns to page 0`() {
        viewModel.confirmSkipSignIn()
        viewModel.retreatPage()
        assertEquals(0, viewModel.state.value.currentPage)
    }

    @Test
    fun `retreatPage on page 0 stays at page 0`() {
        viewModel.retreatPage()
        assertEquals(0, viewModel.state.value.currentPage)
    }

    // 상태 업데이트

    @Test
    fun `updateStepGoal updates dailyStepGoal`() {
        viewModel.updateStepGoal(8_000)
        assertEquals(8_000, viewModel.state.value.dailyStepGoal)
    }

    @Test
    fun `updateNotifications sets notificationsEnabled to false`() {
        viewModel.updateNotifications(false)
        assertFalse(viewModel.state.value.notificationsEnabled)
    }

    @Test
    fun `updateNotifications sets notificationsEnabled to true`() {
        viewModel.updateNotifications(false)
        viewModel.updateNotifications(true)
        assertTrue(viewModel.state.value.notificationsEnabled)
    }

    // 완료

    @Test
    fun `complete saves dailyStepGoal to repository`() = runTest {
        viewModel.updateStepGoal(9_000)
        viewModel.complete()
        advanceUntilIdle()

        assertEquals(9_000, userSettingsRepository.settings.value.dailyStepGoal)
    }

    @Test
    fun `complete saves notificationsEnabled to repository`() = runTest {
        viewModel.updateNotifications(false)
        viewModel.complete()
        advanceUntilIdle()

        assertFalse(userSettingsRepository.settings.value.notificationsEnabled)
    }

    @Test
    fun `complete sets isOnboardingCompleted in repository`() = runTest {
        viewModel.complete()
        advanceUntilIdle()

        assertTrue(userSettingsRepository.settings.value.isOnboardingCompleted)
    }

    @Test
    fun `complete sets navigation destination to Home`() = runTest {
        viewModel.complete()
        advanceUntilIdle()

        assertEquals(OnboardingNavigationDestination.Home, viewModel.state.value.navigationDestination)
    }

    @Test
    fun `clearNavigationDestination clears destination`() = runTest {
        viewModel.complete()
        advanceUntilIdle()

        viewModel.clearNavigationDestination()

        assertEquals(null, viewModel.state.value.navigationDestination)
    }

    @Test
    fun `advancePage on last page triggers complete`() = runTest {
        // Page 0(skip) → 1 → 2 → 3 → 4 → complete (TOTAL_PAGES = 5)
        viewModel.confirmSkipSignIn()
        repeat(4) { viewModel.advancePage() }
        advanceUntilIdle()

        assertTrue(userSettingsRepository.settings.value.isOnboardingCompleted)
        assertEquals(OnboardingNavigationDestination.Home, viewModel.state.value.navigationDestination)
    }

    @Test
    fun `isCompleting is true during complete`() = runTest {
        viewModel.complete()
        assertTrue(viewModel.state.value.isCompleting)
    }
}
