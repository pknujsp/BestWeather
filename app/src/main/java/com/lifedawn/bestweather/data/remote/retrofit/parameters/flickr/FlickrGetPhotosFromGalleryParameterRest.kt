package com.lifedawn.bestweather.data.remote.retrofit.parameters.flickr

import android.util.ArrayMap
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient
import com.lifedawn.bestweather.data.remote.retrofit.parameters.RestRequestParameter

class FlickrGetPhotosFromGalleryParameterRest : RestRequestParameter() {
    var galleryId: String? = null
    val map: Map<String, String?>
        get() {
            val map: MutableMap<String, String?> = ArrayMap()
            map["method"] = "flickr.galleries.getPhotos"
            map["api_key"] = RetrofitClient.FLICKR_KEY
            map["gallery_id"] = galleryId
            map["format"] = "json"
            map["nojsoncallback"] = "1"
            return map
        }
}