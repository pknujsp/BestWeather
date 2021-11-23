package com.lifedawn.bestweather.retrofit.responses.kma.json.midtaresponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.kma.json.kmacommons.KmaHeader;
import com.tickaroo.tikxml.annotation.Element;
import com.tickaroo.tikxml.annotation.PropertyElement;
import com.tickaroo.tikxml.annotation.Xml;

@Xml(name = "response", inheritance = true)
public class MidTaResponse   {
	@Expose
	@SerializedName("header")
	@Element(name = "header")
	private KmaHeader kmaHeader;

	@Expose
	@SerializedName("body")
	@Element(name = "body")
	private MidTaBody body;


	public KmaHeader getKmaHeader() {
		return kmaHeader;
	}

	public MidTaResponse setKmaHeader(KmaHeader kmaHeader) {
		this.kmaHeader = kmaHeader;
		return this;
	}

	public MidTaBody getBody() {
		return body;
	}

	public void setBody(MidTaBody body) {
		this.body = body;
	}
}
