package com.lifedawn.bestweather.retrofit.responses.kma.vilagefcstcommons;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VilageFcstBody {
	@Expose
	@SerializedName("items")
	private VilageFcstItems items;
	
	public void setItems(VilageFcstItems items) {
		this.items = items;
	}
	
	public VilageFcstItems getItems() {
		return items;
	}
}
