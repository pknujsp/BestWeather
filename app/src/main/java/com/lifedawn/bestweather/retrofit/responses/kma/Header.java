package com.lifedawn.bestweather.retrofit.responses.kma;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Header {
	@Expose
	@SerializedName("resultCode")
	private String resultCode;
	
	@Expose
	@SerializedName("resultMsg")
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