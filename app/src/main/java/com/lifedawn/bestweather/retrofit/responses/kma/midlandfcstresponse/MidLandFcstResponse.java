package com.lifedawn.bestweather.retrofit.responses.kma.midlandfcstresponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.kma.VilageFcstHeader;

public class MidLandFcstResponse {
	@Expose
	@SerializedName("header")
	private VilageFcstHeader vilageFcstHeader;
	
	@Expose
	@SerializedName("body")
	private MidLandFcstBody body;
	
	
	public VilageFcstHeader getHeader() {
		return vilageFcstHeader;
	}
	
	public void setHeader(VilageFcstHeader vilageFcstHeader) {
		this.vilageFcstHeader = vilageFcstHeader;
	}
	
	public MidLandFcstBody getBody() {
		return body;
	}
	
	public void setBody(MidLandFcstBody body) {
		this.body = body;
	}
}
