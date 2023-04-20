package com.lifedawn.bestweather.data.remote.retrofit.responses.metnorway.locationforecast

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LocationForecastResponse {
    @Expose @SerializedName("type") var type: String? = null
    @Expose @SerializedName("geometry") var geometry: LocationForecastGeometry? = null
    @Expose @SerializedName("properties") var properties: LocationForecastProperties? = null
}