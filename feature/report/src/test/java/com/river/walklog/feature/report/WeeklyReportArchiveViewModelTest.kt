package com.river.walklog.feature.report

import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.GetWeeklyReportArchiveUseCase
import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.testing.MainDispatcherRule
import com.river.walklog.core.testing.repository.FakeStepRepository
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyReportArchiveViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeStepRepository: FakeStepRepository
    private lateinit var getWeeklyReportArchive: GetWeeklyReportArchiveUseCase
    private lateinit var crashReporter: CrashReporter

    private val currentWeekStart: LocalDate
        get() = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    @Before
    fun setUp() {
        fakeStepRepository = FakeStepRepository()
        getWeeklyReportArchive = GetWeeklyReportArchiveUseCase(fakeStepRepository)
        crashReporter = mockk(relaxed = true)
    }

    // 초기 상태

    @Test
    fun `isLoading is false after initial emission`() {
        assertFalse(createViewModel().state.value.isLoading)
    }

    @Test
    fun `isError is false after initial emission`() {
        assertFalse(createViewModel().state.value.isError)
    }

    @Test
    fun `archiveItems always includes the current locked week`() {
        assertTrue(createViewModel().state.value.archiveItems.any { it.isLocked })
    }

    // 현재 주 (locked) 필드 검증

    @Test
    fun `current week weekStart equals this Monday`() {
        val lockedItem = createViewModel().state.value.archiveItems.first { it.isLocked }
        assertEquals(currentWeekStart, lockedItem.weekStart)
    }

    @Test
    fun `current week weekEnd is 6 days after weekStart`() {
        val lockedItem = createViewModel().state.value.archiveItems.first { it.isLocked }
        assertEquals(currentWeekStart.plusDays(6), lockedItem.weekEnd)
    }

    @Test
    fun `current week weekStartEpochDay matches Monday epoch`() {
        val lockedItem = createViewModel().state.value.archiveItems.first { it.isLocked }
        assertEquals(currentWeekStart.toEpochDay(), lockedItem.weekStartEpochDay)
    }

    // 데이터 없는 과거 주는 제외

    @Test
    fun `past week with zero steps is excluded from archive`() {
        val items = createViewModel().state.value.archiveItems
        assertTrue(items.all { it.isLocked || it.totalSteps > 0 })
    }

    // 과거 주 데이터 포함

    @Test
    fun `past week with steps appears in archive`() {
        seedWeekSteps(currentWeekStart.minusWeeks(1), stepsPerDay = 5_000)

        val items = createViewModel().state.value.archiveItems
        assertNotNull(items.find { !it.isLocked })
    }

    @Test
    fun `past week totalSteps equals sum of seeded daily steps`() {
        seedWeekSteps(currentWeekStart.minusWeeks(1), stepsPerDay = 4_000)

        val pastItem = createViewModel().state.value.archiveItems.first { !it.isLocked }
        assertEquals(4_000 * 7, pastItem.totalSteps)
    }

    @Test
    fun `achievementPct reflects ratio of achieved days to total days`() {
        val pastWeekStart = currentWeekStart.minusWeeks(1)
        fakeStepRepository.setDailyStepCounts(
            (0L..6L).map { offset ->
                DailyStepCount(
                    dateEpochDay = pastWeekStart.toEpochDay() + offset,
                    steps = if (offset < 5) 7_000 else 3_000,
                    targetSteps = 6_000,
                )
            },
        )

        val pastItem = createViewModel().state.value.archiveItems.first { !it.isLocked }
        assertEquals(71, pastItem.achievementPct) // 5/7 = 71%
    }

    @Test
    fun `achievementRateText formats achievementPct as percentage string`() {
        val pastWeekStart = currentWeekStart.minusWeeks(1)
        fakeStepRepository.setDailyStepCounts(
            (0L..6L).map { offset ->
                DailyStepCount(
                    dateEpochDay = pastWeekStart.toEpochDay() + offset,
                    steps = if (offset < 5) 7_000 else 3_000,
                    targetSteps = 6_000,
                )
            },
        )

        val pastItem = createViewModel().state.value.archiveItems.first { !it.isLocked }
        assertEquals("71%", pastItem.achievementRateText)
    }

    @Test
    fun `two past weeks with steps both appear in archive`() {
        val week1Start = currentWeekStart.minusWeeks(1)
        val week2Start = currentWeekStart.minusWeeks(2)
        fakeStepRepository.setDailyStepCounts(
            (0L..6L).map { offset ->
                DailyStepCount(dateEpochDay = week1Start.toEpochDay() + offset, steps = 5_000)
            } + (0L..6L).map { offset ->
                DailyStepCount(dateEpochDay = week2Start.toEpochDay() + offset, steps = 3_000)
            },
        )

        val items = createViewModel().state.value.archiveItems
        assertEquals(3, items.size) // 2 past weeks + 1 locked current week
    }

    // 에러

    @Test
    fun `isError becomes true when repository throws`() {
        fakeStepRepository.setThrowable(RuntimeException("network error"))
        assertTrue(createViewModel().state.value.isError)
    }

    @Test
    fun `isLoading becomes false when repository throws`() {
        fakeStepRepository.setThrowable(RuntimeException("network error"))
        assertFalse(createViewModel().state.value.isLoading)
    }

    @Test
    fun `archiveItems are empty when repository throws`() {
        fakeStepRepository.setThrowable(RuntimeException("network error"))
        assertTrue(createViewModel().state.value.archiveItems.isEmpty())
    }

    // helpers

    private fun createViewModel() = WeeklyReportArchiveViewModel(getWeeklyReportArchive, crashReporter)

    private fun seedWeekSteps(weekStart: LocalDate, stepsPerDay: Int) {
        fakeStepRepository.setDailyStepCounts(
            (0L..6L).map { offset ->
                DailyStepCount(dateEpochDay = weekStart.toEpochDay() + offset, steps = stepsPerDay)
            },
        )
    }
}
