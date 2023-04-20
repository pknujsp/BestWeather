package com.lifedawn.bestweather.data.remote.weather.owm.datasource

import com.google.gson.JsonElement
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import com.lifedawn.bestweather.data.remote.retrofit.parameters.openweathermap.onecall.OwmOneCallParameter
import kotlinx.coroutines.flow.Flow

interface OwmDataSource {
    fun getOneCall(oneCallParameter: OwmOneCallParameter): Flow<ApiResponse<JsonElement>>
}