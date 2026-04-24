package com.river.walklog.core.network

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal data class KmaBaseDateTime(
    val baseDate: String,
    val baseTime: String,
)

internal object KmaForecastTime {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HHmm")

    fun latestUltraShortForecastBase(now: LocalDateTime = LocalDateTime.now()): KmaBaseDateTime {
        val base = if (now.minute < 45) {
            now.minusHours(1).withMinute(30)
        } else {
            now.withMinute(30)
        }.withSecond(0).withNano(0)

        return KmaBaseDateTime(
            baseDate = base.format(dateFormatter),
            baseTime = base.format(timeFormatter),
        )
    }
}
