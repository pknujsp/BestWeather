package com.lifedawn.bestweather.retrofit.responses.accuweather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WindGust {
	@Expose
	@SerializedName("Speed")
	private ValueUnit speed;

	public ValueUnit getSpeed() {
		return speed;
	}

	public void setSpeed(ValueUnit speed) {
		this.speed = speed;
	}
}