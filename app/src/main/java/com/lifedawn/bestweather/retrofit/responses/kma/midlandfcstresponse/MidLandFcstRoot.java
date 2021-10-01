package com.lifedawn.bestweather.retrofit.responses.kma.midlandfcstresponse;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MidLandFcstRoot {
	@Expose
	@SerializedName("response")
	private MidLandFcstResponse response;
	
	
	public void setResponse(MidLandFcstResponse response) {
		this.response = response;
	}
	
	public MidLandFcstResponse getResponse() {
		return response;
	}
}
