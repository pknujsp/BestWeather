package com.lifedawn.bestweather.retrofit.responses.kma.json.ultrasrtfcstresponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.kma.json.vilagefcstcommons.VilageFcstResponse;

public class UltraSrtFcstRoot {
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
