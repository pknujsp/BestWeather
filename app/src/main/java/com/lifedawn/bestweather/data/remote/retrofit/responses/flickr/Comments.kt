package com.lifedawn.bestweather.data.remote.retrofit.responses.flickr

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Comments : Serializable {
    @SerializedName("_content") @Expose var content: Int? = null

    companion object {
        private const val serialVersionUID = -751650815179992814L
    }
}