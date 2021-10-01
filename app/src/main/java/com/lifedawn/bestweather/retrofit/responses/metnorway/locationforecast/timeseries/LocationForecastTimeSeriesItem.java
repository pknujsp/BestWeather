package com.lifedawn.bestweather.retrofit.responses.metnorway.locationforecast.timeseries;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationForecastTimeSeriesItem {
	@Expose
	@SerializedName("time")
	private String time;
	
	@Expose
	@SerializedName("data")
	private Data data;
}
