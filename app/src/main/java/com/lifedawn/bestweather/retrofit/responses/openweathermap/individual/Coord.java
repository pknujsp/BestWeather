package com.lifedawn.bestweather.retrofit.responses.openweathermap.individual;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Coord {
	@Expose
	@SerializedName("lon")
	private String lon;

	@Expose
	@SerializedName("lat")
	private String lat;

	public String getLon() {
		return lon;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}
}