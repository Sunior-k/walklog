package com.river.walklog.feature.settings

import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.SignInWithGoogleUseCase
import com.river.walklog.core.domain.usecase.SignOutUseCase
import com.river.walklog.core.model.ThemeMode
import com.river.walklog.core.model.UserSettings
import com.river.walklog.core.testing.MainDispatcherRule
import com.river.walklog.core.testing.repository.FakeAuthRepository
import com.river.walklog.core.testing.repository.FakeUserSettingsRepository
import com.river.walklog.core.testing.repository.defaultAuthUser
import com.river.walklog.core.testing.repository.defaultUserSettings
import io.mockk.coVerify
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var repository: FakeUserSettingsRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var signInWithGoogle: SignInWithGoogleUseCase
    private lateinit var signOutUseCase: SignOutUseCase
    private lateinit var crashReporter: CrashReporter

    @Before
    fun setUp() {
        repository = FakeUserSettingsRepository()
        authRepository = FakeAuthRepository()
        signInWithGoogle = mockk(relaxed = true)
        signOutUseCase = mockk(relaxed = true)
        crashReporter = mockk(relaxed = true)
    }

    // 초기 상태 및 설정 로딩

    @Test
    fun `state is populated from repository settings on init`() {
        repository.setSettings(
            UserSettings(
                isOnboardingCompleted = true,
                nickname = "",
                totalPoints = 0,
                dailyStepGoal = 8_000,
                notificationsEnabled = false,
                recoveryMissionSteps = 3_000,
                themeMode = ThemeMode.DARK,
                lastDailyMissionAwardedDate = "",
                lastRecoveryMissionAwardedDate = "",
                userId = "",
            ),
        )
        val viewModel = SettingsViewModel(repository, authRepository, signInWithGoogle, signOutUseCase, crashReporter)

        assertEquals(8_000, viewModel.state.value.dailyStepGoal)
        assertFalse(viewModel.state.value.notificationsEnabled)
        assertEquals(3_000, viewModel.state.value.recoveryMissionSteps)
        assertEquals(ThemeMode.DARK, viewModel.state.value.themeMode)
    }

    @Test
    fun `isLoading becomes false after settings emit`() {
        val viewModel = SettingsViewModel(repository, authRepository, signInWithGoogle, signOutUseCase, crashReporter)

        assertFalse(viewModel.state.value.isLoading)
    }

    // 목표 걸음 수 변경

    @Test
    fun `OnStepGoalChanged calls setDailyStepGoal`() = runTest {
        val viewModel = SettingsViewModel(repository, authRepository, signInWithGoogle, signOutUseCase, crashReporter)

        viewModel.updateStepGoal(12_000)
        advanceUntilIdle()

        assertEquals(12_000, repository.settings.value.dailyStepGoal)
    }

    // 알림 설정 변경

    @Test
    fun `OnNotificationsToggled calls setNotificationsEnabled with false`() = runTest {
        val viewModel = SettingsViewModel(repository, authRepository, signInWithGoogle, signOutUseCase, crashReporter)

        viewModel.updateNotifications(false)
        advanceUntilIdle()

        assertFalse(repository.settings.value.notificationsEnabled)
    }

    @Test
    fun `OnNotificationsToggled calls setNotificationsEnabled with true`() = runTest {
        repository.setSettings(defaultUserSettings(notificationsEnabled = false))
        val viewModel = SettingsViewModel(repository, authRepository, signInWithGoogle, signOutUseCase, crashReporter)

        viewModel.updateNotifications(true)
        advanceUntilIdle()

        assertTrue(repository.settings.value.notificationsEnabled)
    }

    // 회복 미션 걸음 수 변경

    @Test
    fun `OnRecoveryStepsChanged calls setRecoveryMissionSteps`() = runTest {
        val viewModel = SettingsViewModel(repository, authRepository, signInWithGoogle, signOutUseCase, crashReporter)

        viewModel.updateRecoverySteps(4_000)
        advanceUntilIdle()

        assertEquals(4_000, repository.settings.value.recoveryMissionSteps)
    }

    // 테마 모드 변경

    @Test
    fun `OnThemeModeChanged with LIGHT calls setThemeMode`() = runTest {
        val viewModel = SettingsViewModel(repository, authRepository, signInWithGoogle, signOutUseCase, crashReporter)

        viewModel.updateThemeMode(ThemeMode.LIGHT)
        advanceUntilIdle()

        assertEquals(ThemeMode.LIGHT, repository.settings.value.themeMode)
    }

    @Test
    fun `OnThemeModeChanged with DARK calls setThemeMode`() = runTest {
        val viewModel = SettingsViewModel(repository, authRepository, signInWithGoogle, signOutUseCase, crashReporter)

        viewModel.updateThemeMode(ThemeMode.DARK)
        advanceUntilIdle()

        assertEquals(ThemeMode.DARK, repository.settings.value.themeMode)
    }

    @Test
    fun `OnThemeModeChanged with SYSTEM calls setThemeMode`() = runTest {
        val viewModel = SettingsViewModel(repository, authRepository, signInWithGoogle, signOutUseCase, crashReporter)

        viewModel.updateThemeMode(ThemeMode.SYSTEM)
        advanceUntilIdle()

        assertEquals(ThemeMode.SYSTEM, repository.settings.value.themeMode)
    }

    // 닉네임 변경

    @Test
    fun `updateNickname saves trimmed nickname to repository`() = runTest {
        val viewModel = SettingsViewModel(repository, authRepository, signInWithGoogle, signOutUseCase, crashReporter)

        viewModel.updateNickname("  Alice  ")
        advanceUntilIdle()

        assertEquals("Alice", repository.settings.value.nickname)
    }

    @Test
    fun `updateNickname with blank string saves empty string`() = runTest {
        val viewModel = SettingsViewModel(repository, authRepository, signInWithGoogle, signOutUseCase, crashReporter)

        viewModel.updateNickname("   ")
        advanceUntilIdle()

        assertEquals("", repository.settings.value.nickname)
    }

    // 로그아웃

    @Test
    fun `signOut calls signOutUseCase`() = runTest {
        val viewModel = SettingsViewModel(repository, authRepository, signInWithGoogle, signOutUseCase, crashReporter)

        viewModel.signOut()
        advanceUntilIdle()

        coVerify { signOutUseCase() }
    }

    // 인증 상태 반영

    @Test
    fun `isSignedIn is true when auth user is set`() = runTest {
        authRepository.setCurrentUser(defaultAuthUser(email = "user@test.com"))
        val viewModel = SettingsViewModel(repository, authRepository, signInWithGoogle, signOutUseCase, crashReporter)

        assertTrue(viewModel.state.value.isSignedIn)
    }

    @Test
    fun `isSignedIn is false when no auth user`() = runTest {
        authRepository.setCurrentUser(null)
        val viewModel = SettingsViewModel(repository, authRepository, signInWithGoogle, signOutUseCase, crashReporter)

        assertFalse(viewModel.state.value.isSignedIn)
    }

    @Test
    fun `userEmail reflects current user email`() = runTest {
        authRepository.setCurrentUser(defaultAuthUser(email = "hello@walklog.io"))
        val viewModel = SettingsViewModel(repository, authRepository, signInWithGoogle, signOutUseCase, crashReporter)

        assertEquals("hello@walklog.io", viewModel.state.value.userEmail)
    }

    @Test
    fun `userEmail is empty when no auth user`() = runTest {
        authRepository.setCurrentUser(null)
        val viewModel = SettingsViewModel(repository, authRepository, signInWithGoogle, signOutUseCase, crashReporter)

        assertEquals("", viewModel.state.value.userEmail)
    }

    // SCREEN crash key

    @Test
    fun `init sets SETTINGS crash key`() {
        val viewModel = SettingsViewModel(repository, authRepository, signInWithGoogle, signOutUseCase, crashReporter)
        verify { crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.SETTINGS) }
    }

    // nickname 상태 반영

    @Test
    fun `nickname in state reflects repository value`() {
        repository.setSettings(defaultUserSettings(nickname = "Bob"))
        val viewModel = SettingsViewModel(repository, authRepository, signInWithGoogle, signOutUseCase, crashReporter)

        assertEquals("Bob", viewModel.state.value.nickname)
    }

    @Test
    fun `totalPoints in state reflects repository value`() {
        repository.setSettings(defaultUserSettings(totalPoints = 500))
        val viewModel = SettingsViewModel(repository, authRepository, signInWithGoogle, signOutUseCase, crashReporter)

        assertEquals(500, viewModel.state.value.totalPoints)
    }
}
