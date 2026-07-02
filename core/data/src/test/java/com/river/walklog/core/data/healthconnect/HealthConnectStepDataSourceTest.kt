package com.river.walklog.core.data.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.aggregate.AggregationResult
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateGroupByDurationRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.response.AggregateGroupByDurationResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.util.TimeZone
import kotlin.test.assertEquals

class HealthConnectStepDataSourceTest {

    private val context: Context = mockk(relaxed = true)
    private val client: HealthConnectClient = mockk()
    private lateinit var dataSource: HealthConnectStepDataSource

    @Before
    fun setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        dataSource = HealthConnectStepDataSource(context, client)
    }

    @Test
    fun `readDailySteps returns step count from aggregate response`() = runTest {
        val result = mockk<AggregationResult>()
        every { result[StepsRecord.COUNT_TOTAL] } returns 1234L
        coEvery { client.aggregate(any<AggregateRequest>()) } returns result

        assertEquals(1234, dataSource.readDailySteps(LocalDate.of(2025, 1, 1)))
    }

    @Test
    fun `readDailySteps returns 0 when COUNT_TOTAL is null`() = runTest {
        val result = mockk<AggregationResult>()
        every { result[StepsRecord.COUNT_TOTAL] } returns null
        coEvery { client.aggregate(any<AggregateRequest>()) } returns result

        assertEquals(0, dataSource.readDailySteps(LocalDate.of(2025, 1, 1)))
    }

    @Test
    fun `readHourlySteps returns array sized days times 24`() = runTest {
        coEvery { client.aggregateGroupByDuration(any<AggregateGroupByDurationRequest>()) } returns emptyList()

        val result = dataSource.readHourlySteps(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 7))

        assertEquals(7 * 24, result.size)
    }

    @Test
    fun `readHourlySteps maps bucket steps to correct hourly slot`() = runTest {
        val from = LocalDate.of(2025, 1, 1)
        val aggregationResult = mockk<AggregationResult>()
        every { aggregationResult[StepsRecord.COUNT_TOTAL] } returns 500L

        val bucket = mockk<AggregateGroupByDurationResponse>()
        every { bucket.startTime } returns Instant.parse("2025-01-01T03:00:00Z")
        every { bucket.result } returns aggregationResult

        coEvery { client.aggregateGroupByDuration(any<AggregateGroupByDurationRequest>()) } returns listOf(bucket)

        val result = dataSource.readHourlySteps(from, from)

        assertEquals(500f, result[3])
    }

    @Test
    fun `readHourlySteps initializes all slots to zero when no buckets`() = runTest {
        coEvery { client.aggregateGroupByDuration(any<AggregateGroupByDurationRequest>()) } returns emptyList()

        val result = dataSource.readHourlySteps(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 1))

        assert(result.all { it == 0f })
    }
}
