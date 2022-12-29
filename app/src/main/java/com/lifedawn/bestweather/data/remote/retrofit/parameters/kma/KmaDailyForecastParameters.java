package com.lifedawn.bestweather.data.remote.retrofit.parameters.kma;

import com.lifedawn.bestweather.data.remote.retrofit.parameters.RequestParameter;

public class KmaDailyForecastParameters extends RequestParameter {
	private final String unit = "m%2Fs";
	private final String hr1 = "Y";
	private final String ext = "N";
	private final String code;
	private double latitude;
	private double longitude;

	public KmaDailyForecastParameters(String code) {
		this.code = code;
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

	public String getCode() {
		return code;
	}
}
