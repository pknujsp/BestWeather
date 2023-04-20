package com.lifedawn.bestweather.data.remote.weather.metnorway.usecase

import com.lifedawn.bestweather.data.remote.weather.commons.model.WeatherDataDto
import com.lifedawn.bestweather.data.remote.weather.metnorway.repository.MetNorwayRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.ZoneId
import javax.inject.Inject

class GetMetNorwayWeatherUseCaseImpl @Inject constructor(private val metNorwayRepository: MetNorwayRepository) :
    GetMetNorwayWeatherUseCase {
    override suspend fun getWeatherData(
        latitude: Double,
        longitude: Double,
        zoneId: ZoneId
    ): Flow<WeatherDataDto> = flow {
        metNorwayRepository.getWeatherData(latitude, longitude, zoneId).collect {
            emit(it)
        }
    }

}