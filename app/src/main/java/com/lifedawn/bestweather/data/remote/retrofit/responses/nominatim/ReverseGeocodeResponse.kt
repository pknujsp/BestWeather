package com.lifedawn.bestweather.data.remote.retrofit.responses.nominatim

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ReverseGeocodeResponse {
    @Expose @SerializedName("features") var features: List<Features>? = null
    @Expose @SerializedName("licence") var licence: String? = null
    @Expose @SerializedName("type") var type: String? = null

    class Features {
        @Expose @SerializedName("geometry") var geometry: Geometry? = null
        @Expose @SerializedName("bbox") var bbox: List<Double>? = null
        @Expose @SerializedName("properties") var properties: Properties? = null
        @Expose @SerializedName("type") var type: String? = null
    }

    class Geometry {
        @Expose @SerializedName("coordinates") var coordinates: List<Double>? = null
        @Expose @SerializedName("type") var type: String? = null
    }

    class Properties {
        @Expose @SerializedName("address") var address: Address? = null
        @Expose @SerializedName("display_name") var displayName: String? = null
        @Expose @SerializedName("name") var name: String? = null
        @Expose @SerializedName("addresstype") var addresstype: String? = null
        @Expose @SerializedName("importance") var importance = 0.0
        @Expose @SerializedName("type") var type: String? = null
        @Expose @SerializedName("category") var category: String? = null
        @Expose @SerializedName("place_rank") var placeRank = 0
        @Expose @SerializedName("osm_id") var osmId: String? = null
        @Expose @SerializedName("osm_type") var osmType: String? = null
        @Expose @SerializedName("place_id") var placeId = 0
    }

    class Address {
        @Expose @SerializedName("country_code") var countryCode: String? = null
        @Expose @SerializedName("country") var country: String? = null
        @Expose @SerializedName("postcode") var postcode: String? = null
        @Expose @SerializedName("city") var city: String? = null
        @Expose @SerializedName("state") var state: String? = null
        @Expose @SerializedName("borough") var borough: String? = null
        @Expose @SerializedName("village") var village: String? = null
        @Expose @SerializedName("neighbourhood") var neighbourhood: String? = null
        @Expose @SerializedName("road") var road: String? = null
        @Expose @SerializedName("house_number") var houseNumber: String? = null
    }
}