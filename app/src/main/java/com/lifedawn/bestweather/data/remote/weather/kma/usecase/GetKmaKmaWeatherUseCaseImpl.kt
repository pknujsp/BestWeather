package com.lifedawn.bestweather.data.remote.weather.kma.usecase

import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.data.remote.weather.commons.model.WeatherDataDto
import com.lifedawn.bestweather.data.remote.weather.kma.repository.KmaWeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.ZoneId
import javax.inject.Inject

class GetKmaKmaWeatherUseCaseImpl @Inject constructor(private val kmaWeatherRepository: KmaWeatherRepository) : GetKmaWeatherUseCase {

    override suspend fun getWeatherData(
        weatherDataTypes: Set<WeatherDataType>,
        areaCode: String,
        latitude: Double,
        longitude: Double,
        zoneId: ZoneId
    ): Flow<WeatherDataDto> = flow {
        kmaWeatherRepository.getWeatherData(weatherDataTypes, areaCode, latitude, longitude).collect {
            emit(it)
        }
    }

}