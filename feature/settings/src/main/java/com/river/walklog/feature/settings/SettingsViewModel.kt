package com.river.walklog.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.SETTINGS)
        observeSettings()
    }

    fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.OnNicknameChanged -> updateNickname(intent.nickname)
            is SettingsIntent.OnStepGoalChanged -> updateStepGoal(intent.steps)
            is SettingsIntent.OnNotificationsToggled -> updateNotifications(intent.enabled)
            is SettingsIntent.OnRecoveryStepsChanged -> updateRecoverySteps(intent.steps)
            is SettingsIntent.OnThemeModeChanged -> updateThemeMode(intent.themeMode)
        }
    }

    private fun observeSettings() {
        userSettingsRepository.settings
            .onEach { settings ->
                _state.update {
                    it.copy(
                        nickname = settings.nickname,
                        totalPoints = settings.totalPoints,
                        dailyStepGoal = settings.dailyStepGoal,
                        notificationsEnabled = settings.notificationsEnabled,
                        recoveryMissionSteps = settings.recoveryMissionSteps,
                        themeMode = settings.themeMode,
                        isLoading = false,
                    )
                }
            }
            .catch { e -> crashReporter.recordException(e) }
            .launchIn(viewModelScope)
    }

    private fun updateNickname(nickname: String) {
        viewModelScope.launch {
            runCatching { userSettingsRepository.setNickname(nickname.trim()) }
                .onFailure { e -> crashReporter.recordException(e) }
        }
    }

    private fun updateStepGoal(steps: Int) {
        viewModelScope.launch {
            runCatching { userSettingsRepository.setDailyStepGoal(steps) }
                .onFailure { e -> crashReporter.recordException(e) }
        }
    }

    private fun updateNotifications(enabled: Boolean) {
        viewModelScope.launch {
            runCatching { userSettingsRepository.setNotificationsEnabled(enabled) }
                .onFailure { e -> crashReporter.recordException(e) }
        }
    }

    private fun updateRecoverySteps(steps: Int) {
        viewModelScope.launch {
            runCatching { userSettingsRepository.setRecoveryMissionSteps(steps) }
                .onFailure { e -> crashReporter.recordException(e) }
        }
    }

    private fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            runCatching { userSettingsRepository.setThemeMode(themeMode) }
                .onFailure { e -> crashReporter.recordException(e) }
        }
    }
}
