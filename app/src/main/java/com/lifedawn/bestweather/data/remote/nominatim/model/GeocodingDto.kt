package com.lifedawn.bestweather.data.remote.nominatim.model

data class GeocodingDto(
    val latitude: Double, val longitude: Double,
    val displayName: String, val country: String, val countryCode: String
)
