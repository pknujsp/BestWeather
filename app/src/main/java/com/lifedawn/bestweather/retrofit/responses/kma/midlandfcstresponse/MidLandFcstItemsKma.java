package com.lifedawn.bestweather.retrofit.responses.kma.midlandfcstresponse;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MidLandFcstItemsKma {
    @Expose
    @SerializedName("item")
    private List<MidLandFcstItem> item;
    
    public void setItem(List<MidLandFcstItem> item) {
        this.item = item;
    }
    
    public List<MidLandFcstItem> getItem() {
        return item;
    }
}
