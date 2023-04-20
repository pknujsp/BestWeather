package com.lifedawn.bestweather.data.remote.retrofit.responses.flickr

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Url : Serializable {
    @SerializedName("type") @Expose var type: String? = null
    @SerializedName("_content") @Expose var content: String? = null

    companion object {
        private const val serialVersionUID = -5745178775952451066L
    }
}