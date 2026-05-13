package com.river.walklog.feature.report

import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.testing.MainDispatcherRule
import com.river.walklog.core.domain.usecase.GetWeeklyReportArchiveUseCase
import com.river.walklog.core.model.WeeklyArchiveSummary
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyReportArchiveViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var getWeeklyReportArchive: GetWeeklyReportArchiveUseCase
    private lateinit var crashReporter: CrashReporter

    @Before
    fun setUp() {
        getWeeklyReportArchive = mockk()
        crashReporter = mockk(relaxed = true)
    }

    // 초기 상태

    @Test
    fun `isLoading is true when use case has not yet emitted`() {
        every { getWeeklyReportArchive() } returns MutableSharedFlow()
        assertTrue(createViewModel().state.value.isLoading)
    }

    @Test
    fun `archiveItems is empty before first emission`() {
        every { getWeeklyReportArchive() } returns MutableSharedFlow()
        assertTrue(createViewModel().state.value.archiveItems.isEmpty())
    }

    @Test
    fun `isError is false on initial state`() {
        every { getWeeklyReportArchive() } returns MutableSharedFlow()
        assertFalse(createViewModel().state.value.isError)
    }

    // 성공

    @Test
    fun `isLoading becomes false after successful emission`() {
        every { getWeeklyReportArchive() } returns flowOf(emptyList())
        assertFalse(createViewModel().state.value.isLoading)
    }

    @Test
    fun `isError stays false on successful emission`() {
        every { getWeeklyReportArchive() } returns flowOf(emptyList())
        assertFalse(createViewModel().state.value.isError)
    }

    @Test
    fun `archiveItems count matches domain item count`() {
        val items = listOf(archiveItem(weekStart = 19_000L), archiveItem(weekStart = 18_993L))
        every { getWeeklyReportArchive() } returns flowOf(items)
        assertEquals(2, createViewModel().state.value.archiveItems.size)
    }

    @Test
    fun `archiveItems are empty when use case emits empty list`() {
        every { getWeeklyReportArchive() } returns flowOf(emptyList())
        assertTrue(createViewModel().state.value.archiveItems.isEmpty())
    }

    // domain → UI 모델 필드 매핑

    @Test
    fun `weekStartEpochDay is mapped from domain item`() {
        every { getWeeklyReportArchive() } returns flowOf(listOf(archiveItem(weekStart = 19_000L)))
        assertEquals(19_000L, createViewModel().state.value.archiveItems[0].weekStartEpochDay)
    }

    @Test
    fun `isLocked is mapped from domain item`() {
        every { getWeeklyReportArchive() } returns flowOf(listOf(archiveItem(isLocked = true)))
        assertTrue(createViewModel().state.value.archiveItems[0].isLocked)
    }

    @Test
    fun `totalSteps is mapped from domain item`() {
        every { getWeeklyReportArchive() } returns flowOf(listOf(archiveItem(totalSteps = 42_000)))
        assertEquals(42_000, createViewModel().state.value.archiveItems[0].totalSteps)
    }

    @Test
    fun `achievementPct is mapped from domain item`() {
        every { getWeeklyReportArchive() } returns flowOf(listOf(archiveItem(achievementPct = 85)))
        assertEquals(85, createViewModel().state.value.archiveItems[0].achievementPct)
    }

    @Test
    fun `achievementRate is mapped from domain item`() {
        every { getWeeklyReportArchive() } returns flowOf(listOf(archiveItem(achievementRate = 0.85f)))
        assertEquals(0.85f, createViewModel().state.value.archiveItems[0].achievementRate)
    }

    @Test
    fun `unlockDate is mapped from domain item`() {
        val unlockDate = LocalDate.of(2026, 5, 20)
        every { getWeeklyReportArchive() } returns flowOf(listOf(archiveItem(unlockDate = unlockDate)))
        assertEquals(unlockDate, createViewModel().state.value.archiveItems[0].unlockDate)
    }

    @Test
    fun `achievementRateText formats achievementPct as percentage string`() {
        every { getWeeklyReportArchive() } returns flowOf(listOf(archiveItem(achievementPct = 71)))
        assertEquals("71%", createViewModel().state.value.archiveItems[0].achievementRateText)
    }

    // derived UI 프로퍼티

    @Test
    fun `weekStart is derived from weekStartEpochDay`() {
        val epochDay = LocalDate.of(2026, 5, 11).toEpochDay()
        every { getWeeklyReportArchive() } returns flowOf(listOf(archiveItem(weekStart = epochDay)))
        assertEquals(LocalDate.of(2026, 5, 11), createViewModel().state.value.archiveItems[0].weekStart)
    }

    @Test
    fun `weekEnd is 6 days after weekStart`() {
        val monday = LocalDate.of(2026, 5, 11)
        every { getWeeklyReportArchive() } returns flowOf(listOf(archiveItem(weekStart = monday.toEpochDay())))
        assertEquals(LocalDate.of(2026, 5, 17), createViewModel().state.value.archiveItems[0].weekEnd)
    }

    // 에러

    @Test
    fun `isError becomes true when use case throws`() {
        every { getWeeklyReportArchive() } returns flow { throw RuntimeException("network error") }
        assertTrue(createViewModel().state.value.isError)
    }

    @Test
    fun `isLoading becomes false when use case throws`() {
        every { getWeeklyReportArchive() } returns flow { throw RuntimeException("network error") }
        assertFalse(createViewModel().state.value.isLoading)
    }

    @Test
    fun `archiveItems stay empty when use case throws`() {
        every { getWeeklyReportArchive() } returns flow { throw RuntimeException("network error") }
        assertTrue(createViewModel().state.value.archiveItems.isEmpty())
    }

    // helpers

    private fun createViewModel() = WeeklyReportArchiveViewModel(getWeeklyReportArchive, crashReporter)

    private fun archiveItem(
        weekStart: Long = 19_000L,
        isLocked: Boolean = false,
        totalSteps: Int = 0,
        achievementPct: Int = 0,
        achievementRate: Float = 0f,
        unlockDate: LocalDate = LocalDate.ofEpochDay(weekStart + 7),
    ) = WeeklyArchiveSummary(
        weekStartEpochDay = weekStart,
        isLocked = isLocked,
        unlockDate = unlockDate,
        totalSteps = totalSteps,
        achievedDays = 0,
        totalDays = 7,
        achievementPct = achievementPct,
        achievementRate = achievementRate,
    )
}
