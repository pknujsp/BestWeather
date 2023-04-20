package com.lifedawn.bestweather.data.remote.weather.kma.parser.model

data class ParsedKmaCurrentConditions(
    val baseDateTimeISO8601: String = "",
    val temp: String = "",
    val yesterdayTemp: String = "",
    val feelsLikeTemp: String = "",
    val humidity: String = "",
    val windDirection: String = "",
    val windSpeed: String = "",
    val precipitationVolume: String = "",
    val pty: String = ""
)