package com.lifedawn.bestweather.retrofit.responses.kma.json.midtaresponse;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tickaroo.tikxml.annotation.Element;
import com.tickaroo.tikxml.annotation.PropertyElement;
import com.tickaroo.tikxml.annotation.Xml;

import java.util.List;


@Xml(name = "items", inheritance = true)
public class MidTaItems {
	@Expose
	@SerializedName("item")
	@Element(name = "item")
	private List<MidTaItem> item;


	public void setItem(List<MidTaItem> item) {
		this.item = item;
	}

	public List<MidTaItem> getItem() {
		return item;
	}
}
