package com.lifedawn.bestweather.data.local.weather.models

import com.lifedawn.bestweather.data.remote.weather.kma.parser.model.ParsedKmaDailyForecast
import java.time.ZonedDateTime

data class DailyForecastDto(
    var date: ZonedDateTime = ZonedDateTime.now(),
    val valuesList: List<Values> = emptyList(),
    var minTemp: String = "",
    var maxTemp: String = "",
    var minFeelsLikeTemp: String = "",
    var maxFeelsLikeTemp: String = "",
    var isAvailable_toMakeMinMaxTemp: Boolean = true,
    var isHaveOnly1HoursForecast: Boolean = false
) {
    data class Values(
        var weatherIcon: Int = -1,
        var weatherDescription: String = "",
        var dateTime: ZonedDateTime = ZonedDateTime.now(),
        var pop: String = "",
        var pos: String = "",
        var por: String = "",
        var precipitationVolume: String = "",

        var rainVolume: String = "",

        var snowVolume: String = "",

        var minTemp: String = "",

        var maxTemp: String = "",

        var temp: String = "",

        var windDirection: String = "",

        var windDirectionVal: Int = -1,
        var windSpeed: String = "",

        var windStrength: String = "",

        var windGust: String = "",

        var pressure: String = "",

        var humidity: String = "",

        var dewPointTemp: String = "",

        var cloudiness: String = "",

        var visibility: String = "",

        var uvIndex: String = "",

        var precipitationType: String = "",

        var precipitationTypeIcon: Int = -1,

        var precipitationNextHoursAmount: Int = -1,

        var isHasRainVolume: Boolean = false,

        var isHasSnowVolume: Boolean = false,

        var isHasPrecipitationVolume: Boolean = false,

        var isHasPop: Boolean = false,

        var isHasPrecipitationNextHoursAmount: Boolean = false
    )
}