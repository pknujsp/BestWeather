package com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.midlandfcstresponse;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tickaroo.tikxml.annotation.Element;
import com.tickaroo.tikxml.annotation.Xml;

import java.util.List;

@Xml(name = "items", inheritance = true)
public class MidLandFcstItems {
    @Expose
    @SerializedName("item")
    @Element(name = "item")
    private List<MidLandFcstItem> item;
    
    public void setItem(List<MidLandFcstItem> item) {
        this.item = item;
    }
    
    public List<MidLandFcstItem> getItem() {
        return item;
    }
}
