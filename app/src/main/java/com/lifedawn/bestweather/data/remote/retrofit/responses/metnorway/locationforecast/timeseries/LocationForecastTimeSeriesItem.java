package com.lifedawn.bestweather.data.remote.retrofit.responses.metnorway.locationforecast.timeseries;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationForecastTimeSeriesItem {
	@Expose
	@SerializedName("time")
	private String time;
	
	@Expose
	@SerializedName("data")
	private Data data;
	
	public String getTime() {
		return time;
	}
	
	public void setTime(String time) {
		this.time = time;
	}
	
	public Data getData() {
		return data;
	}
	
	public void setData(Data data) {
		this.data = data;
	}
}
