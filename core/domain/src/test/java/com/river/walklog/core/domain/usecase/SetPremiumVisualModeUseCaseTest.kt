package com.river.walklog.core.domain.usecase

import com.river.walklog.core.model.PremiumVisualMode
import com.river.walklog.core.testing.repository.FakeUserSettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class SetPremiumVisualModeUseCaseTest {

    @Test
    fun `persists the selected mode`() = runTest {
        val repository = FakeUserSettingsRepository()
        val useCase = SetPremiumVisualModeUseCase(repository)

        useCase(PremiumVisualMode.DAY_WET)

        assertEquals(PremiumVisualMode.DAY_WET, repository.settings.value.premiumVisualMode)
    }
}
