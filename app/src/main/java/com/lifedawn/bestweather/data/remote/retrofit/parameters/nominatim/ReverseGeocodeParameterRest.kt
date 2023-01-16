package com.lifedawn.bestweather.data.remote.retrofit.parameters.nominatim;

import android.util.ArrayMap;

import com.lifedawn.bestweather.data.remote.retrofit.parameters.RestRequestParameter;

import java.util.Map;

public class ReverseGeocodeParameterRest extends RestRequestParameter {
	// format=geojson&lat=44.50155&lon=11.33989

	private final Double latitude;
	private final Double longitude;

	public Map<String, String> getMap() {
		Map<String, String> map = new ArrayMap<>();

		map.put("format", "geojson");
		map.put("lat", latitude.toString());
		map.put("lon", longitude.toString());
		map.put("zoom", "14");


		return map;
	}

	public ReverseGeocodeParameterRest(Double latitude, Double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

}
