package com.lifedawn.bestweather.retrofit.parameters.kma;

import com.lifedawn.bestweather.retrofit.parameters.RequestParameter;

import java.util.HashMap;
import java.util.Map;

public class KmaHourlyForecastRequestParameters extends RequestParameter {
	private final String unit = "m%2Fs";
	private final String hr1 = "Y";
	private final String ext = "N";
	private final String code;
	private double latitude;
	private double longitude;

	public KmaHourlyForecastRequestParameters(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public Map<String, String> getParametersMap() {
		Map<String, String> map = new HashMap<>();
		map.put("unit", unit);
		map.put("hr1", hr1);
		map.put("ext", ext);
		map.put("code", code);
		return map;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
}
