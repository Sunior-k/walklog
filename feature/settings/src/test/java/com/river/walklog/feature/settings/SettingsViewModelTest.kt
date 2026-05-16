package com.river.walklog.feature.settings

import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.model.ThemeMode
import com.river.walklog.core.model.UserSettings
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
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var repository: FakeUserSettingsRepository
    private lateinit var crashReporter: CrashReporter

    @Before
    fun setUp() {
        repository = FakeUserSettingsRepository()
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
            ),
        )
        val viewModel = SettingsViewModel(repository, crashReporter)

        assertEquals(8_000, viewModel.state.value.dailyStepGoal)
        assertFalse(viewModel.state.value.notificationsEnabled)
        assertEquals(3_000, viewModel.state.value.recoveryMissionSteps)
        assertEquals(ThemeMode.DARK, viewModel.state.value.themeMode)
    }

    @Test
    fun `isLoading becomes false after settings emit`() {
        val viewModel = SettingsViewModel(repository, crashReporter)

        assertFalse(viewModel.state.value.isLoading)
    }

    // 목표 걸음 수 변경

    @Test
    fun `OnStepGoalChanged calls setDailyStepGoal`() = runTest {
        val viewModel = SettingsViewModel(repository, crashReporter)

        viewModel.updateStepGoal(12_000)
        advanceUntilIdle()

        assertEquals(12_000, repository.settings.value.dailyStepGoal)
    }

    // 알림 설정 변경

    @Test
    fun `OnNotificationsToggled calls setNotificationsEnabled with false`() = runTest {
        val viewModel = SettingsViewModel(repository, crashReporter)

        viewModel.updateNotifications(false)
        advanceUntilIdle()

        assertFalse(repository.settings.value.notificationsEnabled)
    }

    @Test
    fun `OnNotificationsToggled calls setNotificationsEnabled with true`() = runTest {
        repository.setSettings(defaultUserSettings(notificationsEnabled = false))
        val viewModel = SettingsViewModel(repository, crashReporter)

        viewModel.updateNotifications(true)
        advanceUntilIdle()

        assertTrue(repository.settings.value.notificationsEnabled)
    }

    // 회복 미션 걸음 수 변경

    @Test
    fun `OnRecoveryStepsChanged calls setRecoveryMissionSteps`() = runTest {
        val viewModel = SettingsViewModel(repository, crashReporter)

        viewModel.updateRecoverySteps(4_000)
        advanceUntilIdle()

        assertEquals(4_000, repository.settings.value.recoveryMissionSteps)
    }

    // 테마 모드 변경

    @Test
    fun `OnThemeModeChanged with LIGHT calls setThemeMode`() = runTest {
        val viewModel = SettingsViewModel(repository, crashReporter)

        viewModel.updateThemeMode(ThemeMode.LIGHT)
        advanceUntilIdle()

        assertEquals(ThemeMode.LIGHT, repository.settings.value.themeMode)
    }

    @Test
    fun `OnThemeModeChanged with DARK calls setThemeMode`() = runTest {
        val viewModel = SettingsViewModel(repository, crashReporter)

        viewModel.updateThemeMode(ThemeMode.DARK)
        advanceUntilIdle()

        assertEquals(ThemeMode.DARK, repository.settings.value.themeMode)
    }

    @Test
    fun `OnThemeModeChanged with SYSTEM calls setThemeMode`() = runTest {
        val viewModel = SettingsViewModel(repository, crashReporter)

        viewModel.updateThemeMode(ThemeMode.SYSTEM)
        advanceUntilIdle()

        assertEquals(ThemeMode.SYSTEM, repository.settings.value.themeMode)
    }
}
