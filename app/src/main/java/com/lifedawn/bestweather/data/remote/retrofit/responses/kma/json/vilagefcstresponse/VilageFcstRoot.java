package com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.vilagefcstresponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.vilagefcstcommons.VilageFcstResponse;

public class VilageFcstRoot {
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
