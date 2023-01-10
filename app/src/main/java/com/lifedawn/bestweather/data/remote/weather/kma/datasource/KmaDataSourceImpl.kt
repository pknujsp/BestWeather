package com.lifedawn.bestweather.data.remote.weather.kma.datasource

import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import com.lifedawn.bestweather.data.remote.retrofit.callback.result
import com.lifedawn.bestweather.data.remote.retrofit.client.RestfulApiQuery
import com.lifedawn.bestweather.data.remote.retrofit.parameters.kma.KmaCurrentConditionsParameters
import com.lifedawn.bestweather.data.remote.retrofit.parameters.kma.KmaForecastsParameters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class KmaDataSourceImpl @Inject constructor(
    private val kmaWebCurrentConditionsRestApi: RestfulApiQuery,
    private val KmaWebForecastsRestApi: RestfulApiQuery
) : KmaDataSource {
    override suspend fun getCurrentConditions(kmaCurrentConditionsParameters: KmaCurrentConditionsParameters):
            Flow<ApiResponse<String>> = flow {
        emit(
            kmaWebCurrentConditionsRestApi.getKmaCurrentConditions(kmaCurrentConditionsParameters.parametersMap)
                .result()
        )
    }

    override suspend fun getForecasts(kmaForecastsParameters: KmaForecastsParameters): Flow<ApiResponse<String>> = flow {
        emit(
            KmaWebForecastsRestApi.getKmaHourlyAndDailyForecast(kmaForecastsParameters.parametersMap)
                .result()
        )
    }
}