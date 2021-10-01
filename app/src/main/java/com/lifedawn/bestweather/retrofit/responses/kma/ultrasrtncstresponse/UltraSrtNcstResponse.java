package com.lifedawn.bestweather.retrofit.responses.kma.ultrasrtncstresponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.kma.Header;

public class UltraSrtNcstResponse{
	@Expose
	@SerializedName("header")
	private Header header;

	@Expose
	@SerializedName("body")
	private UltraSrtNcstBody body;



	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public UltraSrtNcstBody getBody() {
		return body;
	}

	public void setBody(UltraSrtNcstBody body) {
		this.body = body;
	}
}
