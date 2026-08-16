package com.river.walklog.core.domain.usecase

import com.river.walklog.core.model.MissionType
import com.river.walklog.core.testing.repository.FakePointsLedgerRepository
import com.river.walklog.core.testing.repository.FakeUserSettingsRepository
import com.river.walklog.core.testing.repository.defaultUserSettings
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AwardMissionPointsUseCaseTest {

    private lateinit var userSettingsRepository: FakeUserSettingsRepository
    private lateinit var pointsLedgerRepository: FakePointsLedgerRepository
    private lateinit var useCase: AwardMissionPointsUseCase

    @Before
    fun setUp() {
        userSettingsRepository = FakeUserSettingsRepository()
        pointsLedgerRepository = FakePointsLedgerRepository()
        useCase = AwardMissionPointsUseCase(userSettingsRepository, pointsLedgerRepository)
    }

    @Test
    fun `DAILY - award records points ledger entry`() = runTest {
        stubSettings(lastDailyMissionAwardedDate = "")

        useCase(MissionType.DAILY, 50)

        val entry = pointsLedgerRepository.entries.value.single()
        assertEquals(50, entry.deltaPoints)
        assertEquals(MissionType.DAILY.name, entry.reason)
    }

    @Test
    fun `DAILY - already awarded skips ledger record`() = runTest {
        stubSettings(lastDailyMissionAwardedDate = LocalDate.now().toString())

        useCase(MissionType.DAILY, 20)

        assertTrue(pointsLedgerRepository.entries.value.isEmpty())
    }

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
    fun `DAILY - award adds provided points to balance`() = runTest {
        stubSettings(lastDailyMissionAwardedDate = "")

        useCase(MissionType.DAILY, 50)

        assertEquals(50, userSettingsRepository.settings.value.totalPoints)
    }

    @Test
    fun `DAILY - award saves today as last awarded date`() = runTest {
        stubSettings(lastDailyMissionAwardedDate = "")
        val today = LocalDate.now().toString()

        useCase(MissionType.DAILY, 20)

        assertEquals(today, userSettingsRepository.settings.value.lastDailyMissionAwardedDate)
    }

    @Test
    fun `DAILY - already awarded skips addPoints`() = runTest {
        stubSettings(lastDailyMissionAwardedDate = LocalDate.now().toString())

        useCase(MissionType.DAILY, 20)

        assertEquals(0, userSettingsRepository.settings.value.totalPoints)
    }

    @Test
    fun `DAILY - concurrent calls only award once`() = runTest {
        stubSettings(lastDailyMissionAwardedDate = "")

        val first = useCase(MissionType.DAILY, 20)
        val second = useCase(MissionType.DAILY, 20)

        assertTrue(first)
        assertFalse(second)
        assertEquals(20, userSettingsRepository.settings.value.totalPoints)
    }

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
    fun `RECOVERY - award adds provided points to balance`() = runTest {
        stubSettings(lastRecoveryMissionAwardedDate = "")

        useCase(MissionType.RECOVERY, 10)

        assertEquals(10, userSettingsRepository.settings.value.totalPoints)
    }

    @Test
    fun `RECOVERY - award saves today as last awarded date`() = runTest {
        stubSettings(lastRecoveryMissionAwardedDate = "")
        val today = LocalDate.now().toString()

        useCase(MissionType.RECOVERY, 10)

        assertEquals(today, userSettingsRepository.settings.value.lastRecoveryMissionAwardedDate)
    }

    @Test
    fun `RECOVERY - already awarded does not affect DAILY award date`() = runTest {
        stubSettings(lastRecoveryMissionAwardedDate = LocalDate.now().toString())

        useCase(MissionType.RECOVERY, 10)

        assertEquals("", userSettingsRepository.settings.value.lastDailyMissionAwardedDate)
    }

    private fun stubSettings(
        lastDailyMissionAwardedDate: String = "",
        lastRecoveryMissionAwardedDate: String = "",
    ) {
        userSettingsRepository.setSettings(
            defaultUserSettings(
                lastDailyMissionAwardedDate = lastDailyMissionAwardedDate,
                lastRecoveryMissionAwardedDate = lastRecoveryMissionAwardedDate,
            ),
        )
    }
}
