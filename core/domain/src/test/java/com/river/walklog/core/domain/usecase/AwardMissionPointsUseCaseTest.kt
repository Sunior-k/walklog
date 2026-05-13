package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.model.MissionType
import com.river.walklog.core.model.ThemeMode
import com.river.walklog.core.model.UserSettings
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AwardMissionPointsUseCaseTest {

    private lateinit var userSettingsRepository: UserSettingsRepository
    private lateinit var useCase: AwardMissionPointsUseCase

    @Before
    fun setUp() {
        userSettingsRepository = mockk()
        useCase = AwardMissionPointsUseCase(userSettingsRepository)
        coJustRun { userSettingsRepository.addPoints(any()) }
        coJustRun { userSettingsRepository.setLastDailyMissionAwardedDate(any()) }
        coJustRun { userSettingsRepository.setLastRecoveryMissionAwardedDate(any()) }
    }

    // DAILY

    @Test
    fun `DAILY - not yet awarded today returns true`() = runTest {
        stubSettings(lastDailyMissionAwardedDate = "")

        assertTrue(useCase(MissionType.DAILY, 20))
    }

    @Test
    fun `DAILY - already awarded today returns false`() = runTest {
        stubSettings(lastDailyMissionAwardedDate = LocalDate.now().toString())

        assertFalse(useCase(MissionType.DAILY, 20))
    }

    @Test
    fun `DAILY - awarded on previous day returns true`() = runTest {
        stubSettings(lastDailyMissionAwardedDate = LocalDate.now().minusDays(1).toString())

        assertTrue(useCase(MissionType.DAILY, 20))
    }

    @Test
    fun `DAILY - award calls addPoints with provided points`() = runTest {
        stubSettings(lastDailyMissionAwardedDate = "")

        useCase(MissionType.DAILY, 50)

        coVerify(exactly = 1) { userSettingsRepository.addPoints(50) }
    }

    @Test
    fun `DAILY - award saves today as last awarded date`() = runTest {
        stubSettings(lastDailyMissionAwardedDate = "")
        val today = LocalDate.now().toString()

        useCase(MissionType.DAILY, 20)

        coVerify(exactly = 1) { userSettingsRepository.setLastDailyMissionAwardedDate(today) }
    }

    @Test
    fun `DAILY - already awarded skips addPoints`() = runTest {
        stubSettings(lastDailyMissionAwardedDate = LocalDate.now().toString())

        useCase(MissionType.DAILY, 20)

        coVerify(exactly = 0) { userSettingsRepository.addPoints(any()) }
    }

    @Test
    fun `DAILY - already awarded skips setLastDailyMissionAwardedDate`() = runTest {
        stubSettings(lastDailyMissionAwardedDate = LocalDate.now().toString())

        useCase(MissionType.DAILY, 20)

        coVerify(exactly = 0) { userSettingsRepository.setLastDailyMissionAwardedDate(any()) }
    }

    // RECOVERY

    @Test
    fun `RECOVERY - not yet awarded today returns true`() = runTest {
        stubSettings(lastRecoveryMissionAwardedDate = "")

        assertTrue(useCase(MissionType.RECOVERY, 10))
    }

    @Test
    fun `RECOVERY - already awarded today returns false`() = runTest {
        stubSettings(lastRecoveryMissionAwardedDate = LocalDate.now().toString())

        assertFalse(useCase(MissionType.RECOVERY, 10))
    }

    @Test
    fun `RECOVERY - award calls addPoints with provided points`() = runTest {
        stubSettings(lastRecoveryMissionAwardedDate = "")

        useCase(MissionType.RECOVERY, 10)

        coVerify(exactly = 1) { userSettingsRepository.addPoints(10) }
    }

    @Test
    fun `RECOVERY - award saves today as last awarded date`() = runTest {
        stubSettings(lastRecoveryMissionAwardedDate = "")
        val today = LocalDate.now().toString()

        useCase(MissionType.RECOVERY, 10)

        coVerify(exactly = 1) { userSettingsRepository.setLastRecoveryMissionAwardedDate(today) }
    }

    @Test
    fun `RECOVERY - already awarded does not affect DAILY award date`() = runTest {
        stubSettings(lastRecoveryMissionAwardedDate = LocalDate.now().toString())

        useCase(MissionType.RECOVERY, 10)

        coVerify(exactly = 0) { userSettingsRepository.setLastDailyMissionAwardedDate(any()) }
    }

    // helper

    private fun stubSettings(
        lastDailyMissionAwardedDate: String = "",
        lastRecoveryMissionAwardedDate: String = "",
    ) {
        every { userSettingsRepository.settings } returns flowOf(
            UserSettings(
                isOnboardingCompleted = false,
                nickname = "",
                totalPoints = 0,
                dailyStepGoal = 10_000,
                notificationsEnabled = true,
                recoveryMissionSteps = 6_000,
                themeMode = ThemeMode.SYSTEM,
                lastDailyMissionAwardedDate = lastDailyMissionAwardedDate,
                lastRecoveryMissionAwardedDate = lastRecoveryMissionAwardedDate,
            ),
        )
    }
}
