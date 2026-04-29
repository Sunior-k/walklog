package com.river.walklog.feature.settings

import androidx.compose.runtime.Immutable
import com.river.walklog.core.model.ThemeMode

@Immutable
data class SettingsState(
    val nickname: String = "",
    val totalPoints: Int = 0,
    val dailyStepGoal: Int = 10_000,
    val notificationsEnabled: Boolean = true,
    val recoveryMissionSteps: Int = 6_000,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isLoading: Boolean = true,
)

sealed interface SettingsIntent {
    data class OnNicknameChanged(val nickname: String) : SettingsIntent
    data class OnStepGoalChanged(val steps: Int) : SettingsIntent
    data class OnNotificationsToggled(val enabled: Boolean) : SettingsIntent
    data class OnRecoveryStepsChanged(val steps: Int) : SettingsIntent
    data class OnThemeModeChanged(val themeMode: ThemeMode) : SettingsIntent
}
