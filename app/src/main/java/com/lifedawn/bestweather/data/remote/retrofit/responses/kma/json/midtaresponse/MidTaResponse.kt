package com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.midtaresponse

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.kmacommons.KmaHeader
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "response", inheritance = true)
open class MidTaResponse {
    @Expose @SerializedName("header") @Element(name = "header") var kmaHeader: KmaHeader? = null
        private set
    @Expose @SerializedName("body") @Element(name = "body") var body: MidTaBody? = null
    fun setKmaHeader(kmaHeader: KmaHeader?): MidTaResponse {
        this.kmaHeader = kmaHeader
        return this
    }
}