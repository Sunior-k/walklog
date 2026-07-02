package com.river.walklog.feature.onboarding

import androidx.compose.runtime.Immutable

@Immutable
data class OnboardingState(
    val currentPage: Int = 0,
    val isSignedIn: Boolean = false,
    val isSigningIn: Boolean = false,
    val nickname: String = "",
    val dailyStepGoal: Int = 10_000,
    val notificationsEnabled: Boolean = true,
    val isCompleting: Boolean = false,
    val showSkipConfirmDialog: Boolean = false,
    val navigationDestination: OnboardingNavigationDestination? = null,
)

enum class OnboardingNavigationDestination {
    Home,
}
