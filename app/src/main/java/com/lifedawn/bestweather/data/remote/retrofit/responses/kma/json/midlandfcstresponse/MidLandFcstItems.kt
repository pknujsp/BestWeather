package com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.midlandfcstresponse

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "items", inheritance = true)
class MidLandFcstItems {
    @Expose @SerializedName("item") @Element(name = "item") var item: List<MidLandFcstItem>? = null
}