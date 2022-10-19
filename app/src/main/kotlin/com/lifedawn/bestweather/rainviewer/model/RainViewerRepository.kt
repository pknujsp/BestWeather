package com.lifedawn.bestweather.rainviewer.model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.lifedawn.bestweather.model.timezone.TimeZoneIdRepository
import com.lifedawn.bestweather.retrofit.client.RetrofitClient
import com.lifedawn.bestweather.room.AppDb
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RainViewerRepository {
    val initMapLiveData: MutableLiveData<RainViewerResponseDto?> = MutableLiveData()

    companion object {
        var INSTANCE: RainViewerRepository? = null

        fun initialize() {
            if (INSTANCE == null) {
                INSTANCE = RainViewerRepository()
            }
        }

    }

    fun initMap() {
        val call = RetrofitClient.getApiService(RetrofitClient.ServiceType.RAIN_VIEWER)
                .rainViewer.enqueue(object : Callback<JsonElement> {
                    override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                        if (response.isSuccessful) {
                            val responseDto: RainViewerResponseDto = Gson().fromJson(response.body(),
                                    RainViewerResponseDto::class.java)
                            initMapLiveData.postValue(responseDto)
                        } else {
                            //fail
                            initMapLiveData.postValue(null)
                        }
                    }

                    override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                        initMapLiveData.postValue(null)
                    }
                })

    }

    interface IRainViewer {
        fun initMap()
    }
}