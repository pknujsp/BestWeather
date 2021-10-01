package com.lifedawn.bestweather.retrofit.responses.kma.midtaresponse;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.kma.WeatherItems;

import java.util.List;

public class MidTaItems extends WeatherItems
{
    @Expose
    @SerializedName("item")
    private List<MidTaItem> item;
    

    public void setItem(List<MidTaItem> item)
    {
        this.item = item;
    }

    public List<MidTaItem> getItem()
    {
        return item;
    }
}
