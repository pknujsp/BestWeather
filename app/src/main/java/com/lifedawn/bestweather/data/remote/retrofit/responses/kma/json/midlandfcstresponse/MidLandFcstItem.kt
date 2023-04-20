package com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.midlandfcstresponse

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "item", inheritance = true)
class MidLandFcstItem : Cloneable {
    //중기육상예보
    @Expose @SerializedName("regId") @PropertyElement(name = "regId") var regId: String? = null
    @Expose @SerializedName("rnSt10") @PropertyElement(name = "rnSt10") var rnSt10: String? = null
    @Expose @SerializedName("rnSt3Am") @PropertyElement(name = "rnSt3Am") var rnSt3Am: String? = null
    @Expose @SerializedName("rnSt3Pm") @PropertyElement(name = "rnSt3Pm") var rnSt3Pm: String? = null
    @Expose @SerializedName("rnSt4Am") @PropertyElement(name = "rnSt4Am") var rnSt4Am: String? = null
    @Expose @SerializedName("rnSt4Pm") @PropertyElement(name = "rnSt4Pm") var rnSt4Pm: String? = null
    @Expose @SerializedName("rnSt5Am") @PropertyElement(name = "rnSt5Am") var rnSt5Am: String? = null
    @Expose @SerializedName("rnSt5Pm") @PropertyElement(name = "rnSt5Pm") var rnSt5Pm: String? = null
    @Expose @SerializedName("rnSt6Am") @PropertyElement(name = "rnSt6Am") var rnSt6Am: String? = null
    @Expose @SerializedName("rnSt6Pm") @PropertyElement(name = "rnSt6Pm") var rnSt6Pm: String? = null
    @Expose @SerializedName("rnSt7Am") @PropertyElement(name = "rnSt7Am") var rnSt7Am: String? = null
    @Expose @SerializedName("rnSt7Pm") @PropertyElement(name = "rnSt7Pm") var rnSt7Pm: String? = null
    @Expose @SerializedName("rnSt8") @PropertyElement(name = "rnSt8") var rnSt8: String? = null
    @Expose @SerializedName("rnSt9") @PropertyElement(name = "rnSt9") var rnSt9: String? = null
    @Expose @SerializedName("wf10") @PropertyElement(name = "wf10") var wf10: String? = null
    @Expose @SerializedName("wf3Am") @PropertyElement(name = "wf3Am") var wf3Am: String? = null
    @Expose @SerializedName("wf3Pm") @PropertyElement(name = "wf3Pm") var wf3Pm: String? = null
    @Expose @SerializedName("wf4Am") @PropertyElement(name = "wf4Am") var wf4Am: String? = null
    @Expose @SerializedName("wf4Pm") @PropertyElement(name = "wf4Pm") var wf4Pm: String? = null
    @Expose @SerializedName("wf5Am") @PropertyElement(name = "wf5Am") var wf5Am: String? = null
    @Expose @SerializedName("wf5Pm") @PropertyElement(name = "wf5Pm") var wf5Pm: String? = null
    @Expose @SerializedName("wf6Am") @PropertyElement(name = "wf6Am") var wf6Am: String? = null
    @Expose @SerializedName("wf6Pm") @PropertyElement(name = "wf6Pm") var wf6Pm: String? = null
    @Expose @SerializedName("wf7Am") @PropertyElement(name = "wf7Am") var wf7Am: String? = null
    @Expose @SerializedName("wf7Pm") @PropertyElement(name = "wf7Pm") var wf7Pm: String? = null
    @Expose @SerializedName("wf8") @PropertyElement(name = "wf8") var wf8: String? = null
    @Expose @SerializedName("wf9") @PropertyElement(name = "wf9") var wf9: String? = null
    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Any {
        return super.clone()
    }
}