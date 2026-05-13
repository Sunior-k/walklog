package com.river.walklog.core.model

data class DailyStepCount(
    val dateEpochDay: Long,
    val steps: Int,
    val targetSteps: Int = DEFAULT_TARGET_STEPS,
) {
    companion object {
        const val DEFAULT_TARGET_STEPS = 6_000
    }
}
