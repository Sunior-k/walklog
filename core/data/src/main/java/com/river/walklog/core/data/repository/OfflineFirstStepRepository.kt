package com.river.walklog.core.data.repository

import com.river.walklog.core.common.dispatcher.WalkLogDispatchers
import com.river.walklog.core.data.healthconnect.HealthConnectStepDataSource
import com.river.walklog.core.database.dao.DailyStepDao
import com.river.walklog.core.database.entity.DailyStepEntity
import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.model.WeeklyStepSummary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstStepRepository @Inject constructor(
    private val healthConnectDataSource: HealthConnectStepDataSource,
    private val dailyStepDao: DailyStepDao,
    private val dispatchers: WalkLogDispatchers,
) : StepRepository {

    override val isHealthConnectAvailable: Boolean
        get() = healthConnectDataSource.isAvailable()

    /**
     * Health Connect 를 [POLL_INTERVAL_MS] 마다 폴링하여 오늘의 걸음 수를 방출한다.
     *
     * HC 를 사용할 수 없거나 권한이 없으면 로컬 캐시 값을 반환하고 계속 재시도한다.
     * 폴링마다 로컬 캐시를 갱신하므로 [getStepsForDay] 등의 쿼리도 최신 상태를 유지한다.
     */
    override fun observeCurrentSteps(): Flow<Int> = flow {
        while (true) {
            val today = LocalDate.now()
            val epochDay = today.toEpochDay()
            val steps = runCatching {
                val hcSteps = healthConnectDataSource.readDailySteps(today)
                dailyStepDao.upsert(
                    DailyStepEntity(
                        dateEpochDay = epochDay,
                        totalSteps = hcSteps,
                        lastUpdatedAt = System.currentTimeMillis(),
                    ),
                )
                hcSteps
            }.getOrElse {
                dailyStepDao.getForDay(epochDay)?.totalSteps ?: 0
            }
            emit(steps)
            delay(POLL_INTERVAL_MS)
        }
    }.flowOn(dispatchers.io)

    /**
     * 특정 날짜의 걸음 수를 관찰한다.
     *
     * [onStart] 에서 HC 를 한 번 조회하여 로컬 캐시를 최신화한 뒤,
     * Room Flow 로 반응형 업데이트를 제공한다.
     */
    override fun getStepsForDay(dateEpochDay: Long): Flow<DailyStepCount> =
        dailyStepDao.observeForDay(dateEpochDay)
            .onStart { seedFromHealthConnect(dateEpochDay) }
            .map { entity ->
                entity?.toDomain() ?: DailyStepCount(dateEpochDay = dateEpochDay, steps = 0)
            }
            .flowOn(dispatchers.io)

    override fun getStepCountsForRange(
        fromEpochDay: Long,
        toEpochDay: Long,
    ): Flow<List<DailyStepCount>> =
        dailyStepDao.observeForRange(fromEpochDay, toEpochDay)
            .onStart {
                (fromEpochDay..toEpochDay).forEach { seedFromHealthConnect(it) }
            }
            .map { entities ->
                val entityMap = entities.associateBy { it.dateEpochDay }
                (fromEpochDay..toEpochDay).map { day ->
                    entityMap[day]?.toDomain()
                        ?: DailyStepCount(dateEpochDay = day, steps = 0)
                }
            }
            .flowOn(dispatchers.io)

    override fun getWeeklyStepSummary(weekStartEpochDay: Long): Flow<WeeklyStepSummary> {
        val weekEndEpochDay = weekStartEpochDay + 6L
        return getStepCountsForRange(weekStartEpochDay, weekEndEpochDay)
            .map { dailyCounts ->
                WeeklyStepSummary(
                    weekStartEpochDay = weekStartEpochDay,
                    dailyCounts = dailyCounts,
                )
            }
            .flowOn(dispatchers.io)
    }

    /**
     * 날짜 범위의 시간별 걸음 수를 반환한다.
     *
     * Health Connect [aggregateGroupByDuration] 으로 1시간 단위 버킷을 조회한다.
     * HC 를 사용할 수 없으면 빈 배열을 반환한다.
     */
    override suspend fun getHourlyStepsForRange(
        fromEpochDay: Long,
        toEpochDay: Long,
    ): FloatArray = withContext(dispatchers.io) {
        runCatching {
            healthConnectDataSource.readHourlySteps(
                from = LocalDate.ofEpochDay(fromEpochDay),
                to = LocalDate.ofEpochDay(toEpochDay),
            )
        }.getOrElse {
            FloatArray((toEpochDay - fromEpochDay + 1).toInt() * 24)
        }
    }

    // ─── Private ──────────────────────────────────────────────────────────────

    private suspend fun seedFromHealthConnect(dateEpochDay: Long) {
        runCatching {
            val steps = healthConnectDataSource.readDailySteps(LocalDate.ofEpochDay(dateEpochDay))
            dailyStepDao.upsert(
                DailyStepEntity(
                    dateEpochDay = dateEpochDay,
                    totalSteps = steps,
                    lastUpdatedAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    private fun DailyStepEntity.toDomain() = DailyStepCount(
        dateEpochDay = dateEpochDay,
        steps = totalSteps,
        targetSteps = targetSteps,
    )

    private companion object {
        const val POLL_INTERVAL_MS = 10_000L
    }
}
