package com.lifedawn.bestweather.forremoteviews.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HourlyForecastObj {
	@Expose
	@SerializedName("successful")
	private boolean successful;

	@Expose
	@SerializedName("clock")
	private String clock;

	@Expose
	@SerializedName("temp")
	private String temp;

	@Expose
	@SerializedName("weatherIcon")
	private int weatherIcon;

	public HourlyForecastObj(boolean successful) {
		this.successful = successful;
	}

	public HourlyForecastObj() {

	}

	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public String getClock() {
		return clock;
	}

	public void setClock(String clock) {
		this.clock = clock;
	}

	public String getTemp() {
		return temp;
	}

	public void setTemp(String temp) {
		this.temp = temp;
	}

	public int getWeatherIcon() {
		return weatherIcon;
	}

	public void setWeatherIcon(int weatherIcon) {
		this.weatherIcon = weatherIcon;
	}
}
