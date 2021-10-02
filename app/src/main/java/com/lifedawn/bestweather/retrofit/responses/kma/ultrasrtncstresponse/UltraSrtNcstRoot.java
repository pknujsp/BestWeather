package com.lifedawn.bestweather.retrofit.responses.kma.ultrasrtncstresponse;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.kma.VilageFcstResponse;

public class UltraSrtNcstRoot {
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
