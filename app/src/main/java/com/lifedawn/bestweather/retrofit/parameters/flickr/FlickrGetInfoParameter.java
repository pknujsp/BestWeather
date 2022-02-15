package com.lifedawn.bestweather.retrofit.parameters.flickr;

import android.util.ArrayMap;

import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.RequestParameter;

import java.util.Map;

public class FlickrGetInfoParameter extends RequestParameter {
	private String photoId;
	private String secret;

	public Map<String, String> getMap() {
		Map<String, String> map = new ArrayMap<>();

		map.put("method", "flickr.photos.getInfo");
		map.put("api_key", RetrofitClient.FLICKR_KEY);
		map.put("photo_id", photoId);
		map.put("secret", secret);
		map.put("format", "json");
		map.put("nojsoncallback", "1");
		return map;
	}

	public String getPhotoId() {
		return photoId;
	}

	public void setPhotoId(String photoId) {
		this.photoId = photoId;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}
}