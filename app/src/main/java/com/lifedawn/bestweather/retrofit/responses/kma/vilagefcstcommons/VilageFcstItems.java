package com.lifedawn.bestweather.retrofit.responses.kma.vilagefcstcommons;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VilageFcstItems {
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
