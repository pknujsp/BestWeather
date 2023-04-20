package com.lifedawn.bestweather.data.remote.retrofit.responses.metnorway.locationforecast.timeseries

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Summary {
    @Expose @SerializedName("symbol_code") var symbolCode: String? = null
}