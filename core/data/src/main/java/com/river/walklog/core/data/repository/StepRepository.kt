package com.river.walklog.core.data.repository

import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.model.WeeklyStepSummary
import kotlinx.coroutines.flow.Flow

interface StepRepository {

    /** Health Connect SDK 가 이 기기에서 사용 가능한지 여부 판별. */
    val isHealthConnectAvailable: Boolean

    fun observeCurrentSteps(): Flow<Int>

    fun getStepsForDay(dateEpochDay: Long): Flow<DailyStepCount>

    fun getStepCountsForRange(
        fromEpochDay: Long,
        toEpochDay: Long,
    ): Flow<List<DailyStepCount>>

    fun getWeeklyStepSummary(weekStartEpochDay: Long): Flow<WeeklyStepSummary>

    suspend fun getHourlyStepsForRange(fromEpochDay: Long, toEpochDay: Long): FloatArray
}
