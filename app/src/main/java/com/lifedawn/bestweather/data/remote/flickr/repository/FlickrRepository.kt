package com.lifedawn.bestweather.data.remote.flickr.repository

import android.graphics.Bitmap
import com.lifedawn.bestweather.data.remote.flickr.FlickrRequestParameter

interface FlickrRepository {
    suspend fun loadImg(
        flickrRequestParameter: FlickrRequestParameter
    ): Result<Bitmap>
}