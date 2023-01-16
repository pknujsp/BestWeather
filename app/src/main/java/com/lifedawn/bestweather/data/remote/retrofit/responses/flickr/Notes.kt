package com.lifedawn.bestweather.data.remote.retrofit.responses.flickr

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Notes : Serializable {
    @SerializedName("note") @Expose var note: List<Any>? = null

    companion object {
        private const val serialVersionUID = 7574167542211636876L
    }
}