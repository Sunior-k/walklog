package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.model.WeeklyStepSummary
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetWeeklyBestHourUseCaseTest {

    private lateinit var repository: StepRepository
    private lateinit var useCase: GetWeeklyBestHourUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetWeeklyBestHourUseCase(repository)
    }

    @Test
    fun `bestHour uses hourly steps from selected week only`() = runTest {
        val summary = summary()
        val hourlySteps = FloatArray(7 * 24)
        hourlySteps[15] = 100f
        hourlySteps[24 + 15] = 300f
        hourlySteps[18] = 250f
        coEvery {
            repository.getHourlyStepsForRange(19_000L, 19_006L)
        } returns hourlySteps

        val bestHour = useCase(summary)

        assertEquals(15, bestHour)
    }

    private fun summary(
        steps: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7),
    ) = WeeklyStepSummary(
        weekStartEpochDay = 19_000L,
        dailyCounts = steps.mapIndexed { index, stepCount ->
            DailyStepCount(
                dateEpochDay = 19_000L + index,
                steps = stepCount,
                targetSteps = 6_000,
            )
        },
    )
}
