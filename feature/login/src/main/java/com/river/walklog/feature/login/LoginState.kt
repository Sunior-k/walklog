package com.river.walklog.feature.login

import androidx.compose.runtime.Immutable

@Immutable
data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val navigationDestination: LoginNavigationDestination? = null,
)

enum class LoginNavigationDestination {
    Onboarding,
    Home,
}
