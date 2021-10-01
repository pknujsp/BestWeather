package com.lifedawn.bestweather.retrofit.responses.kma.ultrasrtncstresponse;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.kma.WeatherItems;

import java.util.List;

public class UltraSrtNcstItems extends WeatherItems
{
    @Expose
    @SerializedName("item")
    private List<UltraSrtNcstItem> item;
    

    public void setItem(List<UltraSrtNcstItem> item)
    {
        this.item = item;
    }

    public List<UltraSrtNcstItem> getItem()
    {
        return item;
    }
}
