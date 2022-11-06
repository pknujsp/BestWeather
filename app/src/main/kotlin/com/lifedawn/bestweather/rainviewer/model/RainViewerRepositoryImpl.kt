package com.lifedawn.bestweather.rainviewer.model

import com.google.gson.JsonElement
import com.lifedawn.bestweather.retrofit.client.RetrofitClient
import retrofit2.Callback

object RainViewerRepositoryImpl : RainViewerRepository {
    override fun initMap(callback: Callback<JsonElement>) {
        RetrofitClient.getApiService(RetrofitClient.ServiceType.RAIN_VIEWER)
                .rainViewer.enqueue(callback)
    }

}