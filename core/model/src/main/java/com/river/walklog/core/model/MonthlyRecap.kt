package com.river.walklog.core.model

data class MonthlyRecap(
    val year: Int,
    val month: Int,
    val totalSteps: Int,
    val averageStepsPerDay: Int,
    val bestDay: DailyStepCount?,
    val achievedDays: Int,
    val totalDays: Int,
    val longestStreak: Int,
    val activeDays: Int,
    val estimatedCalories: Int,
    val dailyCounts: List<DailyStepCount>,
)
