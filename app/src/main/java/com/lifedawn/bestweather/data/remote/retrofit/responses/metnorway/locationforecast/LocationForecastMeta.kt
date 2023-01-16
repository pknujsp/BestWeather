package com.lifedawn.bestweather.data.remote.retrofit.responses.metnorway.locationforecast;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationForecastMeta {
	@Expose
	@SerializedName("updated_at")
	private String updatedAt;
	
	@Expose
	@SerializedName("units")
	private LocationForecastUnits units;
	
	public String getUpdatedAt() {
		return updatedAt;
	}
	
	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}
	
	public LocationForecastUnits getUnits() {
		return units;
	}
	
	public void setUnits(LocationForecastUnits units) {
		this.units = units;
	}
}
