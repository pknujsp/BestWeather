package com.lifedawn.bestweather.retrofit.responses.accuweather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ValuesUnit {
	@Expose
	@SerializedName("Metric")
	private ValueUnit metric;
	
	@Expose
	@SerializedName("Imperial")
	private ValueUnit imperial;
	
	public ValueUnit getMetric() {
		return metric;
	}
	
	public ValuesUnit setMetric(ValueUnit metric) {
		this.metric = metric;
		return this;
	}
	
	public ValueUnit getImperial() {
		return imperial;
	}
	
	public ValuesUnit setImperial(ValueUnit imperial) {
		this.imperial = imperial;
		return this;
	}
}
