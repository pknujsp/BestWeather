package com.lifedawn.bestweather.data.remote.weather.metnorway.repository

import com.lifedawn.bestweather.data.remote.weather.commons.model.WeatherDataDto
import kotlinx.coroutines.flow.Flow
import java.time.ZoneId

interface MetNorwayRepository {
    fun getWeatherData(latitude: Double, longitude: Double, zoneId: ZoneId): Flow<WeatherDataDto>
}