package com.lifedawn.bestweather.data.remote.weather.commons

interface WeatherRepository {
    suspend fun getCurrentConditions()
    suspend fun getHourlyForecasts()
    suspend fun getDailyForecasts()
    suspend fun getAirQuality()
}