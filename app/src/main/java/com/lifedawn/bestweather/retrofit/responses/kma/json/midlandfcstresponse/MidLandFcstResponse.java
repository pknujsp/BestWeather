package com.lifedawn.bestweather.retrofit.responses.kma.json.midlandfcstresponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.kma.json.kmacommons.KmaHeader;
import com.tickaroo.tikxml.annotation.Element;
import com.tickaroo.tikxml.annotation.PropertyElement;
import com.tickaroo.tikxml.annotation.Xml;


@Xml(name = "response", inheritance = true)
public class MidLandFcstResponse {
	@Expose
	@SerializedName("header")
	@Element(name = "header")
	private KmaHeader kmaHeader;

	@Expose
	@SerializedName("body")
	@Element(name = "body")
	private MidLandFcstBody body;


	public KmaHeader getKmaHeader() {
		return kmaHeader;
	}

	public void setKmaHeader(KmaHeader kmaHeader) {
		this.kmaHeader = kmaHeader;
	}

	public MidLandFcstBody getBody() {
		return body;
	}

	public void setBody(MidLandFcstBody body) {
		this.body = body;
	}
}
