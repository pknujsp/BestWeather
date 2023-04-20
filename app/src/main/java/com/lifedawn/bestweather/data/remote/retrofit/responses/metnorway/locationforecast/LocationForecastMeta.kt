package com.lifedawn.bestweather.data.remote.retrofit.responses.metnorway.locationforecast

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LocationForecastMeta {
    @Expose @SerializedName("updated_at") var updatedAt: String? = null
    @Expose @SerializedName("units") var units: LocationForecastUnits? = null
}