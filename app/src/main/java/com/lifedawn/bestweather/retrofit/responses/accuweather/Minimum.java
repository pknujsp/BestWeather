package com.lifedawn.bestweather.retrofit.responses.accuweather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Minimum {
	@Expose
	@SerializedName("Metric")
	private ValueUnit metric;

	@Expose
	@SerializedName("Imperial")
	private ValueUnit imperial;

	public ValueUnit getMetric() {
		return metric;
	}

	public void setMetric(ValueUnit metric) {
		this.metric = metric;
	}

	public ValueUnit getImperial() {
		return imperial;
	}

	public void setImperial(ValueUnit imperial) {
		this.imperial = imperial;
	}
}
