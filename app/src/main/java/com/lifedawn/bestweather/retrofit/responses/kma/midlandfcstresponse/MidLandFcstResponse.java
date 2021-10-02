package com.lifedawn.bestweather.retrofit.responses.kma.midlandfcstresponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.kma.kmacommons.KmaHeader;

public class MidLandFcstResponse {
	@Expose
	@SerializedName("header")
	private KmaHeader kmaHeader;
	
	@Expose
	@SerializedName("body")
	private MidLandFcstBody body;
	
	
	public KmaHeader getHeader() {
		return kmaHeader;
	}
	
	public void setHeader(KmaHeader kmaHeader) {
		this.kmaHeader = kmaHeader;
	}
	
	public MidLandFcstBody getBody() {
		return body;
	}
	
	public void setBody(MidLandFcstBody body) {
		this.body = body;
	}
}
