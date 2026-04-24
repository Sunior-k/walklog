package com.river.walklog.feature.home.model

import androidx.compose.runtime.Immutable

@Immutable
data class MissionCardUiModel(
    val title: String,
    val currentSteps: Int,
    val targetSteps: Int,
    val rewardText: String,
    val isRecovery: Boolean = false,
    val isCompleted: Boolean = false,
)
