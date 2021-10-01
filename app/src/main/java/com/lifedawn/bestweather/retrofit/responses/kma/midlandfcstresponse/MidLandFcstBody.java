package com.lifedawn.bestweather.retrofit.responses.kma.midlandfcstresponse;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MidLandFcstBody
{
    @Expose
    @SerializedName("items")
    private MidLandFcstItems items;
    
    public void setItems(MidLandFcstItems items)
    {
        this.items = items;
    }

    public MidLandFcstItems getItems()
    {
        return items;
    }
}
