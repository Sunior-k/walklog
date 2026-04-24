package com.river.walklog.core.data.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateGroupByDurationRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google Health Connect 걸음 수 데이터 소스.
 *
 * Health Connect는 기기의 건강 데이터 저장소로, OS(Android 14+) 또는
 * Samsung Health 등의 앱이 센서 데이터를 자동으로 기록한다.
 * WalkLog는 해당 데이터를 READ-ONLY로 읽어 걸음 수를 표시한다.
 *
 * References:
 *   - Health Connect 가이드: https://developer.android.com/health-and-fitness/guides/health-connect
 *   - StepsRecord API: https://developer.android.com/reference/kotlin/androidx/health/connect/client/records/StepsRecord
 */
@Singleton
class HealthConnectStepDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val client: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    fun isAvailable(): Boolean =
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    /**
     * 특정 날짜의 총 걸음 수를 집계하여 반환한다.
     *
     * Health Connect의 [AggregateRequest]는 중복 제거(deduplication)를 적용하므로
     * 복수의 앱이 동일 구간을 기록해도 정확한 단일 값을 반환한다.
     */
    suspend fun readDailySteps(date: LocalDate): Int {
        val zone = ZoneId.systemDefault()
        val startOfDay = date.atStartOfDay(zone).toInstant()
        val endOfDay = date.plusDays(1).atStartOfDay(zone).toInstant()
        val response = client.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay),
            ),
        )
        return (response[StepsRecord.COUNT_TOTAL] ?: 0L).toInt()
    }

    /**
     * 날짜 범위에 대해 시간별(hourly) 걸음 수 배열을 반환한다.
     *
     * 반환값: FloatArray[days × 24]. 인덱스 = dayOffset * 24 + hour.
     * [AggregateGroupByDurationRequest]로 1시간 단위 버킷을 생성한다.
     */
    suspend fun readHourlySteps(from: LocalDate, to: LocalDate): FloatArray {
        val zone = ZoneId.systemDefault()
        val startInstant = from.atStartOfDay(zone).toInstant()
        val endInstant = to.plusDays(1).atStartOfDay(zone).toInstant()
        val days = (to.toEpochDay() - from.toEpochDay() + 1).toInt()
        val result = FloatArray(days * 24)

        val buckets = client.aggregateGroupByDuration(
            AggregateGroupByDurationRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(startInstant, endInstant),
                timeRangeSlicer = Duration.ofHours(1),
            ),
        )

        for (bucket in buckets) {
            val localDateTime = bucket.startTime.atZone(zone).toLocalDateTime()
            val dayOffset = (localDateTime.toLocalDate().toEpochDay() - from.toEpochDay()).toInt()
            val hour = localDateTime.hour
            if (dayOffset in 0 until days) {
                result[dayOffset * 24 + hour] +=
                    (bucket.result[StepsRecord.COUNT_TOTAL] ?: 0L).toFloat()
            }
        }

        return result
    }
}
