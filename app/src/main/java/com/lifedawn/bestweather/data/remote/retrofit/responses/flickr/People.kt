package com.lifedawn.bestweather.data.remote.retrofit.responses.flickr

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class People : Serializable {
    @SerializedName("haspeople") @Expose var haspeople: Int? = null

    companion object {
        private const val serialVersionUID = -6712516251128857555L
    }
}