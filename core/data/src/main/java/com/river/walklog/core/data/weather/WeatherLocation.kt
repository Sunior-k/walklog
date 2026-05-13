package com.river.walklog.core.data.weather

import java.util.Locale

/**
 * 날씨 조회에 사용할 위치 정보.
 *
 * @param latitude  위도 (WGS84)
 * @param longitude 경도 (WGS84)
 * @param displayName UI에 표시할 지역명
 * @param countryCode 국가 코드
 */
data class WeatherLocation(
    val latitude: Double,
    val longitude: Double,
    val displayName: String,
    val countryCode: String,
)

interface WeatherLocationProvider {
    suspend fun currentLocation(): WeatherLocation
}

/**
 * 디바이스 로케일 기반 기본 위치.
 * GPS 사용 불가 시 fallback.
 */
class LocaleWeatherLocationProvider : WeatherLocationProvider {
    override suspend fun currentLocation(): WeatherLocation =
        when (Locale.getDefault().country.uppercase(Locale.ROOT)) {
            "KR" -> SEOUL
            "JP" -> TOKYO
            "US" -> NEW_YORK
            else -> SEOUL
        }

    companion object {
        val SEOUL = WeatherLocation(
            latitude = 37.5665,
            longitude = 126.9780,
            displayName = "서울",
            countryCode = "KR",
        )
        val TOKYO = WeatherLocation(
            latitude = 35.6762,
            longitude = 139.6503,
            displayName = "東京",
            countryCode = "JP",
        )
        val NEW_YORK = WeatherLocation(
            latitude = 40.7128,
            longitude = -74.0060,
            displayName = "New York",
            countryCode = "US",
        )
    }
}
