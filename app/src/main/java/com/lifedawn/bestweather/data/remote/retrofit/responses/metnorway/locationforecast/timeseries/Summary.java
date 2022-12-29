package com.lifedawn.bestweather.data.remote.retrofit.responses.metnorway.locationforecast.timeseries;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Summary {
	@Expose
	@SerializedName("symbol_code")
	private String symbolCode;
	
	public String getSymbolCode() {
		return symbolCode;
	}
	
	public void setSymbolCode(String symbolCode) {
		this.symbolCode = symbolCode;
	}
}
