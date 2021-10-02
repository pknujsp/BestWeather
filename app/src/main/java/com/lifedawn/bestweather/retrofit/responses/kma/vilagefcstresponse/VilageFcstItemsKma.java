package com.lifedawn.bestweather.retrofit.responses.kma.vilagefcstresponse;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.kma.KmaWeatherItems;

import java.util.List;

public class VilageFcstItemsKma extends KmaWeatherItems {
	@Expose
	@SerializedName("item")
	private List<VilageFcstItem> item;

	public void setItem(List<VilageFcstItem> item) {
		this.item = item;
	}

	public List<VilageFcstItem> getItem() {
		return item;
	}
}
