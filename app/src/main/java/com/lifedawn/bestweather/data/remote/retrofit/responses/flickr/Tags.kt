package com.lifedawn.bestweather.data.remote.retrofit.responses.flickr

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Tags : Serializable {
    @SerializedName("tag") @Expose var tag: List<Tag>? = null

    companion object {
        private const val serialVersionUID = 9129284584894733331L
    }
}