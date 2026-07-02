package com.river.walklog.core.model

data class UserSettings(
    val isOnboardingCompleted: Boolean,
    val nickname: String,
    val totalPoints: Int,
    val dailyStepGoal: Int,
    val notificationsEnabled: Boolean,
    val recoveryMissionSteps: Int,
    val themeMode: ThemeMode,
    val lastDailyMissionAwardedDate: String,
    val lastRecoveryMissionAwardedDate: String,
    val userId: String,
)
