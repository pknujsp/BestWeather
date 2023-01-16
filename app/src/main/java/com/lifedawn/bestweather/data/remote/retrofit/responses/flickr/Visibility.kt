package com.lifedawn.bestweather.data.remote.retrofit.responses.flickr

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Visibility : Serializable {
    @SerializedName("ispublic") @Expose var ispublic: Int? = null
    @SerializedName("isfriend") @Expose var isfriend: Int? = null
    @SerializedName("isfamily") @Expose var isfamily: Int? = null

    companion object {
        private const val serialVersionUID = -8291273102820984400L
    }
}