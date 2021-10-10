package com.lifedawn.bestweather.retrofit.parameters.flickr;

import android.util.ArrayMap;

import com.lifedawn.bestweather.retrofit.client.RetrofitClient;

import java.util.Map;

public class getPhotosFromGalleryParameter {
	private String galleryId;

	public Map<String, String> getMap() {
		Map<String, String> map = new ArrayMap<>();

		map.put("method", "flickr.galleries.getPhotos");
		map.put("api_key", RetrofitClient.FLICKR_KEY);
		map.put("gallery_id", galleryId);
		map.put("format", "json");
		return map;
	}

	public String getGalleryId() {
		return galleryId;
	}

	public void setGalleryId(String galleryId) {
		this.galleryId = galleryId;
	}
}
