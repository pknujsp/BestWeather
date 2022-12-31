package com.lifedawn.bestweather.data.remote.timezone

import com.google.gson.JsonElement
import retrofit2.Response

class RemoteTimeZoneRepositoryImpl(private val freeTimeZoneApi: FreeTimeZoneApi) : RemoteTimeZoneRepository {
    override suspend fun getTimeZone(latitude: Double, longitude: Double): Response<JsonElement> =
        freeTimeZoneApi.getTimeZone(latitude, longitude)

}