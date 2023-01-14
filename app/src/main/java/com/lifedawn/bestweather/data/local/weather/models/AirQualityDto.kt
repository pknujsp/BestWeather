package com.lifedawn.bestweather.data.local.weather.models

import java.time.ZonedDateTime

data class AirQualityDto(
    val aqi: Int = -1,
    val idx: Int = -1,
    val latitude: Double = -1.0,
    val longitude: Double = -1.0,
    val cityName: String = "",
    val aqiCnUrl: String = "",
    val time: ZonedDateTime = ZonedDateTime.now(),
    val current: Current? = null,
    val timeInfo: Time? = null,
    val dailyForecastList: List<DailyForecast> = emptyList(),
    val isSuccessful: Boolean = false
) {

    data class Current(
        val co: Int = -1,
        val isHasCo: Boolean = false,
        val dew: Int = -1,
        val isHasDew: Boolean = false,
        val no2: Int = -1,
        val isHasNo2: Boolean = false,
        val o3: Int = -1,
        val isHasO3: Boolean = false,
        val pm10: Int = -1,
        val isHasPm10: Boolean = false,
        val pm25: Int = -1,
        val isHasPm25: Boolean = false,
        val so2: Int = -1,
        val isHasSo2: Boolean = false
    )

    data class DailyForecast(
        val date: ZonedDateTime = ZonedDateTime.now(),
        val isHasO3: Boolean = false,
        val isHasPm10: Boolean = false,
        val isHasPm25: Boolean = false,
        val isHasUvi: Boolean = false,
        val o3: Val? = null,
        val pm10: Val? = null,
        val pm25: Val? = null,
        val uvi: Val? = null
    ) {
        data class Val(
            val max: Int = -1,
            val min: Int = -1,
            val avg: Int = -1
        )
    }

    data class Time(
        val v: String = "",
        val s: String = "",
        val tz: String = "",
        val iso: String = ""
    )
}