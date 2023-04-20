package com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual

import android.graphics.drawable.Drawable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Weather {
    @Expose @SerializedName("id") var id: String? = null
    @Expose @SerializedName("main") var main: String? = null
    @Expose @SerializedName("description") var description: String? = null
    @Expose @SerializedName("icon") var icon: String? = null
    var weatherImg: Drawable? = null
}