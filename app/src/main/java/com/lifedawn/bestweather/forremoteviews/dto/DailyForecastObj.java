package com.lifedawn.bestweather.forremoteviews.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DailyForecastObj {
	@Expose
	@SerializedName("successful")
	private boolean successful;

	@Expose
	@SerializedName("isSingle")
	private boolean isSingle;

	@Expose
	@SerializedName("date")
	private String date;

	@Expose
	@SerializedName("minTemp")
	private String minTemp;

	@Expose
	@SerializedName("maxTemp")
	private String maxTemp;

	@Expose
	@SerializedName("leftWeatherIcon")
	private int leftWeatherIcon;

	@Expose
	@SerializedName("rightWeatherIcon")
	private int rightWeatherIcon;

	public DailyForecastObj(boolean successful, boolean isSingle) {
		this.successful = successful;
		this.isSingle = isSingle;
	}

	public DailyForecastObj() {
	}

	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public boolean isSingle() {
		return isSingle;
	}

	public void setSingle(boolean single) {
		isSingle = single;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getMinTemp() {
		return minTemp;
	}

	public void setMinTemp(String minTemp) {
		this.minTemp = minTemp;
	}

	public String getMaxTemp() {
		return maxTemp;
	}

	public void setMaxTemp(String maxTemp) {
		this.maxTemp = maxTemp;
	}

	public int getLeftWeatherIcon() {
		return leftWeatherIcon;
	}

	public void setLeftWeatherIcon(int leftWeatherIcon) {
		this.leftWeatherIcon = leftWeatherIcon;
	}

	public int getRightWeatherIcon() {
		return rightWeatherIcon;
	}

	public void setRightWeatherIcon(int rightWeatherIcon) {
		this.rightWeatherIcon = rightWeatherIcon;
	}
}
