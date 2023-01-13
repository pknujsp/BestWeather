package com.lifedawn.bestweather.data.remote.weather.commons

import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto
import com.lifedawn.bestweather.data.local.weather.models.CurrentConditionsDto
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    suspend fun getCurrentConditions(
        weatherProviderType: WeatherProviderType,
        latitude: Double,
        longitude: Double
    ): Flow<ApiResponse<CurrentConditionsDto>>

    suspend fun getHourlyForecasts(
        weatherProviderTypes: WeatherProviderType,
        latitude: Double,
        longitude: Double
    ): Flow<ApiResponse<List<HourlyForecastDto>>>

    suspend fun getDailyForecasts(
        weatherProviderTypes: WeatherProviderType,
        latitude: Double,
        longitude: Double
    ): Flow<ApiResponse<List<DailyForecastDto>>>

    suspend fun getAirQuality(
        latitude: Double,
        longitude: Double
    ): Flow<ApiResponse<AirQualityDto>>
}