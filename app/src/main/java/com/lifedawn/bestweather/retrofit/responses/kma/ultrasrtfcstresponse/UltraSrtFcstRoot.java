package com.lifedawn.bestweather.retrofit.responses.kma.ultrasrtfcstresponse;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Keep
public class UltraSrtFcstRoot {
	@Expose
	@SerializedName("response")
	private UltraSrtFcstResponse response;


	public void setResponse(UltraSrtFcstResponse response) {
		this.response = response;
	}

	public UltraSrtFcstResponse getResponse() {
		return response;
	}
}
