package com.lifedawn.bestweather.data.remote.flickr;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class FlickrViewModel extends AndroidViewModel {
	private final FlickrRepository flickrRepository = FlickrRepository.getINSTANCE();
	public final MutableLiveData<FlickrRepository.FlickrImgResponse> img = new MutableLiveData<>();
	private FlickrRepository.FlickrRequestParameter lastParameter;

	public FlickrRepository.FlickrRequestParameter getLastParameter() {
		return lastParameter;
	}

	public FlickrViewModel(@NonNull Application application) {
		super(application);
	}

	public FlickrViewModel setLastParameter(FlickrRepository.FlickrRequestParameter lastParameter) {
		this.lastParameter = lastParameter;
		return this;
	}

	public void loadImg(FlickrRepository.FlickrRequestParameter flickrRequestParameter) {
		lastParameter = flickrRequestParameter;
		flickrRepository.loadImg(getApplication().getApplicationContext(), flickrRequestParameter,
				img::postValue);
	}
}
