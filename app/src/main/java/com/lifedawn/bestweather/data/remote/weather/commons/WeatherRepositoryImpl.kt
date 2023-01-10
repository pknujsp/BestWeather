package com.lifedawn.bestweather.data.remote.weather.commons

import com.lifedawn.bestweather.commons.classes.FlowResponse
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto
import com.lifedawn.bestweather.data.local.weather.models.CurrentConditionsDto
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnDataSource
import com.lifedawn.bestweather.data.remote.weather.kma.datasource.KmaDataSource
import com.lifedawn.bestweather.data.remote.weather.metnorway.datasource.MetNorwayDataSource
import com.lifedawn.bestweather.data.remote.weather.owm.datasource.OwmDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val kmaDataSource: KmaDataSource,
    private val owmDataSource: OwmDataSource,
    private val metNorwayDataSource: MetNorwayDataSource,
    private val aqicnDataSource: AqicnDataSource
) : WeatherRepository {

    override suspend fun getCurrentConditions(
        weatherProviderTypes: Set<WeatherProviderType>,
        latitude: Double,
        longitude: Double
    ): Flow<ApiResponse<CurrentConditionsDto>> = flow {
        emit(object : FlowResponse)
    }

    override suspend fun getHourlyForecasts(
        weatherProviderTypes: Set<WeatherProviderType>,
        latitude: Double,
        longitude: Double
    ): Flow<ApiResponse<List<HourlyForecastDto>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getDailyForecasts(
        weatherProviderTypes: Set<WeatherProviderType>,
        latitude: Double,
        longitude: Double
    ): Flow<ApiResponse<List<DailyForecastDto>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getAirQuality(latitude: Double, longitude: Double): Flow<ApiResponse<AirQualityDto>> {
        TODO("Not yet implemented")
    }
}