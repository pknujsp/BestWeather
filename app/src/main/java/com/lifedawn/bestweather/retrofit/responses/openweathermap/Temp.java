package com.lifedawn.bestweather.retrofit.responses.openweathermap;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Temp {
	@Expose
	@SerializedName("day")
	private String day;

	@Expose
	@SerializedName("min")
	private String min;

	@Expose
	@SerializedName("max")
	private String max;

	@Expose
	@SerializedName("night")
	private String night;

	@Expose
	@SerializedName("eve")
	private String eve;

	@Expose
	@SerializedName("morn")
	private String morn;

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public String getMin() {
		return min;
	}

	public void setMin(String min) {
		this.min = min;
	}

	public String getMax() {
		return max;
	}

	public void setMax(String max) {
		this.max = max;
	}

	public String getNight() {
		return night;
	}

	public void setNight(String night) {
		this.night = night;
	}

	public String getEve() {
		return eve;
	}

	public void setEve(String eve) {
		this.eve = eve;
	}

	public String getMorn() {
		return morn;
	}

	public void setMorn(String morn) {
		this.morn = morn;
	}
}
