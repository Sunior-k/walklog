package com.river.walklog.feature.recap

import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.testing.MainDispatcherRule
import com.river.walklog.core.domain.usecase.GetMonthlyRecapUseCase
import com.river.walklog.core.model.MonthlyRecap
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RecapViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var getMonthlyRecap: GetMonthlyRecapUseCase
    private lateinit var crashReporter: CrashReporter

    @Before
    fun setUp() {
        getMonthlyRecap = mockk()
        crashReporter = mockk(relaxed = true)
    }

    // 초기 상태

    @Test
    fun `isLoading is true when use case has not yet emitted`() {
        every { getMonthlyRecap(any(), any()) } returns MutableSharedFlow()
        val viewModel = createViewModel()
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `recap is null when use case has not yet emitted`() {
        every { getMonthlyRecap(any(), any()) } returns MutableSharedFlow()
        val viewModel = createViewModel()
        assertNull(viewModel.state.value.recap)
    }

    @Test
    fun `isError is false on initial state`() {
        every { getMonthlyRecap(any(), any()) } returns MutableSharedFlow()
        val viewModel = createViewModel()
        assertFalse(viewModel.state.value.isError)
    }

    // 성공

    @Test
    fun `isLoading becomes false after recap emission`() {
        every { getMonthlyRecap(any(), any()) } returns flowOf(emptyRecap())
        assertFalse(createViewModel().state.value.isLoading)
    }

    @Test
    fun `recap in state reflects data emitted by use case`() {
        val expected = emptyRecap(totalSteps = 50_000)
        every { getMonthlyRecap(any(), any()) } returns flowOf(expected)
        assertEquals(expected, createViewModel().state.value.recap)
    }

    @Test
    fun `isError stays false on successful load`() {
        every { getMonthlyRecap(any(), any()) } returns flowOf(emptyRecap())
        assertFalse(createViewModel().state.value.isError)
    }

    // 에러

    @Test
    fun `isError is true when use case throws`() {
        every { getMonthlyRecap(any(), any()) } returns flow { throw RuntimeException("DB error") }
        assertTrue(createViewModel().state.value.isError)
    }

    @Test
    fun `isLoading becomes false even when use case throws`() {
        every { getMonthlyRecap(any(), any()) } returns flow { throw RuntimeException("DB error") }
        assertFalse(createViewModel().state.value.isLoading)
    }

    @Test
    fun `recap stays null when use case throws`() {
        every { getMonthlyRecap(any(), any()) } returns flow { throw RuntimeException("DB error") }
        assertNull(createViewModel().state.value.recap)
    }

    // init — 이전 달 자동 로드

    @Test
    fun `init loads recap for the previous calendar month`() {
        val capturedMonth = slot<Int>()
        every { getMonthlyRecap(any(), capture(capturedMonth)) } returns flowOf(emptyRecap())
        createViewModel()
        assertEquals(YearMonth.now().minusMonths(1).monthValue, capturedMonth.captured)
    }

    @Test
    fun `init loads recap for the correct year of the previous month`() {
        val capturedYear = slot<Int>()
        every { getMonthlyRecap(capture(capturedYear), any()) } returns flowOf(emptyRecap())
        createViewModel()
        assertEquals(YearMonth.now().minusMonths(1).year, capturedYear.captured)
    }

    // loadRecap(year, month)

    @Test
    fun `loadRecap delegates to use case with provided year and month`() = runTest {
        every { getMonthlyRecap(any(), any()) } returns flowOf(emptyRecap())
        val viewModel = createViewModel()
        val capturedYear = slot<Int>()
        val capturedMonth = slot<Int>()
        every { getMonthlyRecap(capture(capturedYear), capture(capturedMonth)) } returns flowOf(emptyRecap())

        viewModel.loadRecap(2024, 6)

        assertEquals(2024, capturedYear.captured)
        assertEquals(6, capturedMonth.captured)
    }

    @Test
    fun `loadRecap replaces state recap with newly emitted data`() = runTest {
        every { getMonthlyRecap(any(), any()) } returns flowOf(emptyRecap())
        val viewModel = createViewModel()
        val newRecap = emptyRecap(totalSteps = 99_000)
        every { getMonthlyRecap(2024, 6) } returns flowOf(newRecap)

        viewModel.loadRecap(2024, 6)

        assertEquals(newRecap, viewModel.state.value.recap)
    }

    @Test
    fun `loadRecap sets isError true when use case throws`() = runTest {
        every { getMonthlyRecap(any(), any()) } returns flowOf(emptyRecap())
        val viewModel = createViewModel()
        every { getMonthlyRecap(2023, 12) } returns flow { throw RuntimeException("fail") }

        viewModel.loadRecap(2023, 12)

        assertTrue(viewModel.state.value.isError)
    }

    @Test
    fun `loadRecap sets isLoading false on error`() = runTest {
        every { getMonthlyRecap(any(), any()) } returns flowOf(emptyRecap())
        val viewModel = createViewModel()
        every { getMonthlyRecap(2023, 12) } returns flow { throw RuntimeException("fail") }

        viewModel.loadRecap(2023, 12)

        assertFalse(viewModel.state.value.isLoading)
    }

    // helpers

    private fun createViewModel() = RecapViewModel(getMonthlyRecap, crashReporter)

    private fun emptyRecap(totalSteps: Int = 0) = MonthlyRecap(
        year = YearMonth.now().minusMonths(1).year,
        month = YearMonth.now().minusMonths(1).monthValue,
        totalSteps = totalSteps,
        averageStepsPerDay = 0,
        bestDay = null,
        achievedDays = 0,
        totalDays = 30,
        longestStreak = 0,
        activeDays = 0,
        estimatedCalories = 0,
        dailyCounts = emptyList(),
    )
}
