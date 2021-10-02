package com.lifedawn.bestweather.retrofit.responses.metnorway.locationforecast.timeseries;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {
	@Expose
	@SerializedName("instant")
	private Instant instant;
	
	@Expose
	@SerializedName("next_12_hours")
	private Next12Hours next_12_hours;
	
	@Expose
	@SerializedName("next_1_hours")
	private Next1Hours next_1_hours;
	
	@Expose
	@SerializedName("next_6_hours")
	private Next6Hours next_6_hours;
	
	public Instant getInstant() {
		return instant;
	}
	
	public void setInstant(Instant instant) {
		this.instant = instant;
	}
	
	public Next12Hours getNext_12_hours() {
		return next_12_hours;
	}
	
	public void setNext_12_hours(Next12Hours next_12_hours) {
		this.next_12_hours = next_12_hours;
	}
	
	public Next1Hours getNext_1_hours() {
		return next_1_hours;
	}
	
	public void setNext_1_hours(Next1Hours next_1_hours) {
		this.next_1_hours = next_1_hours;
	}
	
	public Next6Hours getNext_6_hours() {
		return next_6_hours;
	}
	
	public void setNext_6_hours(Next6Hours next_6_hours) {
		this.next_6_hours = next_6_hours;
	}
}
