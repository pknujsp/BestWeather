package com.lifedawn.bestweather.retrofit.responses.metnorway.locationforecast.timeseries;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {
	@Expose
	@SerializedName("instant")
	private Instant instant;
	
	@Expose
	@SerializedName("next_12_hours")
	private Next12Hours next_12_hours;
	
	@Expose
	@SerializedName("next_1_hours")
	private Next1Hours next_1_hours;
	
	@Expose
	@SerializedName("next_6_hours")
	private Next6Hours next_6_hours;
	
}
