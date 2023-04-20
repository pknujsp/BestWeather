package com.lifedawn.bestweather.commons.classes.location

import android.location.Location
import com.google.android.gms.location.LocationResult

sealed class LocationResponse {
    class Success(locationResult: LocationResult) : LocationResponse() {
        val location: Location

        init {
            var bestIndex = 0
            var accuracy = Float.MIN_VALUE
            val locations = locationResult.locations

            for (i in locations.indices) {
                if (locations[i].accuracy > accuracy) {
                    accuracy = locations[i].accuracy
                    bestIndex = i
                }
            }
            location = locations[bestIndex]
        }
    }

    class Failure(val fail: FusedLocation.Fail) : LocationResponse()

    object Loading : LocationResponse()
}