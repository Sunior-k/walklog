package com.river.walklog.core.testing.repository

import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.model.ThemeMode
import com.river.walklog.core.model.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeUserSettingsRepository(
    initialSettings: UserSettings = defaultUserSettings(),
) : UserSettingsRepository {

    private val _settings = MutableStateFlow(initialSettings)
    override val settings: StateFlow<UserSettings> = _settings

    override suspend fun setOnboardingCompleted() {
        update { copy(isOnboardingCompleted = true) }
    }

    override suspend fun setDailyStepGoal(steps: Int) {
        update { copy(dailyStepGoal = steps) }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        update { copy(notificationsEnabled = enabled) }
    }

    override suspend fun setRecoveryMissionSteps(steps: Int) {
        update { copy(recoveryMissionSteps = steps) }
    }

    override suspend fun setNickname(nickname: String) {
        update { copy(nickname = nickname) }
    }

    override suspend fun addPoints(delta: Int) {
        update { copy(totalPoints = totalPoints + delta) }
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        update { copy(themeMode = themeMode) }
    }

    override suspend fun setLastDailyMissionAwardedDate(date: String) {
        update { copy(lastDailyMissionAwardedDate = date) }
    }

    override suspend fun setLastRecoveryMissionAwardedDate(date: String) {
        update { copy(lastRecoveryMissionAwardedDate = date) }
    }

    override suspend fun setUserId(uid: String) {
        update { copy(userId = uid) }
    }

    override suspend fun sync(): Boolean = true

    fun setSettings(settings: UserSettings) {
        _settings.value = settings
    }

    private fun update(transform: UserSettings.() -> UserSettings) {
        _settings.value = _settings.value.transform()
    }
}

fun defaultUserSettings(
    isOnboardingCompleted: Boolean = true,
    nickname: String = "",
    totalPoints: Int = 0,
    dailyStepGoal: Int = 6_000,
    notificationsEnabled: Boolean = true,
    recoveryMissionSteps: Int = 6_000,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    lastDailyMissionAwardedDate: String = "",
    lastRecoveryMissionAwardedDate: String = "",
    userId: String = "test-user-id",
) = UserSettings(
    isOnboardingCompleted = isOnboardingCompleted,
    nickname = nickname,
    totalPoints = totalPoints,
    dailyStepGoal = dailyStepGoal,
    notificationsEnabled = notificationsEnabled,
    recoveryMissionSteps = recoveryMissionSteps,
    themeMode = themeMode,
    lastDailyMissionAwardedDate = lastDailyMissionAwardedDate,
    lastRecoveryMissionAwardedDate = lastRecoveryMissionAwardedDate,
    userId = userId,
)
