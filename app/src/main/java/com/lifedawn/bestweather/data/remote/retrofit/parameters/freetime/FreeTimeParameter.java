package com.lifedawn.bestweather.data.remote.retrofit.parameters.freetime;

import android.util.ArrayMap;

import com.lifedawn.bestweather.data.remote.retrofit.parameters.RequestParameter;

import java.util.Map;

public class FreeTimeParameter extends RequestParameter {
	private final Double latitude;
	private final Double longitude;

	public Map<String, String> getMap() {
		Map<String, String> map = new ArrayMap<>();

		map.put("latitude", latitude.toString());
		map.put("longitude", longitude.toString());

		return map;
	}

	public FreeTimeParameter(Double latitude, Double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

}
