package com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Coord {
    @Expose @SerializedName("lon") var lon: String? = null
    @Expose @SerializedName("lat") var lat: String? = null
}