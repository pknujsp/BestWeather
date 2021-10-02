package com.lifedawn.bestweather.retrofit.responses.kma.ultrasrtncstresponse;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.kma.kmacommons.KmaRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.vilagefcstcommons.VilageFcstResponse;

public class UltraSrtNcstRoot extends KmaRoot {
	@Expose
	@SerializedName("response")
	private VilageFcstResponse response;
	
	
	public void setResponse(VilageFcstResponse response) {
		this.response = response;
	}
	
	public VilageFcstResponse getResponse() {
		return response;
	}
}
