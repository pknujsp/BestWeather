package com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class WindGust {
    @Expose @SerializedName("Speed") var speed: ValuesUnit? = null
}