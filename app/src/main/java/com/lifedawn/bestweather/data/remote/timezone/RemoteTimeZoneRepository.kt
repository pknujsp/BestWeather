package com.lifedawn.bestweather.data.remote.timezone

import com.google.gson.JsonElement
import retrofit2.Response

interface RemoteTimeZoneRepository {
    suspend fun getTimeZone(latitude: Double, longitude: Double): Result<JsonElement>
}