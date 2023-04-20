package com.lifedawn.bestweather.data.remote.retrofit.responses.metnorway.locationforecast.timeseries

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LocationForecastTimeSeriesItem {
    @Expose @SerializedName("time") var time: String? = null
    @Expose @SerializedName("data") var data: Data? = null
}