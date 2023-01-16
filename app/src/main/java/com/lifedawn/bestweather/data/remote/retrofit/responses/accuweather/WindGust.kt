package com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WindGust {
	@Expose
	@SerializedName("Speed")
	private ValuesUnit speed;

	public ValuesUnit getSpeed() {
		return speed;
	}

	public void setSpeed(ValuesUnit speed) {
		this.speed = speed;
	}
}