package com.lifedawn.bestweather.retrofit.responses.accuweather;

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
}
