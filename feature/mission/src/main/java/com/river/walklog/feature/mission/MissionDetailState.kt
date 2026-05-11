package com.river.walklog.feature.mission

import androidx.compose.runtime.Immutable
import com.river.walklog.core.model.MissionType
import com.river.walklog.core.model.WeatherSummary

@Immutable
data class MissionDetailState(
    val currentSteps: Int = 0,
    val targetSteps: Int = 6_000,
    val rewardPoints: Int = 20,
    val missionType: MissionType = MissionType.DAILY,
    val isCompleted: Boolean = false,
    val recommendedPeakHour: Int? = null,
    val weather: WeatherSummary = WeatherSummary.unavailable(),
)
