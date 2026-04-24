package com.river.walklog.feature.settings

import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.model.ThemeMode
import com.river.walklog.core.model.UserSettings
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: UserSettingsRepository
    private lateinit var crashReporter: CrashReporter

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        crashReporter = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // 초기 상태 및 설정 로딩

    @Test
    fun `state is populated from repository settings on init`() {
        val settings = UserSettings(
            isOnboardingCompleted = true,
            dailyStepGoal = 8_000,
            notificationsEnabled = false,
            recoveryMissionSteps = 3_000,
            themeMode = ThemeMode.DARK,
        )
        every { repository.settings } returns flowOf(settings)
        val viewModel = SettingsViewModel(repository, crashReporter)

        assertEquals(8_000, viewModel.state.value.dailyStepGoal)
        assertFalse(viewModel.state.value.notificationsEnabled)
        assertEquals(3_000, viewModel.state.value.recoveryMissionSteps)
        assertEquals(ThemeMode.DARK, viewModel.state.value.themeMode)
    }

    @Test
    fun `isLoading becomes false after settings emit`() {
        every { repository.settings } returns flowOf(defaultSettings())
        val viewModel = SettingsViewModel(repository, crashReporter)

        assertFalse(viewModel.state.value.isLoading)
    }

    // 목표 걸음 수 변경

    @Test
    fun `OnStepGoalChanged calls setDailyStepGoal`() = runTest {
        every { repository.settings } returns flowOf(defaultSettings())
        val viewModel = SettingsViewModel(repository, crashReporter)

        viewModel.handleIntent(SettingsIntent.OnStepGoalChanged(12_000))
        advanceUntilIdle()

        coVerify { repository.setDailyStepGoal(12_000) }
    }

    // 알림 설정 변경

    @Test
    fun `OnNotificationsToggled calls setNotificationsEnabled with false`() = runTest {
        every { repository.settings } returns flowOf(defaultSettings())
        val viewModel = SettingsViewModel(repository, crashReporter)

        viewModel.handleIntent(SettingsIntent.OnNotificationsToggled(false))
        advanceUntilIdle()

        coVerify { repository.setNotificationsEnabled(false) }
    }

    @Test
    fun `OnNotificationsToggled calls setNotificationsEnabled with true`() = runTest {
        every { repository.settings } returns flowOf(defaultSettings())
        val viewModel = SettingsViewModel(repository, crashReporter)

        viewModel.handleIntent(SettingsIntent.OnNotificationsToggled(true))
        advanceUntilIdle()

        coVerify { repository.setNotificationsEnabled(true) }
    }

    // 회복 미션 걸음 수 변경

    @Test
    fun `OnRecoveryStepsChanged calls setRecoveryMissionSteps`() = runTest {
        every { repository.settings } returns flowOf(defaultSettings())
        val viewModel = SettingsViewModel(repository, crashReporter)

        viewModel.handleIntent(SettingsIntent.OnRecoveryStepsChanged(4_000))
        advanceUntilIdle()

        coVerify { repository.setRecoveryMissionSteps(4_000) }
    }

    // 테마 모드 변경

    @Test
    fun `OnThemeModeChanged with LIGHT calls setThemeMode`() = runTest {
        every { repository.settings } returns flowOf(defaultSettings())
        val viewModel = SettingsViewModel(repository, crashReporter)

        viewModel.handleIntent(SettingsIntent.OnThemeModeChanged(ThemeMode.LIGHT))
        advanceUntilIdle()

        coVerify { repository.setThemeMode(ThemeMode.LIGHT) }
    }

    @Test
    fun `OnThemeModeChanged with DARK calls setThemeMode`() = runTest {
        every { repository.settings } returns flowOf(defaultSettings())
        val viewModel = SettingsViewModel(repository, crashReporter)

        viewModel.handleIntent(SettingsIntent.OnThemeModeChanged(ThemeMode.DARK))
        advanceUntilIdle()

        coVerify { repository.setThemeMode(ThemeMode.DARK) }
    }

    @Test
    fun `OnThemeModeChanged with SYSTEM calls setThemeMode`() = runTest {
        every { repository.settings } returns flowOf(defaultSettings())
        val viewModel = SettingsViewModel(repository, crashReporter)

        viewModel.handleIntent(SettingsIntent.OnThemeModeChanged(ThemeMode.SYSTEM))
        advanceUntilIdle()

        coVerify { repository.setThemeMode(ThemeMode.SYSTEM) }
    }

    private fun defaultSettings() = UserSettings(
        isOnboardingCompleted = true,
        dailyStepGoal = 10_000,
        notificationsEnabled = true,
        recoveryMissionSteps = 6_000,
        themeMode = ThemeMode.SYSTEM,
    )
}
