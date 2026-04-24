package com.river.walklog.core.model

data class Mission(
    val id: Long,
    val type: MissionType,
    val title: String,
    val description: String,
    val targetSteps: Int,
    val currentSteps: Int,
    val rewardCash: Int,
    val isCompleted: Boolean,
    val recommendedHour: Int,
) {
    val progressRatio: Float
        get() = if (targetSteps <= 0) {
            0f
        } else {
            (currentSteps.toFloat() / targetSteps).coerceIn(0f, 1f)
        }
}

enum class MissionType { DAILY, RECOVERY }
