package com.lifedawn.bestweather.data.remote.timezone

import com.google.gson.JsonElement
import com.lifedawn.bestweather.data.remote.retrofit.client.RestfulApiQuery
import com.lifedawn.bestweather.data.remote.retrofit.parameters.freetime.FreeTimeParameterRest
import javax.inject.Inject

class RemoteTimeZoneRepositoryImpl @Inject constructor(private val freeTimeRestApi: RestfulApiQuery) : RemoteTimeZoneRepository {
    override suspend fun getTimeZone(latitude: Double, longitude: Double): Result<JsonElement> {
        val response = freeTimeRestApi.getTimeZone(
            FreeTimeParameterRest(
                latitude,
                longitude
            ).map)
        return response.body()?.run { Result.success(this) } ?: Result.failure(Exception(response.message()))
    }
}