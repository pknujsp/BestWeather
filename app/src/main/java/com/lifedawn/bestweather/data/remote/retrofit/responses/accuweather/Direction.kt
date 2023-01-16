package com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Direction {
    @Expose @SerializedName("Degrees") var degrees: String? = null
    @Expose @SerializedName("Localized") var localized: String? = null
    @Expose @SerializedName("English") var english: String? = null
}