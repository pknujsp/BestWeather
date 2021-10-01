package com.lifedawn.bestweather.retrofit.responses.metnorway.locationforecast.timeseries;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Next6Hours {
	@Expose
	@SerializedName("summary")
	private Summary summary;
	
	@Expose
	@SerializedName("details")
	private Details details;
}
