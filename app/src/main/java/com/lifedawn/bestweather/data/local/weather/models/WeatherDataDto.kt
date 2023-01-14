package com.lifedawn.bestweather.data.local.weather.models

import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import java.time.ZoneId

data class WeatherDataDto(
    val currentConditionsDto: CurrentConditionsDto,
    val hourlyForecastList: List<HourlyForecastDto>,
    val dailyForecastList: List<DailyForecastDto>,
    val airQualityDto: AirQualityDto,
    val latitude: Double,
    val longitude: Double,
    val addressName: String,
    val countryCode: String,
    val mainWeatherProviderType: WeatherProviderType,
    val zoneId: ZoneId,
)
