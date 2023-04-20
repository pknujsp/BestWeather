package com.lifedawn.bestweather.data.remote.weather.kma.usecase

import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.data.remote.weather.commons.model.WeatherDataDto
import kotlinx.coroutines.flow.Flow
import java.time.ZoneId

interface GetKmaWeatherUseCase {
    suspend fun getWeatherData(
        weatherDataTypes: Set<WeatherDataType>,
        areaCode: String,
        latitude: Double,
        longitude: Double,
        zoneId: ZoneId
    ): Flow<WeatherDataDto>
}