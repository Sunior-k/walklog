package com.river.walklog.core.data.repository

import com.river.walklog.core.model.ThemeMode
import com.river.walklog.core.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    val settings: Flow<UserSettings>
    suspend fun setOnboardingCompleted()
    suspend fun setDailyStepGoal(steps: Int)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setRecoveryMissionSteps(steps: Int)
    suspend fun setThemeMode(themeMode: ThemeMode)
}
