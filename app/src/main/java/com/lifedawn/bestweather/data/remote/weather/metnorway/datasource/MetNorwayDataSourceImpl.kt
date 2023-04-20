package com.lifedawn.bestweather.data.remote.weather.metnorway.datasource

import com.google.gson.JsonElement
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import com.lifedawn.bestweather.data.remote.retrofit.callback.requestApiFlow
import com.lifedawn.bestweather.data.remote.retrofit.client.RestfulApiQuery
import com.lifedawn.bestweather.data.remote.retrofit.parameters.metnorway.LocationForecastParameters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MetNorwayDataSourceImpl @Inject constructor(private val metNorwayRestApi: RestfulApiQuery) : MetNorwayDataSource {
    override fun getLocationForecast(parameter: LocationForecastParameters): Flow<ApiResponse<JsonElement>> = flow {
        requestApiFlow {
            metNorwayRestApi.getMetNorwayLocationForecast(parameter.map)
        }
    }
}