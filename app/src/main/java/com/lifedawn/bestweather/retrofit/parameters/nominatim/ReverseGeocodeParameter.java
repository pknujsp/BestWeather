package com.lifedawn.bestweather.retrofit.parameters.nominatim;

import android.util.ArrayMap;

import com.lifedawn.bestweather.retrofit.parameters.RequestParameter;

import java.util.Map;

public class ReverseGeocodeParameter extends RequestParameter {
	// format=geojson&lat=44.50155&lon=11.33989

	private final Double latitude;
	private final Double longitude;

	public Map<String, String> getMap() {
		Map<String, String> map = new ArrayMap<>();

		map.put("format", "geojson");
		map.put("lat", latitude.toString());
		map.put("lon", longitude.toString());
		map.put("zoom", "16");


		return map;
	}

	public ReverseGeocodeParameter(Double latitude, Double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

}
