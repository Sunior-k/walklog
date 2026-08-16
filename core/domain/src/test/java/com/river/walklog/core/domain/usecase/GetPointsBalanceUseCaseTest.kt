package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.model.ThemeMode
import com.river.walklog.core.model.UserSettings
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetPointsBalanceUseCaseTest {

    private lateinit var userSettingsRepository: UserSettingsRepository
    private lateinit var useCase: GetPointsBalanceUseCase

    @Before
    fun setUp() {
        userSettingsRepository = mockk()
        useCase = GetPointsBalanceUseCase(userSettingsRepository)
    }

    @Test
    fun `returns total points from settings`() = runTest {
        every { userSettingsRepository.settings } returns flowOf(stubSettings(totalPoints = 1200))

        assertEquals(1200, useCase().first())
    }

    private fun stubSettings(totalPoints: Int) = UserSettings(
        isOnboardingCompleted = false,
        nickname = "",
        totalPoints = totalPoints,
        dailyStepGoal = 10_000,
        notificationsEnabled = true,
        recoveryMissionSteps = 6_000,
        themeMode = ThemeMode.SYSTEM,
        lastDailyMissionAwardedDate = "",
        lastRecoveryMissionAwardedDate = "",
        userId = "",
    )
}
