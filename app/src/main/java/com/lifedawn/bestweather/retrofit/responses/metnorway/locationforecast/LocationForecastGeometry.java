package com.lifedawn.bestweather.retrofit.responses.metnorway.locationforecast;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LocationForecastGeometry {
	@Expose
	@SerializedName("type")
	private String type;
	
	@Expose
	@SerializedName("coordinates")
	private List<String> coordinates;
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public List<String> getCoordinates() {
		return coordinates;
	}
	
	public void setCoordinates(List<String> coordinates) {
		this.coordinates = coordinates;
	}
}
