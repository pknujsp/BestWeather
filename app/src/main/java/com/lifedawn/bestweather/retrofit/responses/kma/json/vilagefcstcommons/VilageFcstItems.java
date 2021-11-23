package com.lifedawn.bestweather.retrofit.responses.kma.json.vilagefcstcommons;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tickaroo.tikxml.annotation.Element;
import com.tickaroo.tikxml.annotation.PropertyElement;
import com.tickaroo.tikxml.annotation.Xml;

import java.util.List;


@Xml(name = "items", inheritance = true)
public class VilageFcstItems {
	@Expose
	@SerializedName("item")
	@Element(name = "item")
	private List<VilageFcstItem> item;
	
	public void setItem(List<VilageFcstItem> item) {
		this.item = item;
	}
	
	public List<VilageFcstItem> getItem() {
		return item;
	}
}
