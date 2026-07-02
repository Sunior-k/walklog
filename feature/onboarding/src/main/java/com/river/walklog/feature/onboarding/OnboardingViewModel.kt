package com.river.walklog.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.domain.usecase.SignInWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TOTAL_PAGES = 5

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val signInWithGoogle: SignInWithGoogleUseCase,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    init {
        crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.ONBOARDING)
    }

    fun advancePage() {
        val next = _state.value.currentPage + 1
        if (next >= TOTAL_PAGES) complete() else _state.update { it.copy(currentPage = next) }
    }

    fun retreatPage() {
        val prev = _state.value.currentPage - 1
        if (prev >= 0) _state.update { it.copy(currentPage = prev) }
    }

    fun updateNickname(nickname: String) {
        _state.update { it.copy(nickname = nickname) }
    }

    fun updateStepGoal(steps: Int) {
        _state.update { it.copy(dailyStepGoal = steps) }
    }

    fun updateNotifications(enabled: Boolean) {
        _state.update { it.copy(notificationsEnabled = enabled) }
    }

    fun clearNavigationDestination() {
        _state.update { it.copy(navigationDestination = null) }
    }

    fun onGoogleIdTokenReceived(idToken: String) {
        if (_state.value.isSigningIn) return
        _state.update { it.copy(isSigningIn = true) }

        viewModelScope.launch {
            signInWithGoogle(idToken)
                .onSuccess { user ->
                    if (user.isNewUser) {
                        _state.update { it.copy(isSigningIn = false, isSignedIn = true) }
                        advancePage()
                    } else {
                        // 기존 계정: Firestore에서 데이터 복원 후 홈으로 바로 이동
                        userSettingsRepository.setOnboardingCompleted()
                        userSettingsRepository.sync()
                        _state.update {
                            it.copy(
                                isSigningIn = false,
                                navigationDestination = OnboardingNavigationDestination.Home,
                            )
                        }
                    }
                }
                .onFailure { e ->
                    crashReporter.log("Onboarding Google sign-in failed: ${e.message}")
                    crashReporter.recordException(e)
                    _state.update { it.copy(isSigningIn = false) }
                }
        }
    }

    /** Page 0: 건너뛰기 버튼 클릭 → 확인 다이얼로그 표시 */
    fun requestSkipSignIn() {
        _state.update { it.copy(showSkipConfirmDialog = true) }
    }

    /** 다이얼로그에서 '건너뛰기' 확인 */
    fun confirmSkipSignIn() {
        _state.update { it.copy(showSkipConfirmDialog = false) }
        advancePage()
    }

    /** 다이얼로그 취소 */
    fun dismissSkipDialog() {
        _state.update { it.copy(showSkipConfirmDialog = false) }
    }

    fun complete() {
        val current = _state.value
        if (current.isCompleting) return

        _state.update { it.copy(isCompleting = true) }
        viewModelScope.launch {
            runCatching {
                userSettingsRepository.setNickname(current.nickname.trim())
                userSettingsRepository.setDailyStepGoal(current.dailyStepGoal)
                userSettingsRepository.setNotificationsEnabled(current.notificationsEnabled)
                userSettingsRepository.setOnboardingCompleted()
            }.onFailure { e ->
                crashReporter.log("Onboarding save failed: ${e.message}")
                crashReporter.recordException(e)
            }
            _state.update {
                it.copy(navigationDestination = OnboardingNavigationDestination.Home)
            }
        }
    }
}
