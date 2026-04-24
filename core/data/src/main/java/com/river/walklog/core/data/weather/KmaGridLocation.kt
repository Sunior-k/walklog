package com.river.walklog.core.data.weather

/**
 * 기상청 격자 위치 정보
 *
 * @param nx 격자 X 좌표
 * @param ny 격자 Y 좌표
 * @param name 위치 이름
 */

data class KmaGridLocation(
    val nx: Int,
    val ny: Int,
    val name: String,
)

interface WeatherLocationProvider {
    fun currentLocation(): KmaGridLocation
}

class DefaultWeatherLocationProvider : WeatherLocationProvider {
    override fun currentLocation(): KmaGridLocation = SEOUL

    companion object {
        val SEOUL = KmaGridLocation(nx = 60, ny = 127, name = "서울")
    }
}
