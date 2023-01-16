package com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class FeelsLike {
    @Expose @SerializedName("day") var day: String? = null
    @Expose @SerializedName("night") var night: String? = null
    @Expose @SerializedName("eve") var eve: String? = null
    @Expose @SerializedName("morn") var morn: String? = null
}