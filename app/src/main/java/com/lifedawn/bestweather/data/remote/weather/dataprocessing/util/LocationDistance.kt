package com.lifedawn.bestweather.data.remote.weather.dataprocessing.util

object LocationDistance {
    /**
     * 두 지점간의 거리 계산
     *
     * @param lat1 지점 1 위도
     * @param lon1 지점 1 경도
     * @param lat2 지점 2 위도
     * @param lon2 지점 2 경도
     * @param unit 거리 표출단위
     * @return
     */
    @JvmStatic
    fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double, unit: Unit): Double {
        val theta = lon1 - lon2
        var dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(
            deg2rad(theta)
        )
        dist = Math.acos(dist)
        dist = rad2deg(dist)
        dist = dist * 60 * 1.1515
        if (unit == Unit.KM) {
            dist = dist * 1.609344
        } else if (unit == Unit.METER) {
            dist = dist * 1609.344
        }
        return dist
    }

    // This function converts decimal degrees to radians
    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    // This function converts radians to decimal degrees
    private fun rad2deg(rad: Double): Double {
        return rad * 180 / Math.PI
    }

    enum class Unit {
        METER, KM
    }
}