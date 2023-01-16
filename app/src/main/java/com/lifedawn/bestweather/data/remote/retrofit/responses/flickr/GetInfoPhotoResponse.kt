package com.lifedawn.bestweather.data.remote.retrofit.responses.flickr

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class GetInfoPhotoResponse : Serializable {
    @SerializedName("photo") @Expose var photo: Photo? = null
    @SerializedName("stat") @Expose var stat: String? = null

    companion object {
        private const val serialVersionUID = 536358442328479556L
    }
}