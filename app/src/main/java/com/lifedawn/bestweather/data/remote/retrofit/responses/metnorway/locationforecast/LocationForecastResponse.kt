package com.lifedawn.bestweather.data.remote.retrofit.responses.metnorway.locationforecast;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationForecastResponse {
	@Expose
	@SerializedName("type")
	private String type;
	
	@Expose
	@SerializedName("geometry")
	private LocationForecastGeometry geometry;

	@Expose
	@SerializedName("properties")
	private LocationForecastProperties properties;
	
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

	public LocationForecastProperties getProperties() {
		return properties;
	}

	public void setProperties(LocationForecastProperties properties) {
		this.properties = properties;
	}
}
