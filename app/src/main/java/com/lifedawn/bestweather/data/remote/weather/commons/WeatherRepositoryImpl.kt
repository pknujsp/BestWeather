package com.lifedawn.bestweather.data.remote.weather.commons

import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnDataSource
import com.lifedawn.bestweather.data.remote.weather.kma.datasource.KmaDataSource
import com.lifedawn.bestweather.data.remote.weather.metnorway.datasource.MetNorwayDataSource
import com.lifedawn.bestweather.data.remote.weather.owm.datasource.OwmDataSource
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val kmaDataSource: KmaDataSource,
    private val owmDataSource: OwmDataSource,
    private val metNorwayDataSource: MetNorwayDataSource,
    private val aqicnDataSource: AqicnDataSource
) : WeatherRepository {

    override suspend fun getCurrentConditions() {
        TODO("Not yet implemented")
    }

    override suspend fun getHourlyForecasts() {
        TODO("Not yet implemented")
    }

    override suspend fun getDailyForecasts() {
        TODO("Not yet implemented")
    }

    override suspend fun getAirQuality() {
        TODO("Not yet implemented")
    }
}