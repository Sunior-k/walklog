package com.river.walklog.core.data.repository

import app.cash.turbine.test
import com.river.walklog.core.common.dispatcher.WalkLogDispatchers
import com.river.walklog.core.data.datasource.FirestoreUserSettings
import com.river.walklog.core.data.datasource.FirestoreUserSettingsDataSource
import com.river.walklog.core.datastore.UserPreferencesDataSource
import com.river.walklog.core.model.PremiumVisualMode
import com.river.walklog.core.model.ThemeMode
import com.river.walklog.core.model.UserSettings
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class DataStoreUserSettingsRepositoryTest {

    private lateinit var dataSource: UserPreferencesDataSource
    private lateinit var firestoreDataSource: FirestoreUserSettingsDataSource
    private lateinit var dispatchers: WalkLogDispatchers
    private lateinit var repository: DataStoreUserSettingsRepository

    @Before
    fun setUp() {
        dataSource = mockk()
        firestoreDataSource = mockk()
        dispatchers = WalkLogDispatchers(
            main = Dispatchers.Unconfined,
            io = Dispatchers.Unconfined,
            default = Dispatchers.Unconfined,
        )
        every { dataSource.settings } returns flowOf(defaultUserSettings())
        repository = DataStoreUserSettingsRepository(dataSource, firestoreDataSource, dispatchers)
    }

    // settings

    @Test
    fun `settings flow emits from dataSource`() = runTest {
        val expected = UserSettings(
            isOnboardingCompleted = false,
            nickname = "테스터",
            totalPoints = 100,
            dailyStepGoal = 10_000,
            notificationsEnabled = true,
            recoveryMissionSteps = 6_000,
            themeMode = ThemeMode.SYSTEM,
            lastDailyMissionAwardedDate = "",
            lastRecoveryMissionAwardedDate = "",
            userId = "",
        )
        every { dataSource.settings } returns flowOf(expected)
        val repo = DataStoreUserSettingsRepository(dataSource, firestoreDataSource, dispatchers)

        repo.settings.test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    // delegation

    @Test
    fun `setOnboardingCompleted delegates to dataSource`() = runTest {
        coJustRun { dataSource.setOnboardingCompleted() }

        repository.setOnboardingCompleted()

        coVerify(exactly = 1) { dataSource.setOnboardingCompleted() }
    }

    @Test
    fun `setDailyStepGoal delegates to dataSource with same value`() = runTest {
        coJustRun { dataSource.setDailyStepGoal(8_000) }

        repository.setDailyStepGoal(8_000)

        coVerify(exactly = 1) { dataSource.setDailyStepGoal(8_000) }
    }

    @Test
    fun `setNotificationsEnabled delegates to dataSource`() = runTest {
        coJustRun { dataSource.setNotificationsEnabled(false) }

        repository.setNotificationsEnabled(false)

        coVerify(exactly = 1) { dataSource.setNotificationsEnabled(false) }
    }

    @Test
    fun `setRecoveryMissionSteps delegates to dataSource with same value`() = runTest {
        coJustRun { dataSource.setRecoveryMissionSteps(5_000) }

        repository.setRecoveryMissionSteps(5_000)

        coVerify(exactly = 1) { dataSource.setRecoveryMissionSteps(5_000) }
    }

    @Test
    fun `setNickname delegates to dataSource with same value`() = runTest {
        coJustRun { dataSource.setNickname("리버") }

        repository.setNickname("리버")

        coVerify(exactly = 1) { dataSource.setNickname("리버") }
    }

    @Test
    fun `addPoints delegates to dataSource with same delta`() = runTest {
        coJustRun { dataSource.addPoints(50) }

        repository.addPoints(50)

        coVerify(exactly = 1) { dataSource.addPoints(50) }
    }

    @Test
    fun `setThemeMode delegates to dataSource with same value`() = runTest {
        coJustRun { dataSource.setThemeMode(ThemeMode.DARK) }

        repository.setThemeMode(ThemeMode.DARK)

        coVerify(exactly = 1) { dataSource.setThemeMode(ThemeMode.DARK) }
    }

    @Test
    fun `setLastDailyMissionAwardedDate delegates to dataSource with same date`() = runTest {
        coJustRun { dataSource.setLastDailyMissionAwardedDate("2026-05-13") }

        repository.setLastDailyMissionAwardedDate("2026-05-13")

        coVerify(exactly = 1) { dataSource.setLastDailyMissionAwardedDate("2026-05-13") }
    }

    @Test
    fun `setLastRecoveryMissionAwardedDate delegates to dataSource with same date`() = runTest {
        coJustRun { dataSource.setLastRecoveryMissionAwardedDate("2026-05-13") }

        repository.setLastRecoveryMissionAwardedDate("2026-05-13")

        coVerify(exactly = 1) { dataSource.setLastRecoveryMissionAwardedDate("2026-05-13") }
    }

    // mergeUserSettings

    @Test
    fun `mergeUserSettings activates premium theme when only remote has it active`() {
        val local = defaultUserSettings().copy(isPremiumThemeActive = false)
        val remote = FirestoreUserSettings().apply { premiumThemeActive = true }

        val merged = mergeUserSettings(local, remote)

        assertEquals(true, merged.isPremiumThemeActive)
    }

    @Test
    fun `mergeUserSettings keeps premium theme active when only local has it active`() {
        val local = defaultUserSettings().copy(isPremiumThemeActive = true)
        val remote = FirestoreUserSettings().apply { premiumThemeActive = false }

        val merged = mergeUserSettings(local, remote)

        assertEquals(true, merged.isPremiumThemeActive)
    }

    @Test
    fun `mergeUserSettings uses remote premiumVisualMode when not blank`() {
        val local = defaultUserSettings().copy(premiumVisualMode = PremiumVisualMode.NIGHT)
        val remote = FirestoreUserSettings().apply { premiumVisualMode = "DAY_CLEAR" }

        val merged = mergeUserSettings(local, remote)

        assertEquals(PremiumVisualMode.DAY_CLEAR, merged.premiumVisualMode)
    }

    @Test
    fun `mergeUserSettings keeps local premiumVisualMode when remote is blank`() {
        val local = defaultUserSettings().copy(premiumVisualMode = PremiumVisualMode.DAY_WET)
        val remote = FirestoreUserSettings().apply { premiumVisualMode = "" }

        val merged = mergeUserSettings(local, remote)

        assertEquals(PremiumVisualMode.DAY_WET, merged.premiumVisualMode)
    }
}

private fun defaultUserSettings() = UserSettings(
    isOnboardingCompleted = false,
    nickname = "",
    totalPoints = 0,
    dailyStepGoal = 10_000,
    notificationsEnabled = true,
    recoveryMissionSteps = 6_000,
    themeMode = ThemeMode.SYSTEM,
    lastDailyMissionAwardedDate = "",
    lastRecoveryMissionAwardedDate = "",
    userId = "",
)
