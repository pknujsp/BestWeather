package com.lifedawn.bestweather.retrofit.responses.kma.midtaresponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MidTaRoot
{
    @Expose
    @SerializedName("response")
    private MidTaResponse response;
    
    public void setResponse(MidTaResponse response)
    {
        this.response = response;
    }

    public MidTaResponse getResponse()
    {
        return response;
    }
}
