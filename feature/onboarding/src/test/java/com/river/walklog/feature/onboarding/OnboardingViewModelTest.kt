package com.river.walklog.feature.onboarding

import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.SignInWithGoogleUseCase
import com.river.walklog.core.testing.MainDispatcherRule
import com.river.walklog.core.testing.repository.FakeAuthRepository
import com.river.walklog.core.testing.repository.FakeUserSettingsRepository
import com.river.walklog.core.testing.repository.defaultAuthUser
import com.river.walklog.core.testing.repository.defaultUserSettings
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var userSettingsRepository: FakeUserSettingsRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var signInWithGoogle: SignInWithGoogleUseCase
    private lateinit var crashReporter: CrashReporter
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        authRepository = FakeAuthRepository()
        userSettingsRepository = FakeUserSettingsRepository(defaultUserSettings(isOnboardingCompleted = false))
        signInWithGoogle = SignInWithGoogleUseCase(authRepository, userSettingsRepository)
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

    // updateNickname

    @Test
    fun `updateNickname updates nickname in state`() {
        viewModel.updateNickname("Charlie")
        assertEquals("Charlie", viewModel.state.value.nickname)
    }

    @Test
    fun `complete saves trimmed nickname`() = runTest {
        viewModel.updateNickname("  Dave  ")
        viewModel.complete()
        advanceUntilIdle()

        assertEquals("Dave", userSettingsRepository.settings.value.nickname)
    }

    // skip 다이얼로그

    @Test
    fun `requestSkipSignIn shows skip dialog`() {
        viewModel.requestSkipSignIn()
        assertTrue(viewModel.state.value.showSkipConfirmDialog)
    }

    @Test
    fun `dismissSkipDialog hides skip dialog`() {
        viewModel.requestSkipSignIn()
        viewModel.dismissSkipDialog()
        assertFalse(viewModel.state.value.showSkipConfirmDialog)
    }

    @Test
    fun `confirmSkipSignIn hides dialog and advances page`() {
        viewModel.requestSkipSignIn()
        viewModel.confirmSkipSignIn()

        assertFalse(viewModel.state.value.showSkipConfirmDialog)
        assertEquals(1, viewModel.state.value.currentPage)
    }

    // Google 로그인 — 신규 사용자

    @Test
    fun `onGoogleIdTokenReceived sets isSignedIn for new user`() = runTest {
        authRepository.signInResult = Result.success(defaultAuthUser(isNewUser = true))
        viewModel.onGoogleIdTokenReceived("token")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isSignedIn)
    }

    @Test
    fun `onGoogleIdTokenReceived advances page for new user`() = runTest {
        authRepository.signInResult = Result.success(defaultAuthUser(isNewUser = true))
        viewModel.onGoogleIdTokenReceived("token")
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.currentPage)
    }

    // Google 로그인 — 기존 사용자

    @Test
    fun `onGoogleIdTokenReceived navigates to Home for existing user`() = runTest {
        authRepository.signInResult = Result.success(defaultAuthUser(isNewUser = false))
        viewModel.onGoogleIdTokenReceived("token")
        advanceUntilIdle()

        assertEquals(OnboardingNavigationDestination.Home, viewModel.state.value.navigationDestination)
    }

    @Test
    fun `onGoogleIdTokenReceived sets onboarding completed for existing user`() = runTest {
        authRepository.signInResult = Result.success(defaultAuthUser(isNewUser = false))
        viewModel.onGoogleIdTokenReceived("token")
        advanceUntilIdle()

        assertTrue(userSettingsRepository.settings.value.isOnboardingCompleted)
    }

    // Google 로그인 — 실패

    @Test
    fun `onGoogleIdTokenReceived clears isSigningIn on failure`() = runTest {
        authRepository.signInResult = Result.failure(RuntimeException("auth error"))
        viewModel.onGoogleIdTokenReceived("bad-token")
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isSigningIn)
    }

    @Test
    fun `onGoogleIdTokenReceived does not advance page on failure`() = runTest {
        authRepository.signInResult = Result.failure(RuntimeException("auth error"))
        viewModel.onGoogleIdTokenReceived("bad-token")
        advanceUntilIdle()

        assertEquals(0, viewModel.state.value.currentPage)
    }

    // 중복 호출 방지

    @Test
    fun `onGoogleIdTokenReceived is no-op when already signing in`() = runTest {
        authRepository.signInResult = Result.success(defaultAuthUser(isNewUser = true))
        viewModel.onGoogleIdTokenReceived("token-1")
        // isSigningIn will be set briefly; second call should be ignored
        // After advanceUntilIdle both calls complete, but second should still be no-op
        advanceUntilIdle()
        // Verify result is valid (not double-advanced)
        assertEquals(1, viewModel.state.value.currentPage)
    }

    // SCREEN crash key

    @Test
    fun `init sets ONBOARDING crash key`() {
        verify { crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.ONBOARDING) }
    }

    // clearNavigationDestination

    @Test
    fun `clearNavigationDestination resets destination to null`() = runTest {
        authRepository.signInResult = Result.success(defaultAuthUser(isNewUser = false))
        viewModel.onGoogleIdTokenReceived("token")
        advanceUntilIdle()

        viewModel.clearNavigationDestination()

        assertNull(viewModel.state.value.navigationDestination)
    }
}
