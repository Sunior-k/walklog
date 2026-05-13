package com.river.walklog.core.network

import com.river.walklog.core.common.dispatcher.WalkLogDispatchers
import com.river.walklog.core.network.model.NetworkWeatherCondition
import com.river.walklog.core.network.model.NetworkWeatherSummary
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject

/**
 * Open-Meteo 기반 날씨 DataSource.
 * KMA 대신 사용.
 * 참고: https://open-meteo.com/en/docs
 */
class OpenMeteoWeatherNetworkDataSource @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val dispatchers: WalkLogDispatchers,
) : WeatherNetworkDataSource {

    override suspend fun getCurrentWeather(
        latitude: Double,
        longitude: Double,
        locationName: String,
    ): NetworkWeatherSummary = withContext(dispatchers.io) {
        val url = BASE_URL.toHttpUrl().newBuilder()
            .addQueryParameter("latitude", latitude.toString())
            .addQueryParameter("longitude", longitude.toString())
            .addQueryParameter(
                "current",
                "temperature_2m,relative_humidity_2m,precipitation_probability,weather_code,wind_speed_10m",
            )
            .addQueryParameter("wind_speed_unit", "ms")
            .build()

        val request = Request.Builder().url(url).get().build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext NetworkWeatherSummary.unavailable(locationName)
            val body = response.body?.string().orEmpty()
            runCatching { parseWeatherSummary(body, locationName) }
                .getOrElse { NetworkWeatherSummary.unavailable(locationName) }
        }
    }

    private fun parseWeatherSummary(body: String, locationName: String): NetworkWeatherSummary {
        val current = JSONObject(body).getJSONObject("current")

        val temperatureRaw = current.optDouble("temperature_2m", Double.NaN)
        val windSpeedRaw = current.optDouble("wind_speed_10m", Double.NaN)

        return NetworkWeatherSummary(
            locationName = locationName,
            temperatureCelsius = temperatureRaw.takeUnless { it.isNaN() }?.toInt(),
            condition = mapWmoCode(current.optInt("weather_code", -1)),
            precipitationProbability = current.optInt("precipitation_probability").takeIf { it >= 0 },
            humidity = current.optInt("relative_humidity_2m").takeIf { it >= 0 },
            windSpeedMetersPerSecond = windSpeedRaw.takeUnless { it.isNaN() }?.toFloat(),
        )
    }

    /**
     * WMO 날씨 코드(weather_code)를 공통 WeatherCondition으로 변환.
     * 코드 정의: https://open-meteo.com/en/docs#weathervariables
     */
    private fun mapWmoCode(code: Int): NetworkWeatherCondition = when (code) {
        0, 1 -> NetworkWeatherCondition.CLEAR
        2 -> NetworkWeatherCondition.PARTLY_CLOUDY
        3, 45, 48 -> NetworkWeatherCondition.CLOUDY
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> NetworkWeatherCondition.RAIN
        56, 57, 66, 67 -> NetworkWeatherCondition.RAIN_SNOW
        71, 73, 75, 77, 85, 86 -> NetworkWeatherCondition.SNOW
        95, 96, 99 -> NetworkWeatherCondition.SHOWER
        else -> NetworkWeatherCondition.UNKNOWN
    }

    companion object {
        private const val BASE_URL = "https://api.open-meteo.com/v1/forecast"
    }
}
