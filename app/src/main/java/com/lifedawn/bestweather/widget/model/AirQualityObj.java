package com.lifedawn.bestweather.widget.model;

public class AirQualityObj {
	private String aqi;	private boolean isSuccessful;


	public String getAqi() {
		return aqi;
	}

	public void setAqi(String aqi) {
		this.aqi = aqi;
	}

	public boolean isSuccessful() {
		return isSuccessful;
	}

	public AirQualityObj setSuccessful(boolean successful) {
		isSuccessful = successful;
		return this;
	}
}
