package com.lifedawn.bestweather.retrofit.parameters.openweathermap.onecall;

import android.util.ArrayMap;

import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.RequestParameter;

import java.util.Map;
import java.util.Set;

public class OneCallParameter extends RequestParameter {
	private String latitude;
	private String longitude;
	private Set<OneCallApis> oneCallApis;

	public enum OneCallApis {
		current, minutely, hourly, daily, alerts
	}

	public Map<String, String> getMap() {
		Map<String, String> map = new ArrayMap<>();

		map.put("lat", latitude);
		map.put("lon", longitude);
		map.put("units", "metric");

		if (oneCallApis != null) {
			if (!oneCallApis.isEmpty()) {
				StringBuilder stringBuilder = new StringBuilder();
				for (OneCallApis exclude : oneCallApis) {
					stringBuilder.append(exclude.toString()).append(",");
				}
				stringBuilder.deleteCharAt(stringBuilder.length() - 1);
				map.put("exclude", stringBuilder.toString());
			}
		}

		map.put("appid", RetrofitClient.OWM_ONECALL_API_KEY);

		return map;
	}

	public String getLatitude() {
		return latitude;
	}

	public OneCallParameter setLatitude(String latitude) {
		this.latitude = latitude;
		return this;
	}

	public String getLongitude() {
		return longitude;
	}

	public OneCallParameter setLongitude(String longitude) {
		this.longitude = longitude;
		return this;

	}

	public Set<OneCallApis> getOneCallApis() {
		return oneCallApis;
	}

	public OneCallParameter setOneCallApis(Set<OneCallApis> oneCallApis) {
		this.oneCallApis = oneCallApis;
		return this;

	}
}
