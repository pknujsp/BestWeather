package com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Minimum {
    @Expose @SerializedName("Metric") var metric: ValueUnit? = null
    @Expose @SerializedName("Imperial") var imperial: ValueUnit? = null
}