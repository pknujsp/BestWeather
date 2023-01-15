package com.lifedawn.bestweather.data.remote.weather.kma.usecase

import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto
import com.lifedawn.bestweather.data.local.weather.models.CurrentConditionsDto
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto
import com.lifedawn.bestweather.data.remote.weather.kma.repository.KmaWeatherRepository
import javax.inject.Inject

class GetKmaWeatherUseCase @Inject constructor(private val kmaWeatherRepository: KmaWeatherRepository) {
    suspend fun getCurrentConditions(
        weatherProviderTypes: Set<WeatherProviderType>,
        latitude: Double,
        longitude: Double
    ): CurrentConditionsDto {
        return weatherRepository.getCurrentConditions(latitude, longitude)
    }

    suspend fun getHourlyForecasts(
        weatherProviderTypes: Set<WeatherProviderType>,
        latitude: Double,
        longitude: Double
    ): List<HourlyForecastDto> {
        return weatherRepository.getHourlyForecasts(latitude, longitude)
    }

    suspend fun getDailyForecasts(
        weatherProviderTypes: Set<WeatherProviderType>,
        latitude: Double,
        longitude: Double
    ): List<DailyForecastDto> {
        return weatherRepository.getDailyForecasts(latitude, longitude)
    }

}