package com.lifedawn.bestweather.retrofit.responses.kma.vilagefcstresponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VilageFcstBody {
	@Expose
	@SerializedName("items")
	private VilageFcstItemsKma items;


	public void setItems(VilageFcstItemsKma items) {
		this.items = items;
	}

	public VilageFcstItemsKma getItems() {
		return items;
	}
}
