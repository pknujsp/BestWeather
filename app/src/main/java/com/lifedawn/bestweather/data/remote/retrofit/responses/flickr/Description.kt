package com.lifedawn.bestweather.data.remote.retrofit.responses.flickr

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Description : Serializable {
    @SerializedName("_content") @Expose var content: String? = null

    companion object {
        private const val serialVersionUID = -2580127429010408574L
    }
}