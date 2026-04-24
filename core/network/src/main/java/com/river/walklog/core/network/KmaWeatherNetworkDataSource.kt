package com.river.walklog.core.network

import com.river.walklog.core.network.model.NetworkWeatherCondition
import com.river.walklog.core.network.model.NetworkWeatherSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject

class KmaWeatherNetworkDataSource @Inject constructor(
    private val okHttpClient: OkHttpClient,
) : WeatherNetworkDataSource {

    override suspend fun getCurrentWeather(
        nx: Int,
        ny: Int,
        locationName: String,
    ): NetworkWeatherSummary = withContext(Dispatchers.IO) {
        val serviceKey = BuildConfig.KMA_SERVICE_KEY
        if (serviceKey.isBlank()) return@withContext NetworkWeatherSummary.unavailable(locationName)

        val base = KmaForecastTime.latestUltraShortForecastBase()
        val urlBuilder = KMA_BASE_URL.toHttpUrl()
            .newBuilder()
            .addPathSegment("getUltraSrtFcst")
            .addQueryParameter("pageNo", "1")
            .addQueryParameter("numOfRows", "60")
            .addQueryParameter("dataType", "JSON")
            .addQueryParameter("base_date", base.baseDate)
            .addQueryParameter("base_time", base.baseTime)
            .addQueryParameter("nx", nx.toString())
            .addQueryParameter("ny", ny.toString())

        if (serviceKey.contains("%")) {
            urlBuilder.addEncodedQueryParameter("serviceKey", serviceKey)
        } else {
            urlBuilder.addQueryParameter("serviceKey", serviceKey)
        }

        val request = Request.Builder().url(urlBuilder.build()).get().build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext NetworkWeatherSummary.unavailable(locationName)
            val body = response.body?.string().orEmpty()
            runCatching { parseWeatherSummary(body, locationName) }
                .getOrElse { NetworkWeatherSummary.unavailable(locationName) }
        }
    }

    private fun parseWeatherSummary(body: String, locationName: String): NetworkWeatherSummary {
        val response = JSONObject(body).getJSONObject("response")
        val header = response.getJSONObject("header")
        if (header.optString("resultCode") != "00") return NetworkWeatherSummary.unavailable(locationName)

        val items = response
            .getJSONObject("body")
            .getJSONObject("items")
            .getJSONArray("item")

        val forecasts = buildMap<String, MutableMap<String, String>> {
            for (index in 0 until items.length()) {
                val item = items.getJSONObject(index)
                val key = item.getString("fcstDate") + item.getString("fcstTime")
                val category = item.getString("category")
                val value = item.getString("fcstValue")
                getOrPut(key) { mutableMapOf() }[category] = value
            }
        }

        val selected = forecasts.entries
            .sortedBy { it.key }
            .firstOrNull { (_, values) ->
                values.containsKey("T1H") || values.containsKey("SKY") || values.containsKey("PTY")
            }
            ?.value
            ?: return NetworkWeatherSummary.unavailable(locationName)

        val sky = selected["SKY"]?.toIntOrNull()
        val precipitationType = selected["PTY"]?.toIntOrNull()

        return NetworkWeatherSummary(
            locationName = locationName,
            temperatureCelsius = selected["T1H"]?.toFloatOrNull()?.toInt(),
            condition = mapCondition(sky = sky, precipitationType = precipitationType),
            precipitationProbability = selected["POP"]?.toIntOrNull(),
            humidity = selected["REH"]?.toIntOrNull(),
            windSpeedMetersPerSecond = selected["WSD"]?.toFloatOrNull(),
        )
    }

    private fun mapCondition(
        sky: Int?,
        precipitationType: Int?,
    ): NetworkWeatherCondition = when (precipitationType) {
        1, 5 -> NetworkWeatherCondition.RAIN
        2, 6 -> NetworkWeatherCondition.RAIN_SNOW
        3, 7 -> NetworkWeatherCondition.SNOW
        4 -> NetworkWeatherCondition.SHOWER
        else -> when (sky) {
            1 -> NetworkWeatherCondition.CLEAR
            3 -> NetworkWeatherCondition.PARTLY_CLOUDY
            4 -> NetworkWeatherCondition.CLOUDY
            else -> NetworkWeatherCondition.UNKNOWN
        }
    }

    companion object {
        private const val KMA_BASE_URL = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0"
    }
}
