package com.lifedawn.bestweather.retrofit.responses.metnorway.locationforecast.timeseries;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Summary {
	@Expose
	@SerializedName("symbol_code")
	private String symbolCode;
}
