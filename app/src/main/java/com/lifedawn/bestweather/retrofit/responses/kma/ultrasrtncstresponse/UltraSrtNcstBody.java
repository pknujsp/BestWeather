package com.lifedawn.bestweather.retrofit.responses.kma.ultrasrtncstresponse;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UltraSrtNcstBody {
	@Expose
	@SerializedName("items")
	private UltraSrtNcstItemsKma items;
	
	
	public void setItems(UltraSrtNcstItemsKma items) {
		this.items = items;
	}
	
	public UltraSrtNcstItemsKma getItems() {
		return items;
	}
}
