package com.river.walklog.feature.recap

import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.GetMonthlyRecapUseCase
import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.testing.MainDispatcherRule
import com.river.walklog.core.testing.repository.FakeStepRepository
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RecapViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeStepRepository: FakeStepRepository
    private lateinit var getMonthlyRecap: GetMonthlyRecapUseCase
    private lateinit var crashReporter: CrashReporter

    @Before
    fun setUp() {
        fakeStepRepository = FakeStepRepository()
        getMonthlyRecap = GetMonthlyRecapUseCase(fakeStepRepository)
        crashReporter = mockk(relaxed = true)
    }

    // 초기 상태

    @Test
    fun `isLoading is false after initial emission`() {
        assertFalse(createViewModel().state.value.isLoading)
    }

    @Test
    fun `recap is not null after initial emission`() {
        assertNotNull(createViewModel().state.value.recap)
    }

    @Test
    fun `isError is false on successful load`() {
        assertFalse(createViewModel().state.value.isError)
    }

    // init — 이전 달 자동 로드

    @Test
    fun `init loads recap for the previous calendar month`() {
        val expected = YearMonth.now().minusMonths(1)
        val viewModel = createViewModel()
        assertEquals(expected.monthValue, viewModel.state.value.recap?.month)
    }

    @Test
    fun `init loads recap for the correct year of the previous month`() {
        val expected = YearMonth.now().minusMonths(1)
        val viewModel = createViewModel()
        assertEquals(expected.year, viewModel.state.value.recap?.year)
    }

    // 성공 — totalSteps 계산

    @Test
    fun `totalSteps reflects seeded step data for previous month`() {
        val recapMonth = YearMonth.now().minusMonths(1)
        val firstDay = LocalDate.of(recapMonth.year, recapMonth.monthValue, 1)
        val dayCount = recapMonth.lengthOfMonth()
        fakeStepRepository.setDailyStepCounts(
            (0 until dayCount).map { offset ->
                DailyStepCount(dateEpochDay = firstDay.toEpochDay() + offset, steps = 3_000)
            },
        )
        val viewModel = createViewModel()
        assertEquals(3_000 * dayCount, viewModel.state.value.recap?.totalSteps)
    }

    @Test
    fun `achievedDays counts days where steps meet target`() {
        val recapMonth = YearMonth.now().minusMonths(1)
        val firstDay = LocalDate.of(recapMonth.year, recapMonth.monthValue, 1)
        fakeStepRepository.setDailyStepCounts(
            listOf(
                DailyStepCount(dateEpochDay = firstDay.toEpochDay(), steps = 7_000, targetSteps = 6_000),
                DailyStepCount(dateEpochDay = firstDay.toEpochDay() + 1, steps = 3_000, targetSteps = 6_000),
            ),
        )
        val viewModel = createViewModel()
        assertEquals(1, viewModel.state.value.recap?.achievedDays)
    }

    @Test
    fun `estimatedCalories is 4 percent of totalSteps`() {
        val recapMonth = YearMonth.now().minusMonths(1)
        val firstDay = LocalDate.of(recapMonth.year, recapMonth.monthValue, 1)
        fakeStepRepository.setDailyStepCounts(
            listOf(DailyStepCount(dateEpochDay = firstDay.toEpochDay(), steps = 10_000)),
        )
        val viewModel = createViewModel()
        assertEquals((10_000 * 0.04f).toInt(), viewModel.state.value.recap?.estimatedCalories)
    }

    // 에러

    @Test
    fun `isError is true when repository throws`() {
        fakeStepRepository.setThrowable(RuntimeException("DB error"))
        assertTrue(createViewModel().state.value.isError)
    }

    @Test
    fun `isLoading is false when repository throws`() {
        fakeStepRepository.setThrowable(RuntimeException("DB error"))
        assertFalse(createViewModel().state.value.isLoading)
    }

    @Test
    fun `recap stays null when repository throws`() {
        fakeStepRepository.setThrowable(RuntimeException("DB error"))
        assertNull(createViewModel().state.value.recap)
    }

    // loadRecap(year, month)

    @Test
    fun `loadRecap updates year and month in state`() = runTest {
        val viewModel = createViewModel()
        viewModel.loadRecap(2024, 6)
        assertEquals(2024, viewModel.state.value.recap?.year)
        assertEquals(6, viewModel.state.value.recap?.month)
    }

    @Test
    fun `loadRecap totalSteps reflects seeded data for given month`() = runTest {
        val june1 = LocalDate.of(2024, 6, 1)
        fakeStepRepository.setDailyStepCounts(
            (0 until 30).map { offset ->
                DailyStepCount(dateEpochDay = june1.toEpochDay() + offset, steps = 4_000)
            },
        )
        val viewModel = createViewModel()
        viewModel.loadRecap(2024, 6)
        assertEquals(4_000 * 30, viewModel.state.value.recap?.totalSteps)
    }

    @Test
    fun `loadRecap sets isError true when repository throws`() = runTest {
        val viewModel = createViewModel()
        fakeStepRepository.setThrowable(RuntimeException("fail"))
        viewModel.loadRecap(2023, 12)
        assertTrue(viewModel.state.value.isError)
    }

    @Test
    fun `loadRecap sets isLoading false on error`() = runTest {
        val viewModel = createViewModel()
        fakeStepRepository.setThrowable(RuntimeException("fail"))
        viewModel.loadRecap(2023, 12)
        assertFalse(viewModel.state.value.isLoading)
    }

    // helpers

    private fun createViewModel() = RecapViewModel(getMonthlyRecap, crashReporter)
}
