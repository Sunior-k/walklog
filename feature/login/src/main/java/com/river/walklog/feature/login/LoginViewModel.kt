package com.river.walklog.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.domain.usecase.SignInWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInWithGoogle: SignInWithGoogleUseCase,
    private val userSettingsRepository: UserSettingsRepository,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onGoogleIdTokenReceived(idToken: String) {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            signInWithGoogle(idToken)
                .onSuccess {
                    val isOnboardingCompleted =
                        userSettingsRepository.settings.first().isOnboardingCompleted
                    val destination = if (isOnboardingCompleted) {
                        LoginNavigationDestination.Home
                    } else {
                        LoginNavigationDestination.Onboarding
                    }
                    _state.update { it.copy(isLoading = false, navigationDestination = destination) }
                }
                .onFailure { e ->
                    crashReporter.recordException(e)
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun clearNavigationDestination() {
        _state.update { it.copy(navigationDestination = null) }
    }

    fun onSignInFailed() {
        _state.update { it.copy(isLoading = false, error = "로그인에 실패했습니다. 다시 시도해 주세요.") }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
