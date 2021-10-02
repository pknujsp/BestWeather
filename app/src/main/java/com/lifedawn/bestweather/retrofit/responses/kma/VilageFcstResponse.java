package com.lifedawn.bestweather.retrofit.responses.kma;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VilageFcstResponse {
	@Expose
	@SerializedName("header")
	private VilageFcstHeader vilageFcstHeader;

	@Expose
	@SerializedName("body")
	private VilageFcstBody body;


	public VilageFcstHeader getHeader() {
		return vilageFcstHeader;
	}

	public void setHeader(VilageFcstHeader vilageFcstHeader) {
		this.vilageFcstHeader = vilageFcstHeader;
	}

	public VilageFcstBody getBody() {
		return body;
	}

	public void setBody(VilageFcstBody body) {
		this.body = body;
	}
}
