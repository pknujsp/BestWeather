package com.lifedawn.bestweather.retrofit.responses.kma.json.midtaresponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tickaroo.tikxml.annotation.Element;
import com.tickaroo.tikxml.annotation.PropertyElement;
import com.tickaroo.tikxml.annotation.Xml;


@Xml(name = "body", inheritance = true)
public class MidTaBody {
	@Expose
	@SerializedName("items")
	@Element(name = "items")
	private MidTaItems items;


	public void setItems(MidTaItems items) {
		this.items = items;
	}

	public MidTaItems getItems() {
		return items;
	}
}
