package com.lifedawn.bestweather.data.remote.weather.commons

import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import com.lifedawn.bestweather.data.local.weather.models.CurrentConditionsDto
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun getCurrentConditions(
        latitude: Double,
        longitude: Double
    ): Flow<ApiResponse<CurrentConditionsDto>>

    fun getHourlyForecasts(
        latitude: Double,
        longitude: Double
    ): Flow<ApiResponse<List<HourlyForecastDto>>>

    fun getDailyForecasts(
        latitude: Double,
        longitude: Double
    ): Flow<ApiResponse<List<DailyForecastDto>>>


}