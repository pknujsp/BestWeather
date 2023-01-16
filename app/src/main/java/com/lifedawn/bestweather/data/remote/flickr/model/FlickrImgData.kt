package com.lifedawn.bestweather.data.remote.flickr.model

import android.graphics.Bitmap
import com.lifedawn.bestweather.data.remote.retrofit.responses.flickr.PhotosFromGalleryResponse
import java.io.Serializable

class FlickrImgData : Serializable {
    var img: Bitmap? = null
    var weather: String? = null
    var time: String? = null
    var volume: String? = null
    var photo: PhotosFromGalleryResponse.Photos.Photo? = null
        private set

    fun setPhoto(photo: PhotosFromGalleryResponse.Photos.Photo?): FlickrImgData {
        this.photo = photo
        return this
    }

    fun clear() {
        img!!.recycle()
        img = null
        weather = null
        time = null
        volume = null
        photo = null
    }

    val realFlickrUrl: String
        get() = "https://www.flickr.com/photos/" + photo!!.owner + "/" + photo!!.id
}