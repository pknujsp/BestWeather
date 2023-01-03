package com.lifedawn.bestweather.data.remote.flickr

import android.content.Context

interface FlickrRepository {
    suspend fun loadImg(
        flickrRequestParameter: FlickrRepositoryImpl.FlickrRequestParameter
    ): FlickrRepositoryImpl.FlickrImgResponse

    fun cancelAllRequests()
}