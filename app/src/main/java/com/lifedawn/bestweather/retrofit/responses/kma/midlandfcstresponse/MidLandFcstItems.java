package com.lifedawn.bestweather.retrofit.responses.kma.midlandfcstresponse;


import android.os.Parcel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.kma.WeatherItems;

import java.util.List;

public class MidLandFcstItems extends WeatherItems
{
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
