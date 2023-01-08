package com.lifedawn.bestweather.data.remote.weather.kma.parser.model


data class ParsedKmaDailyForecast(
    val dateISO8601: String = "",
    val isSingle: Boolean = false,
    val amValues: Values? = null,
    val pmValues: Values? = null,
    val singleValues: Values? = null,
    val minTemp: String = "",
    val maxTemp: String = ""
) {
    data class Values(
        var weatherDescription: String = "",
        var pop: String = ""
    )

}