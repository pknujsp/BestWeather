package com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.midtaresponse

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MidTaRoot : MidTaResponse() {
    @Expose @SerializedName("response") var response: MidTaResponse? = null
}