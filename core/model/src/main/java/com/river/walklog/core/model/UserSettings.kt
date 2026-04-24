package com.river.walklog.core.model

data class UserSettings(
    val isOnboardingCompleted: Boolean = false,
    val dailyStepGoal: Int = 10_000,
    val notificationsEnabled: Boolean = true,
    val recoveryMissionSteps: Int = 6_000,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)
