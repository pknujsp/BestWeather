package com.lifedawn.bestweather.timezone

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient
import com.lifedawn.bestweather.data.remote.retrofit.parameters.freetime.FreeTimeParameter
import com.lifedawn.bestweather.data.remote.retrofit.responses.freetime.FreeTimeResponse
import com.lifedawn.bestweather.data.remote.retrofit.callback.JsonDownloader
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FreeTimeZoneApi {
    companion object {
        fun getTimeZone(latitude: Double, longitude: Double, callback: JsonDownloader) {
            val parameter = FreeTimeParameter(latitude, longitude)
            val call = RetrofitClient.getApiService(RetrofitClient.ServiceType.FREE_TIME).getTimeZone(parameter.map)

            call.enqueue(object : Callback<JsonElement> {
                override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                    callback.onResponseResult(t)
                }

                override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                    if (response.isSuccessful && response.code() == 200) {
                        val freeTimeZoneDto = Gson().fromJson(response.body(), FreeTimeResponse::class.java)
                        callback.onResponseResult(response, freeTimeZoneDto, response.body().toString())
                    } else {
                        callback.onResponseResult(Exception("error"))
                    }
                }
            })
        }

    }
}