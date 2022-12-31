package com.lifedawn.bestweather.ui.weathers.models

import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.remote.retrofit.responses.aqicn.AqiCnGeolocalizedFeedResponse
import java.time.ZoneId

data class WeatherDataDTO(
    val currentConditionsDto: CurrentConditionsDto, val hourlyForecastList: ArrayList<HourlyForecastDto>,
    val dailyForecastList: ArrayList<DailyForecastDto>, val airQualityDto: AirQualityDto, val currentConditionsWeatherVal: String,
    val latitude: Double,
    val longitude: Double, val addressName: String, val countryCode: String, val mainWeatherProviderType: WeatherProviderType,
    val zoneId: ZoneId, val precipitationVolume: String, val airQualityResponse: AqiCnGeolocalizedFeedResponse?
)
