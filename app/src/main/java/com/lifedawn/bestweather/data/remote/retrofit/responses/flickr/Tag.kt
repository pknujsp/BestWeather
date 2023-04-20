package com.lifedawn.bestweather.data.remote.retrofit.responses.flickr

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Tag : Serializable {
    @SerializedName("id") @Expose var id: String? = null
    @SerializedName("author") @Expose var author: String? = null
    @SerializedName("authorname") @Expose var authorname: String? = null
    @SerializedName("raw") @Expose var raw: String? = null
    @SerializedName("_content") @Expose var content: String? = null
    @SerializedName("machine_tag") @Expose var machineTag: String? = null

    companion object {
        const val serialVersionUID = -5059050205966008593L
    }
}