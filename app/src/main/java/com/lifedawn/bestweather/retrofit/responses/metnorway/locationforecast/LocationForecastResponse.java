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
}
