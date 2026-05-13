package com.river.walklog.core.testing.repository

import com.river.walklog.core.data.repository.WalkingInsightsRepository
import com.river.walklog.core.model.WalkingInsightsAnalysis

class FakeWalkingInsightsRepository(
    private var analysis: WalkingInsightsAnalysis = defaultWalkingInsightsAnalysis(),
) : WalkingInsightsRepository {

    override fun analyzeHourlySteps(
        hourlySteps: FloatArray,
        targetStepsPerDay: Int,
        currentHour: Int,
    ): WalkingInsightsAnalysis = analysis

    fun setAnalysis(analysis: WalkingInsightsAnalysis) {
        this.analysis = analysis
    }
}

fun defaultWalkingInsightsAnalysis(
    peakHour: Int = 9,
    weeklyTrend: Float = 0f,
    recoveryDifficulty: Float = 0f,
    streakRisk: Float = 0.2f,
) = WalkingInsightsAnalysis(
    peakHour = peakHour,
    weeklyTrend = weeklyTrend,
    recoveryDifficulty = recoveryDifficulty,
    streakRisk = streakRisk,
)
