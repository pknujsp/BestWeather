package com.lifedawn.bestweather.data.remote.weather.commons

import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.local.weather.models.CurrentConditionsDto
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto

interface WeatherRepository {
    suspend fun getCurrentConditions(
        weatherProviderTypes: Set<WeatherProviderType>,
        latitude: Double,
        longitude: Double
    ): CurrentConditionsDto

    suspend fun getHourlyForecasts(
        weatherProviderTypes: Set<WeatherProviderType>,
        latitude: Double,
        longitude: Double
    ): List<HourlyForecastDto>

    suspend fun getDailyForecasts(
        weatherProviderTypes: Set<WeatherProviderType>,
        latitude: Double,
        longitude: Double
    ): List<DailyForecastDto>

    suspend fun getAirQuality()
}