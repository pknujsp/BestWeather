package com.lifedawn.bestweather.retrofit.responses.accuweather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Wind {
	@Expose
	@SerializedName("Speed")
	private ValueUnit speed;

	@Expose
	@SerializedName("Direction")
	private Direction direction;

	public ValueUnit getSpeed() {
		return speed;
	}

	public void setSpeed(ValueUnit speed) {
		this.speed = speed;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}
}
