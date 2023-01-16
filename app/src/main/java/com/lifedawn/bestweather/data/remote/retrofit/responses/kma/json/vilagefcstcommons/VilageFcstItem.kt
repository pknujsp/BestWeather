package com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.vilagefcstcommons

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "item", inheritance = true)
class VilageFcstItem {
    @Expose @SerializedName("baseDate") @PropertyElement(name = "baseDate") var baseDate: String? = null
    @Expose @SerializedName("baseTime") @PropertyElement(name = "baseTime") var baseTime: String? = null
    @Expose @SerializedName("category") @PropertyElement(name = "category") var category: String? = null
    @Expose @SerializedName("fcstDate") @PropertyElement(name = "fcstDate") var fcstDate: String? = null
    @Expose @SerializedName("fcstTime") @PropertyElement(name = "fcstTime") var fcstTime: String? = null
    @Expose @SerializedName("fcstValue") @PropertyElement(name = "fcstValue") var fcstValue: String? = null
    @Expose @SerializedName("obSrValue") @PropertyElement(name = "obsrValue") var obsrValue: String? = null
        private set
    @Expose @SerializedName("nx") @PropertyElement(name = "nx") var nx: String? = null
    @Expose @SerializedName("ny") @PropertyElement(name = "ny") var ny: String? = null
    fun setObsrValue(obsrValue: String?): VilageFcstItem {
        this.obsrValue = obsrValue
        return this
    }
}