package com.lifedawn.bestweather.retrofit.responses.kma.midtaresponse;

import android.os.Parcel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MidTaBody
{
    @Expose
    @SerializedName("items")
    private MidTaItems items;


    public void setItems(MidTaItems items)
    {
        this.items = items;
    }

    public MidTaItems getItems()
    {
        return items;
    }
}
