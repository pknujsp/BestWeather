package com.lifedawn.bestweather.data.remote.retrofit.parameters.accuweather;

import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.data.remote.retrofit.parameters.RestRequestParameter;

import java.util.HashMap;
import java.util.Map;

public class GeoPositionSearchParameterRest extends RestRequestParameter {
	private String latitude;
	private String longitude;

	public Map<String, String> getMap() {
		Map<String, String> map = new HashMap<>();
		map.put("apikey", RetrofitClient.ACCU_WEATHER_SERVICE_KEY);
		map.put("q", latitude + "," + longitude);

		return map;
	}

	public String getLatitude() {
		return latitude;
	}

	public GeoPositionSearchParameterRest setLatitude(String latitude) {
		this.latitude = latitude;
		return this;
	}

	public String getLongitude() {
		return longitude;
	}

	public GeoPositionSearchParameterRest setLongitude(String longitude) {
		this.longitude = longitude;
		return this;
	}
}
