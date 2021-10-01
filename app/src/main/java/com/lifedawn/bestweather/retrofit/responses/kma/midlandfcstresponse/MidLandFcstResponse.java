package com.lifedawn.bestweather.retrofit.responses.kma.midlandfcstresponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.kma.Header;

public class MidLandFcstResponse {
	@Expose
	@SerializedName("header")
	private Header header;
	
	@Expose
	@SerializedName("body")
	private MidLandFcstBody body;
	
	
	public Header getHeader() {
		return header;
	}
	
	public void setHeader(Header header) {
		this.header = header;
	}
	
	public MidLandFcstBody getBody() {
		return body;
	}
	
	public void setBody(MidLandFcstBody body) {
		this.body = body;
	}
}
