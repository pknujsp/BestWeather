package com.lifedawn.bestweather.flickr;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FlickrViewModel extends AndroidViewModel {
	private final FlickrRepository flickrRepository = FlickrRepository.getINSTANCE();
	public final MutableLiveData<FlickrRepository.FlickrImgResponse> imgLiveData = new MutableLiveData<>();
	private FlickrRepository.FlickrRequestParameter lastParameter;

	public FlickrRepository.FlickrRequestParameter getLastParameter() {
		return lastParameter;
	}

	public FlickrViewModel(@NonNull Application application) {
		super(application);
	}

	public void loadImg(FlickrRepository.FlickrRequestParameter flickrRequestParameter) {
		lastParameter = flickrRequestParameter;
		flickrRepository.loadImg(getApplication().getApplicationContext(), flickrRequestParameter,
				new FlickrRepository.GlideImgCallback() {
			@Override
			public void onLoadedImg(FlickrRepository.FlickrImgResponse flickrImgResponse) {
				imgLiveData.postValue(flickrImgResponse);
			}
		});
	}
}
