package com.lifedawn.bestweather.retrofit.parameters.openweathermap;

import android.util.ArrayMap;

import com.lifedawn.bestweather.retrofit.client.RetrofitClient;

import java.util.Map;

public class DailyForecastParameter {
	private String latitude;
	private String longitude;
	private String cnt;

	public Map<String, String> getMap() {
		Map<String, String> map = new ArrayMap<>();

		map.put("lat", latitude);
		map.put("lon", longitude);
		map.put("cnt", cnt);
		map.put("appid", RetrofitClient.OWM_API_KEY);

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

	public String getCnt() {
		return cnt;
	}

	public void setCnt(String cnt) {
		this.cnt = cnt;
	}
}
