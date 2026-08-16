package com.river.walklog.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.auth.AuthRepository
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.domain.usecase.GetRewardRedemptionsUseCase
import com.river.walklog.core.domain.usecase.SignInWithGoogleUseCase
import com.river.walklog.core.domain.usecase.SignOutUseCase
import com.river.walklog.core.model.RewardCatalogIds
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
    private val authRepository: AuthRepository,
    private val signInWithGoogle: SignInWithGoogleUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getRewardRedemptionsUseCase: GetRewardRedemptionsUseCase,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.SETTINGS)
        observeSettings()
        observeAuthState()
        observePremiumThemeOwnership()
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
                        isPremiumThemeActive = settings.isPremiumThemeActive,
                        isLoading = false,
                    )
                }
            }
            .catch { e ->
                crashReporter.log("Settings load failed: ${e.message}")
                crashReporter.recordException(e)
            }
            .launchIn(viewModelScope)
    }

    private fun observePremiumThemeOwnership() {
        getRewardRedemptionsUseCase(RewardCatalogIds.THEME_PACK)
            .onEach { redemptions ->
                _state.update { it.copy(isPremiumThemeOwned = redemptions.isNotEmpty()) }
            }
            .catch { e ->
                crashReporter.log("settingsViewModel: premium theme ownership query failed: $e")
                crashReporter.recordException(e)
            }
            .launchIn(viewModelScope)
    }

    private fun observeAuthState() {
        authRepository.currentUser
            .onEach { user ->
                _state.update {
                    it.copy(
                        isSignedIn = user != null,
                        userEmail = user?.email ?: "",
                    )
                }
            }
            .catch { e -> crashReporter.recordException(e) }
            .launchIn(viewModelScope)
    }

    fun onGoogleIdTokenReceived(idToken: String) {
        viewModelScope.launch {
            signInWithGoogle(idToken)
                .onFailure { e ->
                    crashReporter.log("Settings sign-in failed: ${e.message}")
                    crashReporter.recordException(e)
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            runCatching { signOutUseCase() }
                .onFailure { e ->
                    crashReporter.log("Sign-out failed: ${e.message}")
                    crashReporter.recordException(e)
                }
        }
    }

    fun updateNickname(nickname: String) {
        viewModelScope.launch {
            runCatching { userSettingsRepository.setNickname(nickname.trim()) }
                .onFailure { e ->
                    crashReporter.log("Nickname update failed: ${e.message}")
                    crashReporter.recordException(e)
                }
        }
    }

    fun updateStepGoal(steps: Int) {
        viewModelScope.launch {
            runCatching { userSettingsRepository.setDailyStepGoal(steps) }
                .onFailure { e ->
                    crashReporter.log("Step goal update failed: ${e.message}")
                    crashReporter.recordException(e)
                }
        }
    }

    fun updateNotifications(enabled: Boolean) {
        viewModelScope.launch {
            runCatching { userSettingsRepository.setNotificationsEnabled(enabled) }
                .onFailure { e ->
                    crashReporter.log("Notifications update failed: ${e.message}")
                    crashReporter.recordException(e)
                }
        }
    }

    fun updateRecoverySteps(steps: Int) {
        viewModelScope.launch {
            runCatching { userSettingsRepository.setRecoveryMissionSteps(steps) }
                .onFailure { e ->
                    crashReporter.log("Recovery steps update failed: ${e.message}")
                    crashReporter.recordException(e)
                }
        }
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            runCatching { userSettingsRepository.setThemeMode(themeMode) }
                .onFailure { e ->
                    crashReporter.log("Theme mode update failed: ${e.message}")
                    crashReporter.recordException(e)
                }
        }
    }

    fun togglePremiumTheme(enabled: Boolean) {
        if (!_state.value.isPremiumThemeOwned) return
        viewModelScope.launch {
            runCatching { userSettingsRepository.setActiveThemePack(enabled) }
                .onFailure { e ->
                    crashReporter.log("Premium theme toggle failed: ${e.message}")
                    crashReporter.recordException(e)
                }
        }
    }
}
