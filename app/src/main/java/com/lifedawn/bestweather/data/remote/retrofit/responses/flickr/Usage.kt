package com.lifedawn.bestweather.data.remote.retrofit.responses.flickr

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Usage : Serializable {
    @SerializedName("candownload") @Expose var candownload: Int? = null
    @SerializedName("canblog") @Expose var canblog: Int? = null
    @SerializedName("canprint") @Expose var canprint: Int? = null
    @SerializedName("canshare") @Expose var canshare: Int? = null

    companion object {
        private const val serialVersionUID = -5283059575544353119L
    }
}