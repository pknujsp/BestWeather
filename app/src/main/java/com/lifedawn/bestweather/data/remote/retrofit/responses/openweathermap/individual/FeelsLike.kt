package com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FeelsLike {
	@Expose
	@SerializedName("day")
	private String day;

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