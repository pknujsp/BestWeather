package com.lifedawn.bestweather.retrofit.responses.kma.midtaresponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MidTaBody
{
    @Expose
    @SerializedName("items")
    private MidTaItemsKma items;


    public void setItems(MidTaItemsKma items)
    {
        this.items = items;
    }

    public MidTaItemsKma getItems()
    {
        return items;
    }
}
