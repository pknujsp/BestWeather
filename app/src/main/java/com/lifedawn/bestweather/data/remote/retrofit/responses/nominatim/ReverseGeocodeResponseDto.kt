package com.lifedawn.bestweather.data.remote.retrofit.responses.nominatim

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ReverseGeocodeResponseDto(
    @Expose @SerializedName("features") var features: List<GeocodeResponseDto.Features> = emptyList(),
    @Expose @SerializedName("licence") var licence: String = "",
    @Expose @SerializedName("type") var type: String = ""
) {
    data class Features(
        @Expose @SerializedName("geometry") var geometry: Geometry? = null,
        @Expose @SerializedName("bbox") var bbox: List<Double> = emptyList(),
        @Expose @SerializedName("properties") var properties: Properties? = null,
        @Expose @SerializedName("type") var type: String = ""
    )

    data class Geometry(
        @Expose @SerializedName("coordinates") var coordinates: List<Double> = emptyList(),
        @Expose @SerializedName("type") var type: String = ""
    )

    data class Properties(
        @Expose @SerializedName("address") var address: Address? = null,
        @Expose @SerializedName("display_name") var displayName: String = "",
        @Expose @SerializedName("name") var name: String = "",
        @Expose @SerializedName("addresstype") var addresstype: String = "",
        @Expose @SerializedName("importance") var importance: String = "0",
        @Expose @SerializedName("type") var type: String = "",
        @Expose @SerializedName("category") var category: String = "",
        @Expose @SerializedName("place_rank") var placeRank: String = "",
        @Expose @SerializedName("osm_id") var osmId: String = "",
        @Expose @SerializedName("osm_type") var osmType: String = "",
        @Expose @SerializedName("place_id") var placeId: String = ""
    )

    data class Address(
        @Expose @SerializedName("country_code") var countryCode: String = "",
        @Expose @SerializedName("country") var country: String = "",
        @Expose @SerializedName("postcode") var postcode: String = "",
        @Expose @SerializedName("city") var city: String = "",
        @Expose @SerializedName("state") var state: String = "",
        @Expose @SerializedName("borough") var borough: String = "",
        @Expose @SerializedName("village") var village: String = "",
        @Expose @SerializedName("neighbourhood") var neighbourhood: String = "",
        @Expose @SerializedName("road") var road: String = "",
        @Expose @SerializedName("house_number") var houseNumber: String = ""
    )
}