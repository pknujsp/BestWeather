package com.lifedawn.bestweather.retrofit.responses.kma.midlandfcstresponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MidLandFcstRoot {
	@Expose
	@SerializedName("response")
	private MidLandFcstResponse response;
	
	public void setResponse(MidLandFcstResponse response) {
		this.response = response;
	}
	
	public MidLandFcstResponse getResponse() {
		return response;
	}
}
