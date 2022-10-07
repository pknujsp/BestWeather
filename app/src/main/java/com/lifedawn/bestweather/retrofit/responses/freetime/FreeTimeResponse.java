package com.lifedawn.bestweather.retrofit.responses.freetime;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FreeTimeResponse {
	@Expose
	@SerializedName("timeZone")
	private String timezone;

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getTimezone() {
		return timezone;
	}
}
