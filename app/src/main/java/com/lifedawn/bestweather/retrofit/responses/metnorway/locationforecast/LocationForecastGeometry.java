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
}
