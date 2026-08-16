package com.river.walklog.core.domain.usecase

import app.cash.turbine.test
import com.river.walklog.core.model.PremiumVisualMode
import com.river.walklog.core.testing.repository.FakeUserSettingsRepository
import com.river.walklog.core.testing.repository.defaultUserSettings
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class GetPremiumVisualModeUseCaseTest {

    @Test
    fun `emits the persisted mode from settings`() = runTest {
        val repository = FakeUserSettingsRepository(
            initialSettings = defaultUserSettings(premiumVisualMode = PremiumVisualMode.DAY_WET),
        )
        val useCase = GetPremiumVisualModeUseCase(repository)

        useCase().test {
            assertEquals(PremiumVisualMode.DAY_WET, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits a new value when the mode is updated`() = runTest {
        val repository = FakeUserSettingsRepository()
        val useCase = GetPremiumVisualModeUseCase(repository)

        useCase().test {
            assertEquals(PremiumVisualMode.NIGHT, awaitItem())
            repository.setPremiumVisualMode(PremiumVisualMode.DAY_CLEAR)
            assertEquals(PremiumVisualMode.DAY_CLEAR, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
