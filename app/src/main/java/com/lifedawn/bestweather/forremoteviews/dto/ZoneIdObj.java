package com.lifedawn.bestweather.forremoteviews.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ZoneIdObj {
	@Expose
	@SerializedName("zoneId")
	private String zoneId;

	public String getZoneId() {
		return zoneId;
	}

	public void setZoneId(String zoneId) {
		this.zoneId = zoneId;
	}
}
