package com.lifedawn.bestweather.data.remote.retrofit.parameters.flickr

import android.util.ArrayMap
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient
import com.lifedawn.bestweather.data.remote.retrofit.parameters.RestRequestParameter

class FlickrGetInfoParameterRest : RestRequestParameter() {
    var photoId: String? = null
    var secret: String? = null
    val map: Map<String, String?>
        get() {
            val map: MutableMap<String, String?> = ArrayMap()
            map["method"] = "flickr.photos.getInfo"
            map["api_key"] = RetrofitClient.FLICKR_KEY
            map["photo_id"] = photoId
            map["secret"] = secret
            map["format"] = "json"
            map["nojsoncallback"] = "1"
            return map
        }
}