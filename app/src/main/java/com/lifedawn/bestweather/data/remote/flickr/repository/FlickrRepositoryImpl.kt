package com.lifedawn.bestweather.data.remote.flickr.repository

import android.content.Context
import android.graphics.Bitmap
import com.lifedawn.bestweather.data.remote.flickr.FlickrRequestParameter
import com.lifedawn.bestweather.ui.weathers.dataprocessing.response.*
import javax.inject.Inject

class FlickrRepositoryImpl @Inject constructor(context: Context) : FlickrRepository {

    init {
        FlickrUtil.init()
    }

    override suspend fun loadImg(flickrRequestParameter: FlickrRequestParameter): Result<Bitmap> {
        TODO("Not yet implemented")
    }

}