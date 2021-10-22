package com.lifedawn.bestweather.retrofit.responses.accuweather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Wind {
	@Expose
	@SerializedName("Speed")
	private ValuesUnit speed;
	
	@Expose
	@SerializedName("Direction")
	private Direction direction;
	
	public ValuesUnit getSpeed() {
		return speed;
	}
	
	public Wind setSpeed(ValuesUnit speed) {
		this.speed = speed;
		return this;
	}
	
	public Direction getDirection() {
		return direction;
	}
	
	public void setDirection(Direction direction) {
		this.direction = direction;
	}
}
