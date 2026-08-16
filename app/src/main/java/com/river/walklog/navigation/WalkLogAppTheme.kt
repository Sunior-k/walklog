package com.river.walklog.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.core.ui.ThemeViewModel

/**
 * [WalkLogTheme]에 프리미엄 테마 상태([ThemeViewModel])를 연결하는 공용 래퍼.
 * `core:designsystem`은 Hilt/ViewModel에 의존할 수 없어(모듈 역방향 금지) 이 결합은
 * `:app`에 있어야 한다 — 모든 `:app` Fragment가 각자 이 3줄을 반복하지 않도록 여기 하나로 모음.
 */
@Composable
fun WalkLogAppTheme(content: @Composable () -> Unit) {
    val themeViewModel = hiltViewModel<ThemeViewModel>()
    val isPremiumTheme by themeViewModel.isPremiumTheme.collectAsStateWithLifecycle()
    val premiumVisualMode by themeViewModel.premiumVisualMode.collectAsStateWithLifecycle()
    WalkLogTheme(isPremiumTheme = isPremiumTheme, premiumVisualMode = premiumVisualMode, content = content)
}
