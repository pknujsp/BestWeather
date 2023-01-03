package com.lifedawn.bestweather.data.remote.rainviewer.repository

import com.google.gson.JsonElement
import com.lifedawn.bestweather.data.remote.retrofit.client.RestfulApiQuery
import retrofit2.Callback

class RainViewerRepositoryImpl constructor(private val rainViewerApi: RestfulApiQuery) : RainViewerRepository {
    override fun initMap(callback: Callback<JsonElement>) {
        rainViewerApi.getRainViewer().enqueue(callback)
    }

}