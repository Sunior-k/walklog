package com.river.walklog.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.GetPremiumVisualModeUseCase
import com.river.walklog.core.domain.usecase.SetPremiumVisualModeUseCase
import com.river.walklog.core.model.PremiumVisualMode
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
class PremiumThemeSettingsViewModel @Inject constructor(
    private val setPremiumVisualModeUseCase: SetPremiumVisualModeUseCase,
    getPremiumVisualModeUseCase: GetPremiumVisualModeUseCase,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(PremiumThemeSettingsState())
    val state: StateFlow<PremiumThemeSettingsState> = _state.asStateFlow()

    init {
        crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.PREMIUM_THEME_SETTINGS)
        getPremiumVisualModeUseCase()
            .onEach { mode -> _state.update { it.copy(selectedMode = mode, isLoading = false) } }
            .catch { e ->
                crashReporter.log("premiumThemeSettingsViewModel: $e")
                crashReporter.recordException(e)
            }
            .launchIn(viewModelScope)
    }

    fun selectMode(mode: PremiumVisualMode) {
        if (_state.value.selectedMode == mode) return
        _state.update { it.copy(selectedMode = mode) }
        viewModelScope.launch {
            runCatching { setPremiumVisualModeUseCase(mode) }
                .onFailure { e ->
                    crashReporter.log("premiumThemeSettingsViewModel select failed: $e")
                    crashReporter.recordException(e)
                }
        }
    }
}
