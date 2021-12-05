package com.lifedawn.bestweather.forremoteviews.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HourlyForecastObj {
	@Expose
	@SerializedName("clock")
	private String clock;

	@Expose
	@SerializedName("temp")
	private String temp;

	@Expose
	@SerializedName("weatherIcon")
	private int weatherIcon;


	public HourlyForecastObj() {

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
