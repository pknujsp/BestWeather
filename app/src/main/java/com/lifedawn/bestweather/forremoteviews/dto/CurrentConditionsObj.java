package com.lifedawn.bestweather.forremoteviews.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CurrentConditionsObj {
	@Expose
	@SerializedName("successful")
	private boolean successful;

	@Expose
	@SerializedName("temp")
	private String temp;

	@Expose
	@SerializedName("realFeelTemp")
	private String realFeelTemp;

	@Expose
	@SerializedName("precipitation")
	private String precipitation;

	@Expose
	@SerializedName("airQuality")
	private String airQuality;

	@Expose
	@SerializedName("weatherIcon")
	private int weatherIcon;

	@Expose
	@SerializedName("zoneId")
	private String zoneId;

	public CurrentConditionsObj(boolean successful) {
		this.successful = successful;
	}

	public CurrentConditionsObj() {
	}

	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public String getTemp() {
		return temp;
	}

	public void setTemp(String temp) {
		this.temp = temp;
	}

	public String getRealFeelTemp() {
		return realFeelTemp;
	}

	public void setRealFeelTemp(String realFeelTemp) {
		this.realFeelTemp = realFeelTemp;
	}

	public String getPrecipitation() {
		return precipitation;
	}

	public void setPrecipitation(String precipitation) {
		this.precipitation = precipitation;
	}

	public String getAirQuality() {
		return airQuality;
	}

	public void setAirQuality(String airQuality) {
		this.airQuality = airQuality;
	}

	public int getWeatherIcon() {
		return weatherIcon;
	}

	public void setWeatherIcon(int weatherIcon) {
		this.weatherIcon = weatherIcon;
	}

	public void setZoneId(String zoneId) {
		this.zoneId = zoneId;
	}

	public String getZoneId() {
		return zoneId;
	}
}
