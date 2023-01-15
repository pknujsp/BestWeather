package com.lifedawn.bestweather.data.remote.retrofit.parameters.freetime;

import android.util.ArrayMap;

import com.lifedawn.bestweather.data.remote.retrofit.parameters.RestRequestParameter;

import java.util.Map;

public class FreeTimeParameterRest extends RestRequestParameter {
	private final Double latitude;
	private final Double longitude;

	public Map<String, String> getMap() {
		Map<String, String> map = new ArrayMap<>();

		map.put("latitude", latitude.toString());
		map.put("longitude", longitude.toString());

		return map;
	}

	public FreeTimeParameterRest(Double latitude, Double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

}
