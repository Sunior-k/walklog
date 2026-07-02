package com.river.walklog.core.data.healthconnect

import androidx.health.connect.client.HealthConnectClient
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Health Connect 계약 테스트 — 실기기(Health Connect 탑재)에서만 실행됩니다.
 *
 * SDK_UNAVAILABLE 기기에서는 Assume으로 건너뜁니다.
 * CI에서는 skip되므로 일반 빌드를 막지 않습니다.
 */
@RunWith(AndroidJUnit4::class)
class HealthConnectStepDataSourceTest {

    private lateinit var dataSource: HealthConnectStepDataSource

    @Before
    fun setUp() {
        dataSource = HealthConnectStepDataSource(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun isAvailable_returnsValidSdkStatus() {
        val status = HealthConnectClient.getSdkStatus(ApplicationProvider.getApplicationContext())
        assertTrue(
            status == HealthConnectClient.SDK_AVAILABLE ||
                status == HealthConnectClient.SDK_UNAVAILABLE ||
                status == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED,
            "Unexpected SDK status: $status",
        )
    }

    @Test
    fun isAvailable_matchesSdkStatus() {
        val expected = HealthConnectClient.getSdkStatus(ApplicationProvider.getApplicationContext()) ==
            HealthConnectClient.SDK_AVAILABLE
        assertTrue(dataSource.isAvailable() == expected)
    }

    @Test
    fun readDailySteps_returnsNonNegativeValue_whenAvailable() = runBlocking {
        assumeTrue(
            "Health Connect SDK not available on this device",
            dataSource.isAvailable(),
        )

        val steps = dataSource.readDailySteps(LocalDate.now())
        assertFalse(steps < 0, "Steps should be non-negative, got $steps")
    }

    @Test
    fun readHourlySteps_returnsSizedArray_whenAvailable() = runBlocking {
        assumeTrue(
            "Health Connect SDK not available on this device",
            dataSource.isAvailable(),
        )

        val from = LocalDate.now().minusDays(6)
        val to = LocalDate.now()
        val hourlySteps = dataSource.readHourlySteps(from, to)

        assertTrue(hourlySteps.size == 7 * 24, "Expected ${7 * 24} hourly slots, got ${hourlySteps.size}")
        assertTrue(hourlySteps.all { it >= 0f }, "All hourly values should be non-negative")
    }
}
