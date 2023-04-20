package com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.vilagefcstcommons

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "body", inheritance = true)
class VilageFcstBody {
    @Expose @SerializedName("items") @Element(name = "items") var items: VilageFcstItems? = null
}