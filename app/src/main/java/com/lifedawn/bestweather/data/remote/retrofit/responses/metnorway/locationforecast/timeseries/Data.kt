package com.lifedawn.bestweather.data.remote.retrofit.responses.metnorway.locationforecast.timeseries

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {
    @Expose @SerializedName("instant") var instant: Instant? = null
    @Expose @SerializedName("next_12_hours") var next_12_hours: Next12Hours? = null
    @Expose @SerializedName("next_1_hours") var next_1_hours: Next1Hours? = null
    @Expose @SerializedName("next_6_hours") var next_6_hours: Next6Hours? = null
}