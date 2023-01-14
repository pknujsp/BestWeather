package com.lifedawn.bestweather.data.local.weather.models

import java.time.ZonedDateTime

data class HourlyForecastDto(
    val hours: ZonedDateTime = ZonedDateTime.now(),

    val weatherIcon: Int = -1,

    val weatherDescription: String = "",

    val feelsLikeTemp: String = "",

    val temp: String = "",

    val pop: String = "",

    val pos: String = "",

    val por: String = "",

    val windDirection: String = "",

    val windDirectionVal: Int = -1,

    val windSpeed: String = "",

    val windStrength: String = "",

    val windGust: String = "",

    val pressure: String = "",

    val humidity: String = "",

    val dewPointTemp: String = "",

    val cloudiness: String = "",

    val visibility: String = "",

    val uvIndex: String = "",

    val precipitationVolume: String = "",

    val rainVolume: String = "",

    val snowVolume: String = "",

    val precipitationType: String = "",

    val precipitationTypeIcon: Int = -1,

    val isHasThunder: Boolean = false,

    val isHasPrecipitation: Boolean = false,

    val isHasNext6HoursPrecipitation: Boolean = false,

    val isHasRain: Boolean = false,

    val isHasSnow: Boolean = false,

    val isHasPor: Boolean = false,
    val isHasPos: Boolean = false
)