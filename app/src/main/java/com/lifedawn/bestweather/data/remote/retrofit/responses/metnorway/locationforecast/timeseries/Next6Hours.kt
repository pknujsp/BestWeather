package com.lifedawn.bestweather.data.remote.retrofit.responses.metnorway.locationforecast.timeseries

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Next6Hours {
    @Expose @SerializedName("summary") var summary: Summary? = null
    @Expose @SerializedName("details") var details: Details? = null
}