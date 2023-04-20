package com.lifedawn.bestweather.data.remote.weather.owm.usecase

import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.data.remote.weather.commons.model.WeatherDataDto
import com.lifedawn.bestweather.data.remote.weather.owm.repository.OwmWeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.ZoneId
import javax.inject.Inject

class GetOwmWeatherUseCaseImpl @Inject constructor(private val owmWeatherRepository: OwmWeatherRepository) : GetOwmWeatherUseCase {
    override suspend fun getWeatherData(
        weatherDataTypes: Set<WeatherDataType>,
        latitude: Double,
        longitude: Double,
        zoneId: ZoneId
    ): Flow<WeatherDataDto> = flow {
        owmWeatherRepository.getWeatherData(weatherDataTypes, latitude, longitude, zoneId).collect {
            emit(it)
        }
    }

}