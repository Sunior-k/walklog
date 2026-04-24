package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.WeatherRepository
import com.river.walklog.core.model.WeatherSummary
import javax.inject.Inject

class GetCurrentWeatherUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): WeatherSummary =
        weatherRepository.getCurrentWeather(forceRefresh = forceRefresh)
}
