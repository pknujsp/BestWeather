package com.lifedawn.bestweather.retrofit.parameters.accuweather;

import com.lifedawn.bestweather.retrofit.client.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

public class GeoPositionSearchParameter {
	private String latitude;
	private String longitude;
	
	public Map<String, String> getMap() {
		Map<String, String> map = new HashMap<>();
		map.put("apiKey", RetrofitClient.ACCU_WEATHER_SERVICE_KEY);
		map.put("q", latitude + "," + longitude);
		
		return map;
	}
	
}
