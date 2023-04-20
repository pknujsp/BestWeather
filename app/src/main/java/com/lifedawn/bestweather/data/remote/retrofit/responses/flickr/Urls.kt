package com.lifedawn.bestweather.data.remote.retrofit.responses.flickr

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Urls : Serializable {
    @SerializedName("url") @Expose var url: List<Url>? = null

    companion object {
        private const val serialVersionUID = -7370851300232911951L
    }
}