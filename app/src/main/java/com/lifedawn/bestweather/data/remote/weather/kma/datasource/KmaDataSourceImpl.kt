package com.lifedawn.bestweather.data.remote.weather.kma.datasource

import com.lifedawn.bestweather.data.remote.retrofit.client.RestfulApiQuery
import com.lifedawn.bestweather.data.remote.retrofit.parameters.kma.KmaCurrentConditionsParameters
import com.lifedawn.bestweather.data.remote.retrofit.parameters.kma.KmaForecastsParameters
import javax.inject.Inject

class KmaDataSourceImpl @Inject constructor(
    private val kmaWebCurrentConditionsRestApi: RestfulApiQuery,
    private val KmaWebForecastsRestApi: RestfulApiQuery
) : KmaDataSource {
    override suspend fun getCurrentConditions(kmaCurrentConditionsParameters: KmaCurrentConditionsParameters): Result<String> {
        val response = kmaWebCurrentConditionsRestApi.getKmaCurrentConditions(kmaCurrentConditionsParameters.parametersMap)

        return if (response.body() != null) Result.success(response.body().toString())
        else Result.failure(Exception("failure"))
    }

    override suspend fun getForecasts(kmaForecastsParameters: KmaForecastsParameters): Result<String> {
        val response = KmaWebForecastsRestApi.getKmaHourlyAndDailyForecast(kmaForecastsParameters.parametersMap)

        return if (response.body() != null) Result.success(response.body().toString())
        else Result.failure(Exception("failure"))
    }
}