package com.river.walklog.core.ui

import app.cash.turbine.test
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.GetActiveThemeUseCase
import com.river.walklog.core.domain.usecase.GetPremiumVisualModeUseCase
import com.river.walklog.core.model.PremiumVisualMode
import com.river.walklog.core.testing.MainDispatcherRule
import com.river.walklog.core.testing.repository.FakeUserSettingsRepository
import com.river.walklog.core.testing.repository.defaultUserSettings
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var userSettingsRepository: FakeUserSettingsRepository
    private lateinit var viewModel: ThemeViewModel
    private val crashReporter: CrashReporter = mockk(relaxed = true)

    private fun createViewModel() {
        viewModel = ThemeViewModel(
            GetActiveThemeUseCase(userSettingsRepository),
            GetPremiumVisualModeUseCase(userSettingsRepository),
            crashReporter,
        )
    }

    @Test
    fun `isPremiumTheme reflects settings value`() = runTest {
        userSettingsRepository = FakeUserSettingsRepository().apply {
            setSettings(defaultUserSettings(isPremiumThemeActive = true))
        }
        createViewModel()

        viewModel.isPremiumTheme.test {
            assertEquals(true, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `premiumVisualMode reflects the persisted selection`() = runTest {
        userSettingsRepository = FakeUserSettingsRepository().apply {
            setSettings(defaultUserSettings(premiumVisualMode = PremiumVisualMode.DAY_WET))
        }
        createViewModel()

        viewModel.premiumVisualMode.test {
            assertEquals(PremiumVisualMode.DAY_WET, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `premiumVisualMode updates when the selection changes`() = runTest {
        userSettingsRepository = FakeUserSettingsRepository()
        createViewModel()

        viewModel.premiumVisualMode.test {
            assertEquals(PremiumVisualMode.NIGHT, awaitItem())
            userSettingsRepository.setPremiumVisualMode(PremiumVisualMode.DAY_CLEAR)
            assertEquals(PremiumVisualMode.DAY_CLEAR, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
