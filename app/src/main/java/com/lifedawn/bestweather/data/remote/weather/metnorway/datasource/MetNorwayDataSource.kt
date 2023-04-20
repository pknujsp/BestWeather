package com.lifedawn.bestweather.data.remote.weather.metnorway.datasource

import com.google.gson.JsonElement
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import com.lifedawn.bestweather.data.remote.retrofit.parameters.metnorway.LocationForecastParameters
import kotlinx.coroutines.flow.Flow

interface MetNorwayDataSource {
    fun getLocationForecast(parameter: LocationForecastParameters): Flow<ApiResponse<JsonElement>>
}