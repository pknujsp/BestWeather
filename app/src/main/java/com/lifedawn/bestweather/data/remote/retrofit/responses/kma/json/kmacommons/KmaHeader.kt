package com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.kmacommons

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "header", inheritance = true)
class KmaHeader {
    @Expose @SerializedName("resultCode") @PropertyElement(name = "resultCode") var resultCode: String? = null
    @Expose @SerializedName("resultMsg") @PropertyElement(name = "resultMsg") var resultMsg: String? = null
}