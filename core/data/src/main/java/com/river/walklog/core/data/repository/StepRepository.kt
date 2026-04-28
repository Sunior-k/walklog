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

    /**
     * Health Connect에서 오늘 걸음 수를 직접 읽어 Room 캐시에 저장하고 반환한다.
     * HC 접근 실패 시 예외를 그대로 던짐.
     */
    suspend fun syncTodaySteps(): Int
}
