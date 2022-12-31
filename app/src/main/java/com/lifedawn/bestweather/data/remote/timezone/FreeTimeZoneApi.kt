package com.lifedawn.bestweather.data.remote.timezone

import com.google.gson.JsonElement
import com.lifedawn.bestweather.data.remote.retrofit.client.RestfulApiQuery
import com.lifedawn.bestweather.data.remote.retrofit.parameters.freetime.FreeTimeParameter
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resumeWithException

class FreeTimeZoneApi(private val freeTimeRestApi: RestfulApiQuery) {
    suspend fun getTimeZone(latitude: Double, longitude: Double) = suspendCancellableCoroutine<Response<JsonElement>> { continuation ->
        val parameter = FreeTimeParameter(latitude, longitude)
        val call = freeTimeRestApi.getTimeZone(parameter.map)

        call.enqueue(object : Callback<JsonElement> {
            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                continuation.resumeWithException(Throwable("freetimezone api response failed"))
            }

            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                if (response.isSuccessful && response.code() == 200) {
                    // val freeTimeZoneDto = Gson().fromJson(response.body(), FreeTimeResponse::class.java)
                    continuation.resumeWith(Result.success(response))
                } else {
                    continuation.resumeWithException(Throwable("freetimezone api response failed"))
                }
            }
        })
    }

}