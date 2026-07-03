package com.river.walklog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.auth.AuthRepository
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.model.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userSettingsRepository: UserSettingsRepository,
    authRepository: AuthRepository,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    val uiState: StateFlow<MainActivityUiState> =
        userSettingsRepository.settings
            .map<_, MainActivityUiState>(MainActivityUiState::Success)
            .stateIn(
                scope = viewModelScope,
                initialValue = MainActivityUiState.Loading,
                started = SharingStarted.WhileSubscribed(5_000),
            )

    private val _signOutEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val signOutEvent: SharedFlow<Unit> = _signOutEvent.asSharedFlow()

    init {
        // 앱 실행 중 Firebase Auth 세션이 만료되거나 로그아웃되면 로그인 화면으로 이동
        authRepository.currentUser
            .drop(1) // 초기 값(앱 시작 시 인증 상태)은 무시 — MainActivity가 직접 처리
            .onEach { user ->
                if (user == null) {
                    _signOutEvent.tryEmit(Unit)
                }
            }
            .catch { e -> crashReporter.log("mainActivityViewModel: $e"); crashReporter.recordException(e) }
            .launchIn(viewModelScope)
    }
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState

    data class Success(
        val userSettings: UserSettings,
    ) : MainActivityUiState
}

fun MainActivityUiState.shouldKeepSplashScreen(): Boolean =
    this is MainActivityUiState.Loading
