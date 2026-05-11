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
