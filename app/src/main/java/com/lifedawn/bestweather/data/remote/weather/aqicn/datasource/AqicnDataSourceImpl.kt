package com.lifedawn.bestweather.data.remote.weather.aqicn.datasource

import com.google.gson.JsonElement
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import com.lifedawn.bestweather.data.remote.retrofit.callback.requestApiFlow
import com.lifedawn.bestweather.data.remote.retrofit.client.RestfulApiQuery
import com.lifedawn.bestweather.data.remote.retrofit.parameters.aqicn.AqicnParameters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AqicnDataSourceImpl @Inject constructor(private val aqicnRestApi: RestfulApiQuery) : AqicnDataSource {
    override fun getAqicn(aqicnParameters: AqicnParameters): Flow<ApiResponse<JsonElement>> = flow {
        requestApiFlow {
            aqicnRestApi.getAqiCnGeolocalizedFeed(aqicnParameters.latitude, aqicnParameters.longitude, aqicnParameters.map)
        }
    }
}