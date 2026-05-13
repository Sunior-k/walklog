package com.river.walklog.core.data.repository

import com.river.walklog.core.model.WalkingInsightsAnalysis

interface WalkingInsightsRepository {
    fun analyzeHourlySteps(
        hourlySteps: FloatArray,
        targetStepsPerDay: Int,
        currentHour: Int,
    ): WalkingInsightsAnalysis
}
