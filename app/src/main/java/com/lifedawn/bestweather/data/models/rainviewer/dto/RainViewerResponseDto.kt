package com.lifedawn.bestweather.data.models.rainviewer.dto


import com.google.gson.annotations.SerializedName

data class RainViewerResponseDto(
    @SerializedName("generated")
        val generated: Int,
    @SerializedName("host")
        val host: String,
    @SerializedName("radar")
        val radar: Radar,
    @SerializedName("satellite")
        val satellite: Satellite,
    @SerializedName("version")
        val version: String
) {
    data class Radar(
        @SerializedName("nowcast")
            val nowcast: ArrayList<Data>,
        @SerializedName("past")
            val past: ArrayList<Data>
    )

    data class Satellite(
            @SerializedName("infrared")
            val infrared: ArrayList<Data>
    )

    data class Data(
            @SerializedName("path")
            val path: String,
            @SerializedName("time")
            val time: Int
    )
}