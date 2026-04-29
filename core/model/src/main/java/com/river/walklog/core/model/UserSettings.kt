package com.river.walklog.core.model

data class UserSettings(
    val isOnboardingCompleted: Boolean = false,
    val nickname: String = "",
    val totalPoints: Int = 0,
    val dailyStepGoal: Int = 10_000,
    val notificationsEnabled: Boolean = true,
    val recoveryMissionSteps: Int = 6_000,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val lastDailyMissionAwardedDate: String = "",
    val lastRecoveryMissionAwardedDate: String = "",
)
