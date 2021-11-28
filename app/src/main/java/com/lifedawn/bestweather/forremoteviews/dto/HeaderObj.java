package com.lifedawn.bestweather.forremoteviews.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HeaderObj {
	@Expose
	@SerializedName("address")
	private String address;

	@Expose
	@SerializedName("refreshDateTime")
	private String refreshDateTime;

	public HeaderObj() {

	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getRefreshDateTime() {
		return refreshDateTime;
	}

	public void setRefreshDateTime(String refreshDateTime) {
		this.refreshDateTime = refreshDateTime;
	}
}
