package com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.vilagefcstcommons

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.kmacommons.KmaHeader
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "response", inheritance = true)
class VilageFcstResponse {
    @Expose @SerializedName("header") @Element(name = "header") var kmaHeader: KmaHeader? = null
        private set
    @Expose @SerializedName("body") @Element(name = "body") var body: VilageFcstBody? = null
    fun setKmaHeader(kmaHeader: KmaHeader?): VilageFcstResponse {
        this.kmaHeader = kmaHeader
        return this
    }
}