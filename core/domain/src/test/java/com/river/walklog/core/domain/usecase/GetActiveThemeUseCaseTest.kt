package com.river.walklog.core.domain.usecase

import com.river.walklog.core.testing.repository.FakeUserSettingsRepository
import com.river.walklog.core.testing.repository.defaultUserSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GetActiveThemeUseCaseTest {

    @Test
    fun `returns false when premium theme not active`() = runTest {
        val repository = FakeUserSettingsRepository(defaultUserSettings(isPremiumThemeActive = false))
        val useCase = GetActiveThemeUseCase(repository)

        assertFalse(useCase().first())
    }

    @Test
    fun `returns true when premium theme active`() = runTest {
        val repository = FakeUserSettingsRepository(defaultUserSettings(isPremiumThemeActive = true))
        val useCase = GetActiveThemeUseCase(repository)

        assertTrue(useCase().first())
    }
}
