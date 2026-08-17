package com.river.walklog.feature.reward

import androidx.compose.runtime.Immutable

@Immutable
data class RewardState(
    val navigationDestination: RewardDest? = null,
)

sealed interface RewardDest {
    data object Store : RewardDest
    data object PointsHistory : RewardDest
    data object BadgeCollection : RewardDest
}
