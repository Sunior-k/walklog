package com.river.walklog.core.testing.repository

import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.model.WeeklyStepSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class FakeStepRepository : StepRepository {

    override var isHealthConnectAvailable: Boolean = true

    private val currentSteps = MutableStateFlow(0)
    private val dailyStepCounts = MutableStateFlow<Map<Long, DailyStepCount>>(emptyMap())
    private var hourlySteps = FloatArray(HOURS_PER_WEEK)
    private var syncTodayStepsResult = 0
    private var throwable: Throwable? = null

    override fun observeCurrentSteps(): Flow<Int> = currentSteps

    override fun getStepsForDay(dateEpochDay: Long): Flow<DailyStepCount> =
        dailyStepCounts.map { counts ->
            counts[dateEpochDay] ?: DailyStepCount(dateEpochDay = dateEpochDay, steps = 0)
        }

    override fun getStepCountsForRange(
        fromEpochDay: Long,
        toEpochDay: Long,
    ): Flow<List<DailyStepCount>> = flow {
        throwable?.let { throw it }
        emitAll(
            dailyStepCounts.map { counts ->
                (fromEpochDay..toEpochDay).map { epochDay ->
                    counts[epochDay] ?: DailyStepCount(dateEpochDay = epochDay, steps = 0)
                }
            },
        )
    }

    override fun getWeeklyStepSummary(weekStartEpochDay: Long): Flow<WeeklyStepSummary> =
        getStepCountsForRange(
            fromEpochDay = weekStartEpochDay,
            toEpochDay = weekStartEpochDay + DAYS_PER_WEEK - 1,
        ).map { counts ->
            WeeklyStepSummary(
                weekStartEpochDay = weekStartEpochDay,
                dailyCounts = counts,
            )
        }

    override suspend fun getHourlyStepsForRange(
        fromEpochDay: Long,
        toEpochDay: Long,
    ): FloatArray = hourlySteps.copyOf()

    override suspend fun syncTodaySteps(): Int = syncTodayStepsResult

    fun setCurrentSteps(steps: Int) {
        currentSteps.value = steps
    }

    fun setDailyStepCounts(counts: List<DailyStepCount>) {
        dailyStepCounts.value = counts.associateBy { it.dateEpochDay }
    }

    fun setDailyStepCount(count: DailyStepCount) {
        dailyStepCounts.value = dailyStepCounts.value + (count.dateEpochDay to count)
    }

    fun setHourlySteps(steps: FloatArray) {
        hourlySteps = steps.copyOf()
    }

    fun setSyncTodayStepsResult(steps: Int) {
        syncTodayStepsResult = steps
    }

    fun setThrowable(t: Throwable) {
        throwable = t
    }

    fun clearThrowable() {
        throwable = null
    }

    private companion object {
        const val DAYS_PER_WEEK = 7L
        const val HOURS_PER_WEEK = 24 * 7
    }
}
