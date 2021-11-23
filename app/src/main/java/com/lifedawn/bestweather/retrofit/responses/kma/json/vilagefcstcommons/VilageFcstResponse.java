package com.lifedawn.bestweather.retrofit.responses.kma.json.vilagefcstcommons;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.kma.json.kmacommons.KmaHeader;
import com.tickaroo.tikxml.annotation.Element;
import com.tickaroo.tikxml.annotation.PropertyElement;
import com.tickaroo.tikxml.annotation.Xml;

@Xml(name = "response", inheritance = true)
public class VilageFcstResponse {
	@Expose
	@SerializedName("header")
	@Element(name = "header")
	private KmaHeader kmaHeader;

	@Expose
	@SerializedName("body")
	@Element(name = "body")
	private VilageFcstBody body;


	public KmaHeader getKmaHeader() {
		return kmaHeader;
	}

	public VilageFcstResponse setKmaHeader(KmaHeader kmaHeader) {
		this.kmaHeader = kmaHeader;
		return this;
	}

	public VilageFcstBody getBody() {
		return body;
	}

	public void setBody(VilageFcstBody body) {
		this.body = body;
	}
}
