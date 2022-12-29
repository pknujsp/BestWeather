package com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.vilagefcstcommons;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tickaroo.tikxml.annotation.Element;
import com.tickaroo.tikxml.annotation.Xml;


@Xml(name = "body", inheritance = true)
public class VilageFcstBody {
	@Expose
	@SerializedName("items")
	@Element(name = "items")
	private VilageFcstItems items;

	public void setItems(VilageFcstItems items) {
		this.items = items;
	}

	public VilageFcstItems getItems() {
		return items;
	}
}
