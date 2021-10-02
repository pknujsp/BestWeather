package com.lifedawn.bestweather.retrofit.parameters.aqicn;

import com.lifedawn.bestweather.retrofit.client.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

public class AqicnParameter {
	private String latitude;
	private String longitude;
	
	public Map<String, String> getMap() {
		Map<String, String> map = new HashMap<>();
		map.put("token", RetrofitClient.AQICN_TOKEN);
		map.put("latitude", latitude);
		map.put("longitude", longitude);
		
		return map;
	}
	
	public String getLatitude() {
		return latitude;
	}
	
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	
	public String getLongitude() {
		return longitude;
	}
	
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
}
