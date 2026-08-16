package com.river.walklog.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.GetActiveThemeUseCase
import com.river.walklog.core.domain.usecase.GetPremiumVisualModeUseCase
import com.river.walklog.core.model.PremiumVisualMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * 리워드 스토어 "테마 팩" 교환 시 앱 전체에 프리미엄 테마를 적용하기 위한 공용 ViewModel.
 * 각 화면의 Fragment가 개별적으로 hiltViewModel()로 가져와 WalkLogTheme(isPremiumTheme = ...)에 연결한다.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    getActiveThemeUseCase: GetActiveThemeUseCase,
    getPremiumVisualModeUseCase: GetPremiumVisualModeUseCase,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    // 여러 화면에서 공유되는 유틸리티 ViewModel이라 SCREEN crash key를 여기서 설정하지 않는다 —
    // 화면 자체 ViewModel이 이미 자신의 CrashKeys.Screens.XXX를 설정하므로, 여기서 덮어쓰면
    // 화면 전환 시점에 크래시가 나면 잘못된 화면으로 오귀속될 수 있다.
    val isPremiumTheme: StateFlow<Boolean> = getActiveThemeUseCase()
        .catch { e -> crashReporter.log("themeViewModel: $e"); crashReporter.recordException(e) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val premiumVisualMode: StateFlow<PremiumVisualMode> = getPremiumVisualModeUseCase()
        .catch { e -> crashReporter.log("themeViewModel: $e"); crashReporter.recordException(e) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PremiumVisualMode.NIGHT)
}
