package com.lifedawn.bestweather.data.remote.retrofit.responses.flickr

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class PhotosFromGalleryResponse : Serializable {
    @Expose @SerializedName("photos") var photos: Photos? = null
    @Expose @SerializedName("stat") var stat: String? = null
        private set

    fun setStat(stat: String?): PhotosFromGalleryResponse {
        this.stat = stat
        return this
    }

    class Photos {
        @Expose @SerializedName("page") var page: String? = null
        @Expose @SerializedName("pages") var pages: String? = null
        @Expose @SerializedName("perpage") var perPage: String? = null
        @Expose @SerializedName("total") var total: String? = null
        @Expose @SerializedName("photo") var photo: List<Photo>? = null

        class Photo {
            @Expose @SerializedName("id") var id: String? = null
            @Expose @SerializedName("owner") var owner: String? = null
            @Expose @SerializedName("secret") var secret: String? = null
            @Expose @SerializedName("server") var server: String? = null
            @Expose @SerializedName("farm") var farm: String? = null
            @Expose @SerializedName("title") var title: String? = null
            @Expose @SerializedName("ispublic") var isPublic: String? = null
            @Expose @SerializedName("isfriend") var isFriend: String? = null
            @Expose @SerializedName("isfamily") var isFamily: String? = null
            @Expose @SerializedName("is_primary") var isPrimary: String? = null
            @Expose @SerializedName("has_comment") var hasComment: String? = null
        }
    }
}