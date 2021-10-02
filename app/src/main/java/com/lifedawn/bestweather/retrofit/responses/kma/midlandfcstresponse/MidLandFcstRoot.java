package com.lifedawn.bestweather.retrofit.responses.kma.midlandfcstresponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.kma.kmacommons.KmaRoot;

public class MidLandFcstRoot extends KmaRoot {
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
