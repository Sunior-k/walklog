package com.river.walklog.feature.onboarding

import androidx.compose.runtime.Immutable

@Immutable
data class OnboardingState(
    val currentPage: Int = 0,
    val nickname: String = "",
    val dailyStepGoal: Int = 10_000,
    val notificationsEnabled: Boolean = true,
    val isCompleting: Boolean = false,
    val navigationDestination: OnboardingNavigationDestination? = null,
)

enum class OnboardingNavigationDestination {
    Home,
}
