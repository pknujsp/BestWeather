package com.lifedawn.bestweather.retrofit.responses.kma.ultrasrtfcstresponse;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UltraSrtFcstBody {
	@Expose
	@SerializedName("items")
	private UltraSrtFcstItems items;


	public void setItems(UltraSrtFcstItems items) {
		this.items = items;
	}

	public UltraSrtFcstItems getItems() {
		return items;
	}
}
