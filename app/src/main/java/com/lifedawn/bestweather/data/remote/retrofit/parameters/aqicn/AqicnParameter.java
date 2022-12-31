package com.lifedawn.bestweather.data.remote.retrofit.parameters.aqicn;

import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.data.remote.retrofit.parameters.RequestParameter;

import java.util.HashMap;
import java.util.Map;

public class AqicnParameter extends RequestParameter {
	private String latitude;
	private String longitude;

	public Map<String, String> getMap() {
		Map<String, String> map = new HashMap<>();
		map.put("token", RetrofitClient.AQICN_TOKEN);

		return map;
	}

	public String getLatitude() {
		return latitude;
	}

	public AqicnParameter setLatitude(String latitude) {
		this.latitude = latitude;
		return this;
	}

	public String getLongitude() {
		return longitude;
	}

	public AqicnParameter setLongitude(String longitude) {
		this.longitude = longitude;
		return this;
	}
}