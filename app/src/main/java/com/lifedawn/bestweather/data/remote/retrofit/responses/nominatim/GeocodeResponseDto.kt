package com.lifedawn.bestweather.data.remote.retrofit.responses.nominatim

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class GeocodeResponseDto(
    @Expose @SerializedName("features") val features: List<Features> = emptyList(),
    @Expose @SerializedName("type") var type: String = ""
) {
    data class Features(
        @Expose @SerializedName("geometry") var geometry: Geometry? = null,
        @Expose @SerializedName("bbox") val bbox: List<Double> = emptyList(),
        @Expose @SerializedName("properties") var properties: Properties? = null,
        @Expose @SerializedName("type") var type: String = "",
    )

    data class Geometry(
        @Expose @SerializedName("coordinates") val coordinates: List<Double> = emptyList(),
        @Expose @SerializedName("type") var type: String = "",
    )

    data class Properties(
        @Expose @SerializedName("address") var address: Address? = null,
        @Expose @SerializedName("icon") var icon: String = "",
        @Expose @SerializedName("type") var type: String = "",
        @Expose @SerializedName("category") var category: String = "",
        @Expose @SerializedName("place_rank") var placeRank: String = "",
        @Expose @SerializedName("display_name") var displayName: String = "",
        @Expose @SerializedName("osm_id") var osmId: String = "",
        @Expose @SerializedName("osm_type") var osmType: String = "",
        @Expose @SerializedName("place_id") var placeId: String = ""
    )

    data class Address(
        @Expose @SerializedName("country_code") var countryCode: String = "",
        @Expose @SerializedName("country") var country: String = "",
        @Expose @SerializedName("state") var state: String = "",
        @Expose @SerializedName("town") var town: String = "",
        @Expose @SerializedName("railway") var railway: String = ""
    )
}