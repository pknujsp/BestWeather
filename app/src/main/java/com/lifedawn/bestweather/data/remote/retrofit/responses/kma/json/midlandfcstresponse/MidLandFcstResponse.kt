package com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.midlandfcstresponse

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.kmacommons.KmaHeader
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "response", inheritance = true)
class MidLandFcstResponse {
    @Expose @SerializedName("header") @Element(name = "header") var kmaHeader: KmaHeader? = null
    @Expose @SerializedName("body") @Element(name = "body") var body: MidLandFcstBody? = null
}