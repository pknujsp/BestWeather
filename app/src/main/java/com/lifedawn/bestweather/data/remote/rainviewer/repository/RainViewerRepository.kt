package com.lifedawn.bestweather.data.remote.rainviewer.repository

import com.google.gson.JsonElement
import retrofit2.Callback

interface RainViewerRepository {
    fun initMap(callback: Callback<JsonElement>)

}