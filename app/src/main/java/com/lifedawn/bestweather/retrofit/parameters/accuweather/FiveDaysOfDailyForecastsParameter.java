package com.lifedawn.bestweather.retrofit.parameters.accuweather;

import com.lifedawn.bestweather.retrofit.client.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

public class FiveDaysOfDailyForecastsParameter {
	private String locationId;
	private final String details = "True";
	private final String metric = "True";
	
	public Map<String, String> getMap() {
		Map<String, String> map = new HashMap<>();
		map.put("apiKey", RetrofitClient.ACCU_WEATHER_SERVICE_KEY);
		map.put("details", details);
		map.put("locationId", locationId);
		map.put("metric", metric);
		
		return map;
	}
	
	public String getLocationId() {
		return locationId;
	}
	
	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}
	
}
