package com.river.walklog.core.data.repository

import app.cash.turbine.test
import com.river.walklog.core.common.dispatcher.WalkLogDispatchers
import com.river.walklog.core.data.healthconnect.HealthConnectStepDataSource
import com.river.walklog.core.database.dao.DailyStepDao
import com.river.walklog.core.database.entity.DailyStepEntity
import com.river.walklog.core.model.DailyStepCount
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OfflineFirstStepRepositoryTest {

    private lateinit var healthConnectDataSource: HealthConnectStepDataSource
    private lateinit var dailyStepDao: DailyStepDao
    private lateinit var repository: OfflineFirstStepRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        healthConnectDataSource = mockk()
        dailyStepDao = mockk()
        repository = OfflineFirstStepRepository(
            healthConnectDataSource = healthConnectDataSource,
            dailyStepDao = dailyStepDao,
            dispatchers = WalkLogDispatchers(
                io = testDispatcher,
                default = testDispatcher,
                main = testDispatcher,
            ),
        )
    }

    // ─── isHealthConnectAvailable ──────────────────────────────────────────

    @Test
    fun `isHealthConnectAvailable delegates to healthConnectDataSource`() {
        every { healthConnectDataSource.isAvailable() } returns true
        assertTrue(repository.isHealthConnectAvailable)

        every { healthConnectDataSource.isAvailable() } returns false
        assertFalse(repository.isHealthConnectAvailable)
    }

    // ─── getStepsForDay ────────────────────────────────────────────────────

    @Test
    fun `getStepsForDay emits DailyStepCount with 0 steps when no entity and HC returns 0`() = runTest {
        val epochDay = 19_000L
        coEvery { healthConnectDataSource.readDailySteps(any()) } returns 0
        coJustRun { dailyStepDao.upsert(any()) }
        every { dailyStepDao.observeForDay(epochDay) } returns flowOf(null)

        repository.getStepsForDay(epochDay).test {
            val item = awaitItem()
            assertEquals(epochDay, item.dateEpochDay)
            assertEquals(0, item.steps)
            awaitComplete()
        }
    }

    @Test
    fun `getStepsForDay maps entity totalSteps to domain steps`() = runTest {
        val epochDay = 19_000L
        val entity = dailyEntity(epochDay = epochDay, totalSteps = 7_500, targetSteps = 6_000)
        coEvery { healthConnectDataSource.readDailySteps(any()) } returns 7_500
        coJustRun { dailyStepDao.upsert(any()) }
        every { dailyStepDao.observeForDay(epochDay) } returns flowOf(entity)

        repository.getStepsForDay(epochDay).test {
            val item = awaitItem()
            assertEquals(7_500, item.steps)
            assertEquals(6_000, item.targetSteps)
            awaitComplete()
        }
    }

    @Test
    fun `getStepsForDay preserves dateEpochDay from entity`() = runTest {
        val epochDay = 20_000L
        val entity = dailyEntity(epochDay = epochDay, totalSteps = 3_000)
        coEvery { healthConnectDataSource.readDailySteps(any()) } returns 3_000
        coJustRun { dailyStepDao.upsert(any()) }
        every { dailyStepDao.observeForDay(epochDay) } returns flowOf(entity)

        repository.getStepsForDay(epochDay).test {
            assertEquals(epochDay, awaitItem().dateEpochDay)
            awaitComplete()
        }
    }

    // ─── getStepCountsForRange ─────────────────────────────────────────────

    @Test
    fun `getStepCountsForRange fills missing days with 0 steps`() = runTest {
        val from = 0L
        val to = 2L
        val entity = dailyEntity(epochDay = 1L, totalSteps = 5_000)
        coEvery { healthConnectDataSource.readDailySteps(any()) } returns 0
        coJustRun { dailyStepDao.upsert(any()) }
        every { dailyStepDao.observeForRange(from, to) } returns flowOf(listOf(entity))

        repository.getStepCountsForRange(from, to).test {
            val counts = awaitItem()
            assertEquals(3, counts.size)
            assertEquals(0, counts[0].steps) // day 0 — gap filled
            assertEquals(5_000, counts[1].steps) // day 1 — from entity
            assertEquals(0, counts[2].steps) // day 2 — gap filled
            awaitComplete()
        }
    }

    @Test
    fun `getStepCountsForRange returns all days in order`() = runTest {
        val from = 100L
        val to = 102L
        val entities = listOf(
            dailyEntity(epochDay = 100L, totalSteps = 1_000),
            dailyEntity(epochDay = 101L, totalSteps = 2_000),
            dailyEntity(epochDay = 102L, totalSteps = 3_000),
        )
        coEvery { healthConnectDataSource.readDailySteps(any()) } returns 0
        coJustRun { dailyStepDao.upsert(any()) }
        every { dailyStepDao.observeForRange(from, to) } returns flowOf(entities)

        repository.getStepCountsForRange(from, to).test {
            val counts = awaitItem()
            assertEquals(listOf(1_000, 2_000, 3_000), counts.map { it.steps })
            awaitComplete()
        }
    }

    // ─── getWeeklyStepSummary ──────────────────────────────────────────────

    @Test
    fun `getWeeklyStepSummary spans exactly 7 days from weekStart`() = runTest {
        val weekStart = 19_000L
        coEvery { healthConnectDataSource.readDailySteps(any()) } returns 0
        coJustRun { dailyStepDao.upsert(any()) }
        every {
            dailyStepDao.observeForRange(weekStart, weekStart + 6)
        } returns flowOf(emptyList())

        repository.getWeeklyStepSummary(weekStart).test {
            val summary = awaitItem()
            assertEquals(weekStart, summary.weekStartEpochDay)
            assertEquals(7, summary.dailyCounts.size)
            awaitComplete()
        }
    }

    // ─── getHourlyStepsForRange ────────────────────────────────────────────

    @Test
    fun `getHourlyStepsForRange returns empty array when HC throws`() = runTest {
        coEvery { healthConnectDataSource.readHourlySteps(any(), any()) } throws Exception("HC error")

        val result = repository.getHourlyStepsForRange(0L, 0L)
        assertEquals(24, result.size) // 1 day × 24 hours
        assertTrue(result.all { it == 0f })
    }

    @Test
    fun `getHourlyStepsForRange delegates to HealthConnectStepDataSource`() = runTest {
        val expected = FloatArray(7 * 24) { it.toFloat() }
        coEvery {
            healthConnectDataSource.readHourlySteps(
                from = LocalDate.ofEpochDay(0L),
                to = LocalDate.ofEpochDay(6L),
            )
        } returns expected

        val result = repository.getHourlyStepsForRange(0L, 6L)
        assertEquals(expected.toList(), result.toList())
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private fun dailyEntity(
        epochDay: Long,
        totalSteps: Int = 0,
        targetSteps: Int = DailyStepCount.DEFAULT_TARGET_STEPS,
    ) = DailyStepEntity(
        dateEpochDay = epochDay,
        totalSteps = totalSteps,
        targetSteps = targetSteps,
        lastUpdatedAt = 0L,
    )
}
