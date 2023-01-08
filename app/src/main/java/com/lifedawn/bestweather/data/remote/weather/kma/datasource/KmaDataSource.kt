package com.lifedawn.bestweather.data.remote.weather.kma.datasource

import com.lifedawn.bestweather.data.remote.retrofit.parameters.kma.KmaCurrentConditionsParameters
import com.lifedawn.bestweather.data.remote.retrofit.parameters.kma.KmaForecastsParameters
import retrofit2.Response

interface KmaDataSource {
    suspend fun getCurrentConditions(kmaCurrentConditionsParameters: KmaCurrentConditionsParameters): Result<String>
    suspend fun getForecasts(kmaForecastsParameters: KmaForecastsParameters): Result<String>
}