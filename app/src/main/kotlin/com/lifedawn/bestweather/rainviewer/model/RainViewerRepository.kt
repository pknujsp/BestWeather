package com.lifedawn.bestweather.rainviewer.model

import com.google.gson.JsonElement
import retrofit2.Callback

interface RainViewerRepository {
    fun initMap(callback: Callback<JsonElement>)

}