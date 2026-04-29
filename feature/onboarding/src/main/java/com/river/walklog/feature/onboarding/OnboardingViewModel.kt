package com.river.walklog.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.data.repository.UserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TOTAL_PAGES = 4

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _navigateToHome = MutableStateFlow(false)
    val navigateToHome: StateFlow<Boolean> = _navigateToHome.asStateFlow()

    init {
        crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.ONBOARDING)
    }

    fun handleIntent(intent: OnboardingIntent) {
        when (intent) {
            OnboardingIntent.OnClickNext -> advancePage()
            OnboardingIntent.OnClickBack -> retreatPage()
            is OnboardingIntent.OnNicknameChanged ->
                _state.update { it.copy(nickname = intent.nickname) }
            is OnboardingIntent.OnStepGoalChanged ->
                _state.update { it.copy(dailyStepGoal = intent.steps) }
            is OnboardingIntent.OnNotificationsToggled ->
                _state.update { it.copy(notificationsEnabled = intent.enabled) }
            is OnboardingIntent.OnPermissionResult -> advancePage()
            OnboardingIntent.OnClickComplete -> complete()
        }
    }

    private fun advancePage() {
        val next = _state.value.currentPage + 1
        if (next >= TOTAL_PAGES) complete() else _state.update { it.copy(currentPage = next) }
    }

    private fun retreatPage() {
        val prev = _state.value.currentPage - 1
        if (prev >= 0) _state.update { it.copy(currentPage = prev) }
    }

    private fun complete() {
        val current = _state.value
        _state.update { it.copy(isCompleting = true) }
        viewModelScope.launch {
            runCatching {
                userSettingsRepository.setNickname(current.nickname.trim())
                userSettingsRepository.setDailyStepGoal(current.dailyStepGoal)
                userSettingsRepository.setNotificationsEnabled(current.notificationsEnabled)
                userSettingsRepository.setOnboardingCompleted()
            }.onFailure { e ->
                crashReporter.recordException(e)
            }
            _navigateToHome.value = true
        }
    }
}
