package com.lifedawn.bestweather.data.remote.weather.kma.repository

import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.data.remote.weather.commons.model.WeatherDataDto
import kotlinx.coroutines.flow.Flow

interface KmaWeatherRepository {
    fun getWeatherData(
        weatherDataTypes: Set<WeatherDataType>,
        areaCode: String,
        latitude: Double,
        longitude: Double
    ): Flow<WeatherDataDto>

}