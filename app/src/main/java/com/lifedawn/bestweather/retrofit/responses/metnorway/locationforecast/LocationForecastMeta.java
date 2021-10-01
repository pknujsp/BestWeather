package com.lifedawn.bestweather.retrofit.responses.metnorway.locationforecast;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationForecastMeta {
	@Expose
	@SerializedName("updated_at")
	private String updatedAt;
	
	@Expose
	@SerializedName("units")
	private LocationForecastUnits units;
}
