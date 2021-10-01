package com.lifedawn.bestweather.retrofit.responses.kma.ultrasrtncstresponse;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UltraSrtNcstBody {
	@Expose
	@SerializedName("items")
	private UltraSrtNcstItems items;
	
	
	public void setItems(UltraSrtNcstItems items) {
		this.items = items;
	}
	
	public UltraSrtNcstItems getItems() {
		return items;
	}
}
