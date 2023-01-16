package com.lifedawn.bestweather.data.remote.retrofit.responses.nominatim

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GeocodeResponse {
    @Expose @SerializedName("features") var features: List<Features>? = null
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
        @Expose @SerializedName("icon") var icon: String? = null
        @Expose @SerializedName("type") var type: String? = null
        @Expose @SerializedName("category") var category: String? = null
        @Expose @SerializedName("place_rank") var placeRank = 0
        @Expose @SerializedName("display_name") var displayName: String? = null
        @Expose @SerializedName("osm_id") var osmId: String? = null
        @Expose @SerializedName("osm_type") var osmType: String? = null
        @Expose @SerializedName("place_id") var placeId: String? = null
    }

    class Address {
        @Expose @SerializedName("country_code") var countryCode: String? = null
        @Expose @SerializedName("country") var country: String? = null
        @Expose @SerializedName("state") var state: String? = null
        @Expose @SerializedName("town") var town: String? = null
        @Expose @SerializedName("railway") var railway: String? = null
    }
}