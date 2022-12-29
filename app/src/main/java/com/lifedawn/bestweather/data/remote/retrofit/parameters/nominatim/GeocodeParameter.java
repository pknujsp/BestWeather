package com.lifedawn.bestweather.data.remote.retrofit.parameters.nominatim;

import android.util.ArrayMap;

import com.lifedawn.bestweather.data.remote.retrofit.parameters.RequestParameter;

import java.util.Map;

public class GeocodeParameter extends RequestParameter {

	private final String query;

	public Map<String, String> getMap() {
		Map<String, String> map = new ArrayMap<>();

		map.put("q", query);
		map.put("format", "geojson");
		map.put("addressdetails", "1");


		return map;
	}

	public GeocodeParameter(String query) {
		this.query = query;
	}

}
