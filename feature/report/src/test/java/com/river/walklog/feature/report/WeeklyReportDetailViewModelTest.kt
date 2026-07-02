package com.river.walklog.feature.report

import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.GetWeeklyBestHourUseCase
import com.river.walklog.core.domain.usecase.GetWeeklyReportDetailUseCase
import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.testing.MainDispatcherRule
import com.river.walklog.core.testing.repository.FakeStepRepository
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyReportDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeStepRepository: FakeStepRepository
    private lateinit var getWeeklyReportDetail: GetWeeklyReportDetailUseCase
    private lateinit var crashReporter: CrashReporter
    private lateinit var viewModel: WeeklyReportDetailViewModel

    @Before
    fun setUp() {
        fakeStepRepository = FakeStepRepository()
        getWeeklyReportDetail = GetWeeklyReportDetailUseCase(
            fakeStepRepository,
            GetWeeklyBestHourUseCase(fakeStepRepository),
        )
        crashReporter = mockk(relaxed = true)
        viewModel = WeeklyReportDetailViewModel(getWeeklyReportDetail, crashReporter)
    }

    // 초기 상태 (loadReport 호출 전)

    @Test
    fun `initial state has isLoading true`() {
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `initial state has isError false`() {
        assertFalse(viewModel.state.value.isError)
    }

    @Test
    fun `initial state has isEmpty false`() {
        assertFalse(viewModel.state.value.isEmpty)
    }

    @Test
    fun `initial state has isSharing false`() {
        assertFalse(viewModel.state.value.isSharing)
    }

    @Test
    fun `initial weekStartEpochDay is null`() {
        assertNull(viewModel.state.value.weekStartEpochDay)
    }

    @Test
    fun `userMessage is null initially`() {
        assertNull(viewModel.state.value.userMessage)
    }

    // loadReport — 성공

    @Test
    fun `loadReport sets isLoading false on success`() = runTest {
        seedWeekSteps(WEEK_START, stepsPerDay = 5_000)
        viewModel.loadReport(WEEK_START)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `loadReport sets isEmpty false when detail has step data`() = runTest {
        seedWeekSteps(WEEK_START, stepsPerDay = 5_000)
        viewModel.loadReport(WEEK_START)
        assertFalse(viewModel.state.value.isEmpty)
    }

    @Test
    fun `loadReport sets weekStartEpochDay`() = runTest {
        seedWeekSteps(WEEK_START, stepsPerDay = 5_000)
        viewModel.loadReport(WEEK_START)
        assertEquals(WEEK_START, viewModel.state.value.weekStartEpochDay)
    }

    @Test
    fun `loadReport maps totalSteps from seeded data`() = runTest {
        seedWeekSteps(WEEK_START, stepsPerDay = 5_000)
        viewModel.loadReport(WEEK_START)
        assertEquals(5_000 * 7, viewModel.state.value.totalSteps)
    }

    @Test
    fun `loadReport maps achievedDays from seeded data`() = runTest {
        fakeStepRepository.setDailyStepCounts(
            (0L..6L).map { offset ->
                DailyStepCount(
                    dateEpochDay = WEEK_START + offset,
                    steps = if (offset < 5) 7_000 else 3_000,
                    targetSteps = 6_000,
                )
            },
        )
        viewModel.loadReport(WEEK_START)
        assertEquals(5, viewModel.state.value.achievedDays)
    }

    @Test
    fun `loadReport computes achievementPct from achievedDays`() = runTest {
        fakeStepRepository.setDailyStepCounts(
            (0L..6L).map { offset ->
                DailyStepCount(
                    dateEpochDay = WEEK_START + offset,
                    steps = if (offset < 5) 7_000 else 3_000,
                    targetSteps = 6_000,
                )
            },
        )
        viewModel.loadReport(WEEK_START)
        assertEquals(71, viewModel.state.value.achievementPct) // 5/7 = 71%
    }

    // summaryMessageType 분기

    @Test
    fun `summaryMessageType is AllAchieved when all 7 days achieved`() = runTest {
        seedWeekSteps(WEEK_START, stepsPerDay = 7_000, targetSteps = 6_000)
        viewModel.loadReport(WEEK_START)
        assertEquals(WeeklyReportSummaryMessageType.AllAchieved, viewModel.state.value.summaryMessageType)
    }

    @Test
    fun `summaryMessageType is GoodProgress when 5 of 7 days achieved`() = runTest {
        fakeStepRepository.setDailyStepCounts(
            (0L..6L).map { offset ->
                DailyStepCount(
                    dateEpochDay = WEEK_START + offset,
                    steps = if (offset < 5) 7_000 else 3_000,
                    targetSteps = 6_000,
                )
            },
        )
        viewModel.loadReport(WEEK_START)
        assertEquals(WeeklyReportSummaryMessageType.GoodProgress, viewModel.state.value.summaryMessageType)
    }

    @Test
    fun `summaryMessageType is KeepGoing when fewer than 5 days achieved`() = runTest {
        fakeStepRepository.setDailyStepCounts(
            (0L..6L).map { offset ->
                DailyStepCount(
                    dateEpochDay = WEEK_START + offset,
                    steps = if (offset < 3) 7_000 else 3_000,
                    targetSteps = 6_000,
                )
            },
        )
        viewModel.loadReport(WEEK_START)
        assertEquals(WeeklyReportSummaryMessageType.KeepGoing, viewModel.state.value.summaryMessageType)
    }

    // loadReport — 빈 결과 (모든 걸음 수가 0)

    @Test
    fun `loadReport sets isEmpty true when all steps are zero`() = runTest {
        viewModel.loadReport(WEEK_START)
        assertTrue(viewModel.state.value.isEmpty)
    }

    @Test
    fun `loadReport sets isLoading false for empty detail`() = runTest {
        viewModel.loadReport(WEEK_START)
        assertFalse(viewModel.state.value.isLoading)
    }

    // loadReport — 에러

    @Test
    fun `loadReport sets isError true when repository throws`() = runTest {
        fakeStepRepository.setThrowable(RuntimeException("network"))
        viewModel.loadReport(WEEK_START)
        assertTrue(viewModel.state.value.isError)
    }

    @Test
    fun `loadReport sets isLoading false on error`() = runTest {
        fakeStepRepository.setThrowable(RuntimeException("network"))
        viewModel.loadReport(WEEK_START)
        assertFalse(viewModel.state.value.isLoading)
    }

    // 연속 loadReport — isSharing 초기화

    @Test
    fun `second loadReport resets isSharing to false`() = runTest {
        seedWeekSteps(WEEK_START, stepsPerDay = 5_000)
        viewModel.loadReport(WEEK_START)
        viewModel.startSharing()

        seedWeekSteps(WEEK_START + 7, stepsPerDay = 5_000)
        viewModel.loadReport(WEEK_START + 7)

        assertFalse(viewModel.state.value.isSharing)
    }

    // 공유 상태

    @Test
    fun `startSharing sets isSharing true`() {
        viewModel.startSharing()
        assertTrue(viewModel.state.value.isSharing)
    }

    @Test
    fun `completeSharing sets isSharing false`() {
        viewModel.startSharing()
        viewModel.completeSharing()
        assertFalse(viewModel.state.value.isSharing)
    }

    @Test
    fun `failSharing sets isSharing false`() {
        viewModel.startSharing()
        viewModel.failSharing()
        assertFalse(viewModel.state.value.isSharing)
    }

    @Test
    fun `failSharing sets userMessage to ShareFailed`() {
        viewModel.failSharing()
        assertEquals(WeeklyReportUserMessage.ShareFailed, viewModel.state.value.userMessage)
    }

    @Test
    fun `clearUserMessage removes userMessage`() {
        viewModel.failSharing()
        viewModel.clearUserMessage()
        assertNull(viewModel.state.value.userMessage)
    }

    // achievementRateText

    @Test
    fun `achievementRateText formats achievementPct as percentage string`() = runTest {
        fakeStepRepository.setDailyStepCounts(
            (0L..6L).map { offset ->
                DailyStepCount(
                    dateEpochDay = WEEK_START + offset,
                    steps = if (offset < 5) 7_000 else 3_000,
                    targetSteps = 6_000,
                )
            },
        )
        viewModel.loadReport(WEEK_START)
        assertEquals("71%", viewModel.state.value.achievementRateText)
    }

    // helpers

    private fun seedWeekSteps(
        weekStart: Long,
        stepsPerDay: Int,
        targetSteps: Int = DailyStepCount.DEFAULT_TARGET_STEPS,
    ) {
        fakeStepRepository.setDailyStepCounts(
            (0L..6L).map { offset ->
                DailyStepCount(
                    dateEpochDay = weekStart + offset,
                    steps = stepsPerDay,
                    targetSteps = targetSteps,
                )
            },
        )
    }

    private companion object {
        const val WEEK_START = 19_000L
    }
}
