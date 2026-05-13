package com.river.walklog.core.model

data class WalkingInsights(
    val peakHour: Int,
    val streakRisk: Float,
    val averageStepsAtPeakHour: Int?,
    val hourlyAverages: List<Float>,
    val activeDays: Int,
    val totalDays: Int,
)
