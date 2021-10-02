package com.lifedawn.bestweather.retrofit.responses.kma.midlandfcstresponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MidLandFcstBody
{
    @Expose
    @SerializedName("items")
    private MidLandFcstItemsKma items;
    
    public void setItems(MidLandFcstItemsKma items)
    {
        this.items = items;
    }

    public MidLandFcstItemsKma getItems()
    {
        return items;
    }
}
