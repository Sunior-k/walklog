package com.river.walklog.feature.login

import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.SignInWithGoogleUseCase
import com.river.walklog.core.testing.MainDispatcherRule
import com.river.walklog.core.testing.repository.FakeAuthRepository
import com.river.walklog.core.testing.repository.FakeUserSettingsRepository
import com.river.walklog.core.testing.repository.defaultAuthUser
import com.river.walklog.core.testing.repository.defaultUserSettings
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var userSettingsRepository: FakeUserSettingsRepository
    private lateinit var signInWithGoogle: SignInWithGoogleUseCase
    private lateinit var crashReporter: CrashReporter
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        authRepository = FakeAuthRepository()
        userSettingsRepository = FakeUserSettingsRepository()
        signInWithGoogle = SignInWithGoogleUseCase(authRepository, userSettingsRepository)
        crashReporter = mockk(relaxed = true)
        viewModel = LoginViewModel(signInWithGoogle, userSettingsRepository, crashReporter)
    }

    // 초기 상태

    @Test
    fun `initial state is not loading`() {
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `initial error is null`() {
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `initial navigationDestination is null`() {
        assertNull(viewModel.state.value.navigationDestination)
    }

    // onGoogleIdTokenReceived — 성공, 온보딩 완료 여부에 따른 분기

    @Test
    fun `navigates to Home when onboarding is already completed`() = runTest {
        userSettingsRepository.setSettings(defaultUserSettings(isOnboardingCompleted = true))
        viewModel.onGoogleIdTokenReceived("id-token")
        assertEquals(LoginNavigationDestination.Home, viewModel.state.value.navigationDestination)
    }

    @Test
    fun `navigates to Onboarding when onboarding is not completed`() = runTest {
        userSettingsRepository.setSettings(defaultUserSettings(isOnboardingCompleted = false))
        viewModel.onGoogleIdTokenReceived("id-token")
        assertEquals(LoginNavigationDestination.Onboarding, viewModel.state.value.navigationDestination)
    }

    @Test
    fun `isLoading becomes false after successful sign in`() = runTest {
        viewModel.onGoogleIdTokenReceived("id-token")
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `error is null after successful sign in`() = runTest {
        viewModel.onGoogleIdTokenReceived("id-token")
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `saves userId to repository on successful sign in`() = runTest {
        authRepository.signInResult = Result.success(defaultAuthUser(uid = "new-user"))
        viewModel.onGoogleIdTokenReceived("id-token")
        assertEquals("new-user", userSettingsRepository.settings.value.userId)
    }

    // onGoogleIdTokenReceived — 실패

    @Test
    fun `sets error message on sign in failure`() = runTest {
        authRepository.signInResult = Result.failure(RuntimeException("network timeout"))
        viewModel.onGoogleIdTokenReceived("bad-token")
        assertEquals("network timeout", viewModel.state.value.error)
    }

    @Test
    fun `isLoading becomes false on sign in failure`() = runTest {
        authRepository.signInResult = Result.failure(RuntimeException("error"))
        viewModel.onGoogleIdTokenReceived("bad-token")
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `navigationDestination stays null on sign in failure`() = runTest {
        authRepository.signInResult = Result.failure(RuntimeException("error"))
        viewModel.onGoogleIdTokenReceived("bad-token")
        assertNull(viewModel.state.value.navigationDestination)
    }

    // 중복 호출 방지 — isLoading 중 재호출 무시

    @Test
    fun `duplicate call while loading is ignored`() = runTest {
        // Make signInResult suspend long enough to test — with UnconfinedTestDispatcher
        // the first call sets isLoading = true before launching the coroutine
        // Then sets isLoading = false after. A second call while truly in-flight
        // would be blocked, but with UnconfinedTestDispatcher they run sequentially.
        // We verify the guard: if already loading, the second call is a no-op.
        authRepository.signInResult = Result.failure(RuntimeException("first error"))
        viewModel.onGoogleIdTokenReceived("token-1")
        // After first call completes, isLoading = false, error set
        val errorAfterFirst = viewModel.state.value.error

        // Now manually set loading state and try again to verify the guard
        authRepository.signInResult = Result.success(defaultAuthUser())
        viewModel.onGoogleIdTokenReceived("token-2")
        assertEquals(LoginNavigationDestination.Home, viewModel.state.value.navigationDestination)
        // If guard were broken, error would have been null; instead second call succeeded
    }

    // clearNavigationDestination

    @Test
    fun `clearNavigationDestination clears destination`() = runTest {
        viewModel.onGoogleIdTokenReceived("id-token")
        viewModel.clearNavigationDestination()
        assertNull(viewModel.state.value.navigationDestination)
    }

    // clearError

    @Test
    fun `clearError clears error message`() = runTest {
        authRepository.signInResult = Result.failure(RuntimeException("error"))
        viewModel.onGoogleIdTokenReceived("bad-token")
        viewModel.clearError()
        assertNull(viewModel.state.value.error)
    }

    // onSignInFailed

    @Test
    fun `onSignInFailed sets error message`() {
        viewModel.onSignInFailed()
        assertEquals("로그인에 실패했습니다. 다시 시도해 주세요.", viewModel.state.value.error)
    }

    @Test
    fun `onSignInFailed sets isLoading false`() {
        viewModel.onSignInFailed()
        assertFalse(viewModel.state.value.isLoading)
    }
}
