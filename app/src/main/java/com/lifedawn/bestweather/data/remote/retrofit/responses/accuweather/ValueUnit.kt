package com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ValueUnit {
    @Expose @SerializedName("Value") var value: String? = null
    @Expose @SerializedName("Unit") var unit: String? = null
    @Expose @SerializedName("UnitType") var unitType: String? = null
}