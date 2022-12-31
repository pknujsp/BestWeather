package com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.midlandfcstresponse;

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