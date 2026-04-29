package com.river.walklog.feature.onboarding

import androidx.compose.runtime.Immutable

@Immutable
data class OnboardingState(
    val currentPage: Int = 0,
    val nickname: String = "",
    val dailyStepGoal: Int = 10_000,
    val notificationsEnabled: Boolean = true,
    val isCompleting: Boolean = false,
)

sealed interface OnboardingIntent {
    data object OnClickNext : OnboardingIntent
    data object OnClickBack : OnboardingIntent
    data class OnNicknameChanged(val nickname: String) : OnboardingIntent
    data class OnStepGoalChanged(val steps: Int) : OnboardingIntent
    data class OnNotificationsToggled(val enabled: Boolean) : OnboardingIntent
    data class OnPermissionResult(val granted: Boolean) : OnboardingIntent
    data object OnClickComplete : OnboardingIntent
}
