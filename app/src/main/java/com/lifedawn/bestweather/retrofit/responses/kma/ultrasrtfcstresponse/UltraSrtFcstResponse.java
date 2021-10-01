package com.lifedawn.bestweather.retrofit.responses.kma.ultrasrtfcstresponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.kma.Header;

public class UltraSrtFcstResponse {
	@Expose
	@SerializedName("header")
	private Header header;

	@Expose
	@SerializedName("body")
	private UltraSrtFcstBody body;


	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public UltraSrtFcstBody getBody() {
		return body;
	}

	public void setBody(UltraSrtFcstBody body) {
		this.body = body;
	}
}
