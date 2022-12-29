package com.lifedawn.bestweather.data.remote.retrofit.parameters.openweathermap.individual;

import android.util.ArrayMap;

import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.data.remote.retrofit.parameters.RequestParameter;

import java.util.Map;

public class OwmCurrentWeatherParameter extends RequestParameter {
	private String latitude;
	private String longitude;

	public Map<String, String> getMap() {
		Map<String, String> map = new ArrayMap<>();

		map.put("lat", latitude);
		map.put("lon", longitude);
		map.put("units", "metric");
		map.put("appid", RetrofitClient.OWM_ONECALL_API_KEY);

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
