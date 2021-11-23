package com.lifedawn.bestweather.retrofit.responses.kma.json.midlandfcstresponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tickaroo.tikxml.annotation.Element;
import com.tickaroo.tikxml.annotation.PropertyElement;
import com.tickaroo.tikxml.annotation.Xml;


@Xml(name = "body", inheritance = true)
public class MidLandFcstBody {
	@Expose
	@SerializedName("items")
	@Element(name = "items")
	private MidLandFcstItems items;

	public void setItems(MidLandFcstItems items) {
		this.items = items;
	}

	public MidLandFcstItems getItems() {
		return items;
	}
}
