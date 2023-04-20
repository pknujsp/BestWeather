package com.lifedawn.bestweather.data.remote.weather.commons.model

import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto
import com.lifedawn.bestweather.data.local.weather.models.CurrentConditionsDto
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto

data class WeatherDataDto(
    val weatherDataTypes: Set<WeatherDataType> = emptySet()
) {
    var currentConditions: CurrentConditionsDto? = null
    var hourlyForecasts: List<HourlyForecastDto> = emptyList()
    var dailyForecasts: List<DailyForecastDto> = emptyList()
    var airQuality: AirQualityDto? = null
}