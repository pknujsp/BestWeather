package com.lifedawn.bestweather.retrofit.responses.kma.ultrasrtncstresponse;


import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UltraSrtNcstRoot
{
    @Expose
    @SerializedName("response")
    private UltraSrtNcstResponse response;


    public void setResponse(UltraSrtNcstResponse response)
    {
        this.response = response;
    }

    public UltraSrtNcstResponse getResponse()
    {
        return response;
    }
}
