package com.lifedawn.bestweather.retrofit.responses.metnorway.locationforecast.timeseries;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Next1Hours {
	@Expose
	@SerializedName("summary")
	private Summary summary;
	
	@Expose
	@SerializedName("details")
	private Details details;
	
	public Summary getSummary() {
		return summary;
	}
	
	public void setSummary(Summary summary) {
		this.summary = summary;
	}
	
	public Details getDetails() {
		return details;
	}
	
	public void setDetails(Details details) {
		this.details = details;
	}
}
