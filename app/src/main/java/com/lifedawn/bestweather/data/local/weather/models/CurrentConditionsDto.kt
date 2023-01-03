package com.lifedawn.bestweather.data.local.weather.models

import java.time.ZonedDateTime

data class CurrentConditionsDto(
    val temp: String,
    val minTemp: String,
    val maxTemp: String,
    val feelsLikeTemp: String,
    val weatherIconId: Int,
    val weatherDescription: String,
    val humidity: String,
    val dewPoint: String,
    val windDirection: String,
    val windDirectionDegree: Int,
    val windSpeed: String,
    val windStrength: String,
    val simpleWindStrength: String,
    val windGust: String,
    val pressure: String,
    val uvIndex: String,
    val visibility: String,
    val cloudiness: String,
    val currentTime: ZonedDateTime,
    val tempYesterday: String,
    val precipitationType: String,
    var hasRainVolume: Boolean,
    var hasSnowVolume: Boolean,
    var hasPrecipitationVolume: Boolean
) {

    var rainVolume: String = ""
        get() = this.toString()
        set(value) {
            field = value
            hasRainVolume = true
        }

    var snowVolume: String = ""
        get() = this.toString()
        set(value) {
            field = value
            hasSnowVolume = true
        }

    var precipitationVolume: String = ""
        get() = this.toString()
        set(value) {
            field = value
            hasPrecipitationVolume = true
        }

}
