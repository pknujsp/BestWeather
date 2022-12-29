package com.lifedawn.bestweather.data.remote.retrofit.parameters.metnorway;

import android.util.ArrayMap;

import com.lifedawn.bestweather.data.remote.retrofit.parameters.RequestParameter;

import java.util.Map;

public class LocationForecastParameter extends RequestParameter {
	private String latitude;
	private String longitude;

	public Map<String, String> getMap() {
		Map<String, String> map = new ArrayMap<>();

		map.put("lat", latitude);
		map.put("lon", longitude);
		return map;
	}

	public String getLatitude() {
		return latitude;
	}

	public LocationForecastParameter setLatitude(String latitude) {
		this.latitude = latitude;
		return this;
	}

	public String getLongitude() {
		return longitude;
	}

	public LocationForecastParameter setLongitude(String longitude) {
		this.longitude = longitude;
		return this;

	}
}
