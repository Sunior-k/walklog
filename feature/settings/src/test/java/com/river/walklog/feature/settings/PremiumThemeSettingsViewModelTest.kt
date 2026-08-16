package com.river.walklog.feature.settings

import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.GetPremiumVisualModeUseCase
import com.river.walklog.core.domain.usecase.SetPremiumVisualModeUseCase
import com.river.walklog.core.model.PremiumVisualMode
import com.river.walklog.core.testing.MainDispatcherRule
import com.river.walklog.core.testing.repository.FakeUserSettingsRepository
import com.river.walklog.core.testing.repository.defaultUserSettings
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class PremiumThemeSettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeUserSettingsRepository
    private lateinit var crashReporter: CrashReporter
    private lateinit var viewModel: PremiumThemeSettingsViewModel

    private fun createViewModel() {
        crashReporter = mockk(relaxed = true)
        viewModel = PremiumThemeSettingsViewModel(
            SetPremiumVisualModeUseCase(repository),
            GetPremiumVisualModeUseCase(repository),
            crashReporter,
        )
    }

    @Before
    fun setUp() {
        repository = FakeUserSettingsRepository()
    }

    @Test
    fun `init sets PREMIUM_THEME_SETTINGS crash key`() {
        createViewModel()

        verify { crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.PREMIUM_THEME_SETTINGS) }
    }

    @Test
    fun `state reflects the persisted selection`() = runTest {
        repository.setSettings(defaultUserSettings(premiumVisualMode = PremiumVisualMode.DAY_WET))
        createViewModel()

        assertEquals(PremiumVisualMode.DAY_WET, viewModel.state.value.selectedMode)
    }

    @Test
    fun `selectMode updates state immediately and persists the change`() = runTest {
        createViewModel()

        viewModel.selectMode(PremiumVisualMode.DAY_CLEAR)

        assertEquals(PremiumVisualMode.DAY_CLEAR, viewModel.state.value.selectedMode)
        assertEquals(PremiumVisualMode.DAY_CLEAR, repository.settings.value.premiumVisualMode)
    }
}
