package com.lifedawn.bestweather.data.remote.weather.metnorway.repository

import android.content.Context
import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import com.lifedawn.bestweather.data.remote.retrofit.parameters.metnorway.LocationForecastParameters
import com.lifedawn.bestweather.data.remote.weather.commons.model.WeatherDataDto
import com.lifedawn.bestweather.data.remote.weather.metnorway.MetNorwayResponseProcessor
import com.lifedawn.bestweather.data.remote.weather.metnorway.MetNorwayResponseProcessor.getLocationForecastResponseObjFromJson
import com.lifedawn.bestweather.data.remote.weather.metnorway.datasource.MetNorwayDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.ZoneId
import javax.inject.Inject

class MetNorwayRepositoryImpl @Inject constructor(
    private val metNorwayDataSource: MetNorwayDataSource,
    private val context: Context
) : MetNorwayRepository {

    override fun getWeatherData(latitude: Double, longitude: Double, zoneId: ZoneId): Flow<WeatherDataDto> = flow {
        val response = metNorwayDataSource.getLocationForecast(LocationForecastParameters(latitude.toString(), longitude.toString()))
        response.collect {
            if (it is ApiResponse.Success) {
                val locationForecastResponse = getLocationForecastResponseObjFromJson(it.data.toString())
                val weatherDataDto = WeatherDataDto(
                    setOf(
                        WeatherDataType.hourlyForecast,
                        WeatherDataType.dailyForecast, WeatherDataType.currentConditions
                    )
                )

                val currentConditions = MetNorwayResponseProcessor.makeCurrentConditionsDto(
                    context,
                    locationForecastResponse, zoneId
                )
                val hourlyForecasts = MetNorwayResponseProcessor.makeHourlyForecastDtoList(
                    context,
                    locationForecastResponse, zoneId
                )
                val dailyForecasts = MetNorwayResponseProcessor.makeDailyForecastDtoList(
                    context,
                    locationForecastResponse, zoneId
                )

                weatherDataDto.currentConditions = currentConditions
                weatherDataDto.hourlyForecasts = hourlyForecasts
                weatherDataDto.dailyForecasts = dailyForecasts

            } else {
                emit(WeatherDataDto())
            }
        }
    }
}