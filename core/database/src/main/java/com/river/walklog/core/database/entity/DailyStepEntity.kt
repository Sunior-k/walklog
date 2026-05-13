package com.river.walklog.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.river.walklog.core.model.DailyStepCount

@Entity(tableName = "daily_steps")
data class DailyStepEntity(
    @PrimaryKey
    val dateEpochDay: Long,
    val totalSteps: Int,
    val targetSteps: Int,
    val lastUpdatedAt: Long,
)

fun DailyStepEntity.asExternalModel(): DailyStepCount = DailyStepCount(
    dateEpochDay = dateEpochDay,
    steps = totalSteps,
    targetSteps = targetSteps,
)
