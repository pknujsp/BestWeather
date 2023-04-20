package com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Temp {
    @Expose @SerializedName("day") var day: String? = null
    @Expose @SerializedName("min") var min: String? = null
    @Expose @SerializedName("max") var max: String? = null
    @Expose @SerializedName("night") var night: String? = null
    @Expose @SerializedName("eve") var eve: String? = null
    @Expose @SerializedName("morn") var morn: String? = null
}