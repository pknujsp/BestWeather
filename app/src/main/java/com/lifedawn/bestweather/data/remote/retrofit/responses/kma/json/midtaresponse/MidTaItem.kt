package com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.midtaresponse

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "item", inheritance = true)
class MidTaItem : Cloneable {
    @Expose @SerializedName("regId") @PropertyElement(name = "regId") var regId: String? = null
    @Expose @SerializedName("taMax3") @PropertyElement(name = "taMax3") var taMax3: String? = null
    @Expose @SerializedName("taMax4") @PropertyElement(name = "taMax4") var taMax4: String? = null
    @Expose @SerializedName("taMax5") @PropertyElement(name = "taMax5") var taMax5: String? = null
    @Expose @SerializedName("taMax6") @PropertyElement(name = "taMax6") var taMax6: String? = null
    @Expose @SerializedName("taMax7") @PropertyElement(name = "taMax7") var taMax7: String? = null
    @Expose @SerializedName("taMax8") @PropertyElement(name = "taMax8") var taMax8: String? = null
    @Expose @SerializedName("taMax9") @PropertyElement(name = "taMax9") var taMax9: String? = null
    @Expose @SerializedName("taMax10") @PropertyElement(name = "taMax10") var taMax10: String? = null
    @Expose @SerializedName("taMin3") @PropertyElement(name = "taMin3") var taMin3: String? = null
    @Expose @SerializedName("taMin4") @PropertyElement(name = "taMin4") var taMin4: String? = null
    @Expose @SerializedName("taMin5") @PropertyElement(name = "taMin5") var taMin5: String? = null
    @Expose @SerializedName("taMin6") @PropertyElement(name = "taMin6") var taMin6: String? = null
    @Expose @SerializedName("taMin7") @PropertyElement(name = "taMin7") var taMin7: String? = null
    @Expose @SerializedName("taMin8") @PropertyElement(name = "taMin8") var taMin8: String? = null
    @Expose @SerializedName("taMin9") @PropertyElement(name = "taMin9") var taMin9: String? = null
    @Expose @SerializedName("taMin10") @PropertyElement(name = "taMin10") var taMin10: String? = null
    val minArr: Array<String?>
        get() = arrayOf(taMin3, taMin4, taMin5, taMin6, taMin7, taMin8, taMin9, taMin10)
    val maxArr: Array<String?>
        get() = arrayOf(taMax3, taMax4, taMax5, taMax6, taMax7, taMax8, taMax9, taMax10)

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Any {
        return super.clone()
    }
}