package com.river.walklog.feature.settings

import androidx.compose.runtime.Immutable
import com.river.walklog.core.model.PremiumVisualMode

@Immutable
data class PremiumThemeSettingsState(
    val selectedMode: PremiumVisualMode = PremiumVisualMode.NIGHT,
    val isLoading: Boolean = true,
)
