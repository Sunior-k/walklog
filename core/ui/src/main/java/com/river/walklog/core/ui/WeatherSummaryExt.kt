package com.river.walklog.core.ui

import com.river.walklog.core.model.WeatherSummary

val WeatherSummary.temperatureText: String
    get() = temperatureCelsius?.let { "$it°" } ?: "-"
