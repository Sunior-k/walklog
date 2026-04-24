package com.river.walklog.core.model

data class WeatherSummary(
    val locationName: String,
    val temperatureCelsius: Int?,
    val condition: WeatherCondition,
    val precipitationProbability: Int?,
    val humidity: Int?,
    val windSpeedMetersPerSecond: Float?,
    val isAvailable: Boolean = true,
) {
    val temperatureText: String
        get() = temperatureCelsius?.let { "$it°" } ?: "-"

    val conditionText: String
        get() = condition.label

    val walkingAdvice: String
        get() = when {
            !isAvailable -> "날씨 정보를 불러오지 못했어요"
            condition == WeatherCondition.SNOW -> "눈 예보가 있어 미끄럼에 주의하세요"
            condition == WeatherCondition.RAIN -> "비 예보가 있어 우산을 챙기세요"
            condition == WeatherCondition.SHOWER -> "소나기 가능성이 있어 짧게 걸어보세요"
            windSpeedMetersPerSecond != null && windSpeedMetersPerSecond >= 8f -> "바람이 강해 실내 걷기도 좋아요"
            temperatureCelsius != null && temperatureCelsius <= 0 -> "추운 날씨예요. 따뜻하게 입고 걸어보세요"
            temperatureCelsius != null && temperatureCelsius >= 30 -> "더운 날씨예요. 물을 챙기고 무리하지 마세요"
            condition == WeatherCondition.CLEAR -> "맑은 날씨예요. 가볍게 걷기 좋아요"
            else -> "오늘 날씨를 확인하고 걷기 계획을 세워보세요"
        }

    companion object {
        fun unavailable(locationName: String = "서울"): WeatherSummary = WeatherSummary(
            locationName = locationName,
            temperatureCelsius = null,
            condition = WeatherCondition.UNKNOWN,
            precipitationProbability = null,
            humidity = null,
            windSpeedMetersPerSecond = null,
            isAvailable = false,
        )
    }
}

enum class WeatherCondition(val label: String) {
    CLEAR("맑음"),
    PARTLY_CLOUDY("구름많음"),
    CLOUDY("흐림"),
    RAIN("비"),
    RAIN_SNOW("비/눈"),
    SNOW("눈"),
    SHOWER("소나기"),
    UNKNOWN("정보 없음"),
}
