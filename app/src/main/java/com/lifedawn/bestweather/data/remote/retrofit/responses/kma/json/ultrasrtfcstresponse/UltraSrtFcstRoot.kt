package com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.ultrasrtfcstresponse

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.vilagefcstcommons.VilageFcstResponse

class UltraSrtFcstRoot {
    @Expose @SerializedName("response") var response: VilageFcstResponse? = null
}