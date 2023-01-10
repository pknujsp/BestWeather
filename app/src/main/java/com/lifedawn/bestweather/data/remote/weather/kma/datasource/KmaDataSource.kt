package com.lifedawn.bestweather.data.remote.weather.kma.datasource

import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import com.lifedawn.bestweather.data.remote.retrofit.parameters.kma.KmaCurrentConditionsParameters
import com.lifedawn.bestweather.data.remote.retrofit.parameters.kma.KmaForecastsParameters
import kotlinx.coroutines.flow.Flow

interface KmaDataSource {
    suspend fun getCurrentConditions(kmaCurrentConditionsParameters: KmaCurrentConditionsParameters): Flow<ApiResponse<String>>
    suspend fun getForecasts(kmaForecastsParameters: KmaForecastsParameters): Flow<ApiResponse<String>>
}