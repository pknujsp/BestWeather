package com.lifedawn.bestweather.data.remote.weather.owm.repository

import android.content.Context
import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import com.lifedawn.bestweather.data.remote.retrofit.parameters.openweathermap.onecall.OwmOneCallParameter
import com.lifedawn.bestweather.data.remote.weather.commons.model.WeatherDataDto
import com.lifedawn.bestweather.data.remote.weather.owm.OwmResponseProcessor
import com.lifedawn.bestweather.data.remote.weather.owm.datasource.OwmDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.ZoneId
import javax.inject.Inject

class OwmWeatherRepositoryImpl @Inject constructor(
    private val owmDataSource: OwmDataSource,
    private val context: Context
) : OwmWeatherRepository {
    override fun getWeatherData(weatherDataTypes: Set<WeatherDataType>, latitude: Double, longitude: Double, zoneId: ZoneId)
            : Flow<WeatherDataDto> = flow {
        val excludes = mutableSetOf<OwmOneCallParameter.OneCallApis>()
        if (!weatherDataTypes.contains(WeatherDataType.currentConditions))
            excludes.add(OwmOneCallParameter.OneCallApis.current)
        if (!weatherDataTypes.contains(WeatherDataType.dailyForecast))
            excludes.add(OwmOneCallParameter.OneCallApis.daily)
        if (!weatherDataTypes.contains(WeatherDataType.hourlyForecast))
            excludes.add(OwmOneCallParameter.OneCallApis.hourly)

        val oneCallParameter =
            OwmOneCallParameter(latitude.toString(), longitude.toString())
        oneCallParameter.oneCallApis.addAll(excludes)

        val response = owmDataSource.getOneCall(oneCallParameter)
        response.collect {
            if (it is ApiResponse.Success) {
                val owmOneCallResponse = OwmResponseProcessor.getOneCallObjFromJson(
                    it.data.toString()
                )
                val weatherDataDto = WeatherDataDto(weatherDataTypes)

                if (weatherDataTypes.contains(WeatherDataType.currentConditions))
                    weatherDataDto.currentConditions =
                        OwmResponseProcessor.makeCurrentConditionsDtoOneCall(context, owmOneCallResponse, zoneId)
                if (weatherDataTypes.contains(WeatherDataType.dailyForecast))
                    weatherDataDto.dailyForecasts =
                        OwmResponseProcessor.makeDailyForecastDtoListOneCall(context, owmOneCallResponse, zoneId)
                if (weatherDataTypes.contains(WeatherDataType.hourlyForecast))
                    weatherDataDto.hourlyForecasts =
                        OwmResponseProcessor.makeHourlyForecastDtoListOneCall(context, owmOneCallResponse, zoneId)
            } else {
                emit(WeatherDataDto(weatherDataTypes))
            }
        }
    }
}