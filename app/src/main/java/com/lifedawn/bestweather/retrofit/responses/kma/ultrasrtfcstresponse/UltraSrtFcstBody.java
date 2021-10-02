package com.lifedawn.bestweather.retrofit.responses.kma.ultrasrtfcstresponse;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UltraSrtFcstBody {
	@Expose
	@SerializedName("items")
	private UltraSrtFcstItemsKma items;


	public void setItems(UltraSrtFcstItemsKma items) {
		this.items = items;
	}

	public UltraSrtFcstItemsKma getItems() {
		return items;
	}
}
