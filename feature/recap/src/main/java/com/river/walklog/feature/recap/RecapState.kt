package com.river.walklog.feature.recap

import androidx.compose.runtime.Immutable
import com.river.walklog.core.model.MonthlyRecap

@Immutable
data class RecapState(
    val isLoading: Boolean = true,
    val recap: MonthlyRecap? = null,
)

sealed interface RecapIntent {
    data object OnClose : RecapIntent
}
