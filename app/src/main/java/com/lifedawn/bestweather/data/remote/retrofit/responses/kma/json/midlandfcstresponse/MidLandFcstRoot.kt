package com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.midlandfcstresponse

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MidLandFcstRoot {
    @Expose @SerializedName("response") var response: MidLandFcstResponse? = null
}