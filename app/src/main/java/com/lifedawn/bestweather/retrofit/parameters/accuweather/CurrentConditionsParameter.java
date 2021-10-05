package com.lifedawn.bestweather.retrofit.parameters.accuweather;

import com.lifedawn.bestweather.retrofit.client.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

public class CurrentConditionsParameter {
	private String locationKey;
	private final String details = "True";
	private final String metric = "True";

	public Map<String, String> getMap() {
		Map<String, String> map = new HashMap<>();
		map.put("apikey", RetrofitClient.ACCU_WEATHER_SERVICE_KEY);
		map.put("details", details);
		map.put("metric", metric);

		return map;
	}

	public String getLocationKey() {
		return locationKey;
	}

	public void setLocationKey(String locationKey) {
		this.locationKey = locationKey;
	}

}
