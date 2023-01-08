package com.lifedawn.bestweather.data.remote.weather.kma.parser.model

data class ParsedKmaCurrentConditions(
    val baseDateTimeISO8601: String? = null,
    val temp: String? = null,
    val yesterdayTemp: String? = null,
    val feelsLikeTemp: String? = null,
    val humidity: String? = null,
    val windDirection: String? = null,
    val windSpeed: String? = null,
    val precipitationVolume: String? = null,
    val pty: String? = null
)