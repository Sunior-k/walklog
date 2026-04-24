package com.river.walklog.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 일간 걸음 수 집계 Entity.
 * - dateEpochDay: LocalDate.toEpochDay() 형태로 저장 (UTC 기준)
 * - totalSteps: 해당 일의 총 걸음 수
 * - targetSteps: 해당 일의 목표 걸음 수
 * - lastUpdatedAt: 해당 일의 걸음 수 정보가 마지막으로 업데이트된 시점 (Epoch millis)
 */
@Entity(tableName = "daily_steps")
data class DailyStepEntity(
    @PrimaryKey
    val dateEpochDay: Long,
    val totalSteps: Int,
    val targetSteps: Int = 6_000,
    val lastUpdatedAt: Long,
)
