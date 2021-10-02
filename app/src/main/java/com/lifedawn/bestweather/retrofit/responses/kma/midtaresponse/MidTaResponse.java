package com.lifedawn.bestweather.retrofit.responses.kma.midtaresponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.kma.kmacommons.KmaHeader;

public class MidTaResponse   {
	@Expose
	@SerializedName("header")
	private KmaHeader kmaHeader;

	@Expose
	@SerializedName("body")
	private MidTaBody body;

	
	public KmaHeader getHeader() {
		return kmaHeader;
	}

	public void setHeader(KmaHeader kmaHeader) {
		this.kmaHeader = kmaHeader;
	}

	public MidTaBody getBody() {
		return body;
	}

	public void setBody(MidTaBody body) {
		this.body = body;
	}
}
