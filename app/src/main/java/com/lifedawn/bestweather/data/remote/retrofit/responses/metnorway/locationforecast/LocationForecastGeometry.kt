package com.lifedawn.bestweather.data.remote.retrofit.responses.metnorway.locationforecast

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LocationForecastGeometry {
    @Expose @SerializedName("type") var type: String? = null
    @Expose @SerializedName("coordinates") var coordinates: List<String>? = null
}