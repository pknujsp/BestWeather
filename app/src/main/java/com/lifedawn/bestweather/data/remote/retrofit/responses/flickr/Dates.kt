package com.lifedawn.bestweather.data.remote.retrofit.responses.flickr

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Dates : Serializable {
    @SerializedName("posted") @Expose var posted: String? = null
    @SerializedName("taken") @Expose var taken: String? = null
    @SerializedName("takengranularity") @Expose var takengranularity: String? = null
    @SerializedName("takenunknown") @Expose var takenunknown: String? = null
    @SerializedName("lastupdate") @Expose var lastupdate: String? = null

    companion object {
        const val serialVersionUID = 838551288520418340L
    }
}