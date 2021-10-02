package com.lifedawn.bestweather.retrofit.responses.kma.midtaresponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.kma.VilageFcstHeader;

public class MidTaResponse   {
	@Expose
	@SerializedName("header")
	private VilageFcstHeader vilageFcstHeader;

	@Expose
	@SerializedName("body")
	private MidTaBody body;

	
	public VilageFcstHeader getHeader() {
		return vilageFcstHeader;
	}

	public void setHeader(VilageFcstHeader vilageFcstHeader) {
		this.vilageFcstHeader = vilageFcstHeader;
	}

	public MidTaBody getBody() {
		return body;
	}

	public void setBody(MidTaBody body) {
		this.body = body;
	}
}
