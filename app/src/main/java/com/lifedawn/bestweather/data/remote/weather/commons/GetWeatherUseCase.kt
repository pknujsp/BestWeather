package com.lifedawn.bestweather.data.remote.weather.commons

import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.local.weather.models.CurrentConditionsDto
import javax.inject.Inject

class GetWeatherUseCase @Inject constructor(private val weatherRepository: WeatherRepository) {
    suspend fun getCurrentConditions(
        weatherProviderTypes: Set<WeatherProviderType>,
        latitude: Double,
        longitude: Double
    ): CurrentConditionsDto {
        return weatherRepository.getCurrentConditions(weatherProviderTypes, latitude, longitude)
    }
}