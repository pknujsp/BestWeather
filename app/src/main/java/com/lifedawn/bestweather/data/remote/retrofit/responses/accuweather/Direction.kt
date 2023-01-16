package com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Direction {
	@Expose
	@SerializedName("Degrees")
	private String degrees;

	@Expose
	@SerializedName("Localized")
	private String localized;

	@Expose
	@SerializedName("English")
	private String english;

	public String getDegrees() {
		return degrees;
	}

	public void setDegrees(String degrees) {
		this.degrees = degrees;
	}

	public String getLocalized() {
		return localized;
	}

	public void setLocalized(String localized) {
		this.localized = localized;
	}

	public String getEnglish() {
		return english;
	}

	public void setEnglish(String english) {
		this.english = english;
	}
}
