package com.lifedawn.bestweather.data.remote.weather.owm.datasource

import com.google.gson.JsonElement
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import com.lifedawn.bestweather.data.remote.retrofit.callback.requestApiFlow
import com.lifedawn.bestweather.data.remote.retrofit.client.RestfulApiQuery
import com.lifedawn.bestweather.data.remote.retrofit.parameters.openweathermap.onecall.OwmOneCallParameter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class OwmDataSourceImpl @Inject constructor(private val owmOneCallRestApi: RestfulApiQuery) : OwmDataSource {
    override fun getOneCall(oneCallParameter: OwmOneCallParameter): Flow<ApiResponse<JsonElement>> = flow {
        requestApiFlow {
            owmOneCallRestApi.getOneResponse(oneCallParameter.map)
        }
    }
}