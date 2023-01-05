package com.lifedawn.bestweather.data.remote.timezone

import com.google.gson.JsonElement
import com.lifedawn.bestweather.data.remote.retrofit.client.RestfulApiQuery
import com.lifedawn.bestweather.data.remote.retrofit.parameters.freetime.FreeTimeParameter
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.await
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

class RemoteTimeZoneRepositoryImpl @Inject constructor(private val freeTimeRestApi: RestfulApiQuery) : RemoteTimeZoneRepository {
    override suspend fun getTimeZone(latitude: Double, longitude: Double): Result<JsonElement> {
        val response = freeTimeRestApi.getTimeZone(FreeTimeParameter(latitude, longitude).map)
        return response.body()?.run { Result.success(this) } ?: Result.failure(Exception(response.message()))
    }
}