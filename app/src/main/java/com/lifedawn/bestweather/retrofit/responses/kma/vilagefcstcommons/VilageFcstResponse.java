package com.lifedawn.bestweather.retrofit.responses.kma.vilagefcstcommons;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.kma.kmacommons.KmaHeader;

public class VilageFcstResponse {
	@Expose
	@SerializedName("header")
	private KmaHeader kmaHeader;

	@Expose
	@SerializedName("body")
	private VilageFcstBody body;


	public KmaHeader getHeader() {
		return kmaHeader;
	}

	public void setHeader(KmaHeader kmaHeader) {
		this.kmaHeader = kmaHeader;
	}

	public VilageFcstBody getBody() {
		return body;
	}

	public void setBody(VilageFcstBody body) {
		this.body = body;
	}
}
