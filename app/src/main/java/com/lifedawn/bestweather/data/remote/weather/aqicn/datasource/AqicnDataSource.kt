package com.lifedawn.bestweather.data.remote.weather.aqicn.datasource

import com.google.gson.JsonElement
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import com.lifedawn.bestweather.data.remote.retrofit.parameters.aqicn.AqicnParameters
import kotlinx.coroutines.flow.Flow

interface AqicnDataSource {
    fun getAqicn(aqicnParameters: AqicnParameters): Flow<ApiResponse<JsonElement>>
}