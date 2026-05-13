package com.river.walklog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.model.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userSettingsRepository: UserSettingsRepository,
) : ViewModel() {
    val uiState: StateFlow<MainActivityUiState> =
        userSettingsRepository.settings
            .map<_, MainActivityUiState>(MainActivityUiState::Success)
            .stateIn(
                scope = viewModelScope,
                initialValue = MainActivityUiState.Loading,
                started = SharingStarted.WhileSubscribed(5_000),
            )
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState

    data class Success(
        val userSettings: UserSettings,
    ) : MainActivityUiState
}

fun MainActivityUiState.shouldKeepSplashScreen(): Boolean =
    this is MainActivityUiState.Loading
