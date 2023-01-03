package com.lifedawn.bestweather.ui.flickr

import androidx.lifecycle.ViewModel
import com.lifedawn.bestweather.data.remote.flickr.FlickrRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FlickrViewModel @Inject constructor(flickrRepository: FlickrRepository) : ViewModel() {
}