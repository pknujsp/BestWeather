package com.lifedawn.bestweather.data.remote.retrofit.responses.freetime

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class FreeTimeResponse {
    @Expose @SerializedName("timeZone") var timezone: String? = null
}