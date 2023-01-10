package com.lifedawn.bestweather.data.remote.weather.commons

import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto
import com.lifedawn.bestweather.data.local.weather.models.CurrentConditionsDto
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto
import javax.inject.Inject

class GetWeatherUseCase @Inject constructor(private val weatherRepository: WeatherRepository) {
    suspend fun getCurrentConditions(
        weatherProviderTypes: Set<WeatherProviderType>,
        latitude: Double,
        longitude: Double
    ): CurrentConditionsDto {
        return weatherRepository.getCurrentConditions(weatherProviderTypes, latitude, longitude)
    }

    suspend fun getHourlyForecasts(
        weatherProviderTypes: Set<WeatherProviderType>,
        latitude: Double,
        longitude: Double
    ): List<HourlyForecastDto> {
        return weatherRepository.getHourlyForecasts(weatherProviderTypes, latitude, longitude)
    }

    suspend fun getDailyForecasts(
        weatherProviderTypes: Set<WeatherProviderType>,
        latitude: Double,
        longitude: Double
    ): List<DailyForecastDto> {
        return weatherRepository.getDailyForecasts(weatherProviderTypes, latitude, longitude)
    }

    suspend fun getAirQuality(
        latitude: Double,
        longitude: Double
    ): AirQualityDto {
        return weatherRepository.getAirQuality(latitude, longitude)
    }
}