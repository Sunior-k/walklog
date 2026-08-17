package com.river.walklog

import app.cash.turbine.test
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.GetPremiumVisualModeUseCase
import com.river.walklog.core.testing.MainDispatcherRule
import com.river.walklog.core.testing.repository.FakeAuthRepository
import com.river.walklog.core.testing.repository.FakeUserSettingsRepository
import com.river.walklog.core.testing.repository.defaultAuthUser
import com.river.walklog.core.testing.repository.defaultUserSettings
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MainActivityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var userSettingsRepository: FakeUserSettingsRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var crashReporter: CrashReporter
    private lateinit var getPremiumVisualModeUseCase: GetPremiumVisualModeUseCase
    private lateinit var viewModel: MainActivityViewModel

    @Before
    fun setUp() {
        userSettingsRepository = FakeUserSettingsRepository()
        authRepository = FakeAuthRepository()
        crashReporter = mockk(relaxed = true)
        getPremiumVisualModeUseCase = mockk(relaxed = true)
        every { getPremiumVisualModeUseCase() } returns flowOf(com.river.walklog.core.model.PremiumVisualMode.NIGHT)
        viewModel = MainActivityViewModel(
            userSettingsRepository,
            authRepository,
            crashReporter,
            getPremiumVisualModeUseCase,
        )
    }

    // uiState

    @Test
    fun `uiState is Loading before first subscriber`() {
        assertEquals(MainActivityUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `uiState becomes Success after settings emit`() = runTest {
        viewModel.uiState.test {
            assertIs<MainActivityUiState.Success>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState Success contains correct settings`() = runTest {
        val settings = defaultUserSettings(nickname = "runner", dailyStepGoal = 10_000)
        userSettingsRepository.setSettings(settings)
        viewModel.uiState.test {
            val item = awaitItem()
            assertIs<MainActivityUiState.Success>(item)
            assertEquals(settings, item.userSettings)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState emits new Success when settings change`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial emission
            userSettingsRepository.setSettings(defaultUserSettings(dailyStepGoal = 12_000))
            val updated = awaitItem()
            assertIs<MainActivityUiState.Success>(updated)
            assertEquals(12_000, updated.userSettings.dailyStepGoal)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // signOutEvent

    @Test
    fun `signOutEvent does not emit for the initial null currentUser`() = runTest {
        viewModel.signOutEvent.test {
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `signOutEvent emits when authenticated user signs out`() = runTest {
        viewModel.signOutEvent.test {
            authRepository.setCurrentUser(defaultAuthUser()) // sign in
            authRepository.setCurrentUser(null)              // sign out
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `signOutEvent does not emit when user signs in`() = runTest {
        viewModel.signOutEvent.test {
            authRepository.setCurrentUser(defaultAuthUser())
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `signOutEvent emits only once per sign-out`() = runTest {
        viewModel.signOutEvent.test {
            authRepository.setCurrentUser(defaultAuthUser())
            authRepository.setCurrentUser(null)
            awaitItem() // single signOut event
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // shouldKeepSplashScreen

    @Test
    fun `shouldKeepSplashScreen is true while Loading`() {
        assertTrue(viewModel.uiState.value.shouldKeepSplashScreen())
    }

    @Test
    fun `shouldKeepSplashScreen is false when Success`() = runTest {
        viewModel.uiState.test {
            val item = awaitItem()
            assertIs<MainActivityUiState.Success>(item)
            assertTrue(!item.shouldKeepSplashScreen())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `premiumNavMode is null when premium theme is inactive`() = runTest {
        viewModel.premiumNavMode.test {
            assertEquals(null, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `premiumNavMode resolves a mode when premium theme becomes active`() = runTest {
        viewModel.premiumNavMode.test {
            assertEquals(null, awaitItem())
            userSettingsRepository.setSettings(defaultUserSettings(isPremiumThemeActive = true))
            assertEquals(com.river.walklog.core.model.PremiumVisualMode.NIGHT, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `premiumNavMode returns to null when premium theme is deactivated`() = runTest {
        viewModel.premiumNavMode.test {
            awaitItem() // initial null
            userSettingsRepository.setSettings(defaultUserSettings(isPremiumThemeActive = true))
            awaitItem() // resolved mode
            userSettingsRepository.setSettings(defaultUserSettings(isPremiumThemeActive = false))
            assertEquals(null, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
