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

	@Expose
	@SerializedName("leftPop")
	private String leftPop;

	@Expose
	@SerializedName("rightPop")
	private String rightPop;


	public DailyForecastObj(boolean isSingle) {
		this.isSingle = isSingle;
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

	public String getLeftPop() {
		return leftPop;
	}

	public void setLeftPop(String leftPop) {
		this.leftPop = leftPop;
	}

	public String getRightPop() {
		return rightPop;
	}

	public void setRightPop(String rightPop) {
		this.rightPop = rightPop;
	}
}
