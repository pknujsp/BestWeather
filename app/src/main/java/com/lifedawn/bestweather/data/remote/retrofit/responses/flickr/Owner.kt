package com.lifedawn.bestweather.data.remote.retrofit.responses.flickr

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Owner : Serializable {
    @SerializedName("nsid") @Expose var nsid: String? = null
    @SerializedName("username") @Expose var username: String? = null
    @SerializedName("realname") @Expose var realname: String? = null
    @SerializedName("location") @Expose var location: String? = null
    @SerializedName("iconserver") @Expose var iconserver: String? = null
    @SerializedName("iconfarm") @Expose var iconfarm: String? = null
    @SerializedName("path_alias") @Expose var pathAlias: String? = null

    companion object {
        const val serialVersionUID = -6339607373779670021L
    }
}