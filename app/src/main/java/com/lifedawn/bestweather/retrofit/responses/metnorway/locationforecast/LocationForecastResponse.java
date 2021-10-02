package com.lifedawn.bestweather.retrofit.responses.metnorway.locationforecast;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationForecastResponse {
	@Expose
	@SerializedName("type")
	private String type;
	
	@Expose
	@SerializedName("geometry")
	private LocationForecastGeometry geometry;
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public LocationForecastGeometry getGeometry() {
		return geometry;
	}
	
	public void setGeometry(LocationForecastGeometry geometry) {
		this.geometry = geometry;
	}
}
