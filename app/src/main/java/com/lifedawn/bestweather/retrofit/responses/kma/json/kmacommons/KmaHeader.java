package com.lifedawn.bestweather.retrofit.responses.kma.json.kmacommons;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tickaroo.tikxml.annotation.PropertyElement;
import com.tickaroo.tikxml.annotation.Xml;

@Xml(name = "header", inheritance = true)
public class KmaHeader {
	@Expose
	@SerializedName("resultCode")
	@PropertyElement(name = "resultCode")
	private String resultCode;

	@Expose
	@SerializedName("resultMsg")
	@PropertyElement(name = "resultMsg")
	private String resultMsg;

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getResultMsg() {
		return resultMsg;
	}

	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}
}