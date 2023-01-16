package com.lifedawn.bestweather.data.remote.retrofit.responses.flickr

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Publiceditability : Serializable {
    @SerializedName("cancomment") @Expose var cancomment: Int? = null
    @SerializedName("canaddmeta") @Expose var canaddmeta: Int? = null

    companion object {
        private const val serialVersionUID = 5373334880571009989L
    }
}