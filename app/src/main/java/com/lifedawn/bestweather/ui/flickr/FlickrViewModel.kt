package com.lifedawn.bestweather.ui.flickr

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.lifedawn.bestweather.data.remote.flickr.repository.FlickrRepository
import com.lifedawn.bestweather.data.remote.flickr.FlickrRequestParameter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlickrViewModel @Inject constructor(private val flickrRepository: FlickrRepository) : ViewModel() {
    private val _img = MutableStateFlow<DownloadedImg>(DownloadedImg.Failure(Exception("")))
    val img = _img.asStateFlow()

    suspend fun loadImg(parameter: FlickrRequestParameter) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = flickrRepository.loadImg(parameter)

            if (response.isSuccess)
                response.onSuccess { _img.emit(DownloadedImg.Success(it)) }
            else
                response.onFailure { _img.emit(DownloadedImg.Failure(it)) }

        }
    }

    sealed class DownloadedImg {
        data class Success(val img: Bitmap) : DownloadedImg()
        data class Failure(val exception: Throwable) : DownloadedImg()
    }
}