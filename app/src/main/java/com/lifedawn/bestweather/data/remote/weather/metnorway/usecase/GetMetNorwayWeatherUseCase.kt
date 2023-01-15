package com.lifedawn.bestweather.data.remote.weather.metnorway.usecase

import com.lifedawn.bestweather.data.remote.weather.commons.model.WeatherDataDto
import kotlinx.coroutines.flow.Flow
import java.time.ZoneId

interface GetMetNorwayWeatherUseCase {
    suspend fun getWeatherData(
        latitude: Double,
        longitude: Double,
        zoneId: ZoneId
    ): Flow<WeatherDataDto>
}