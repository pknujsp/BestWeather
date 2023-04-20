package com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.midlandfcstresponse

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "body", inheritance = true)
class MidLandFcstBody {
    @Expose @SerializedName("items") @Element(name = "items") var items: MidLandFcstItems? = null
}