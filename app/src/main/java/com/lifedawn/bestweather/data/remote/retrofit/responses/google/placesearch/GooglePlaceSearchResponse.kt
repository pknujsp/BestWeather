package com.lifedawn.bestweather.data.remote.retrofit.responses.google.placesearch

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GooglePlaceSearchResponse {
    @SerializedName("status") @Expose var status: String? = null
    @SerializedName("results") @Expose var results: ArrayList<Item>? = null

    class Item {
        @SerializedName("name") @Expose var name: String? = null
        @SerializedName("formatted_address") @Expose var formatted_address: String? = null
        @SerializedName("geometry") @Expose var geometry: Geometry? = null

        class Geometry {
            @SerializedName("location") @Expose var location: LatLng? = null

            class LatLng {
                @SerializedName("lat") @Expose var lat: String? = null
                @SerializedName("lng") @Expose var lng: String? = null
            }
        }
    }
}