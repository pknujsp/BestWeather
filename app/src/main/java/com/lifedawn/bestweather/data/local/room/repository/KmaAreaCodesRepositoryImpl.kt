package com.lifedawn.bestweather.data.local.room.repository

import com.lifedawn.bestweather.data.local.room.AppDb
import com.lifedawn.bestweather.data.local.room.dao.KmaAreaCodesDao
import com.lifedawn.bestweather.data.local.room.dto.KmaAreaCodeDto
import com.lifedawn.bestweather.data.local.room.queryinterfaces.KmaAreaCodesRepository
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.LocationDistance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class KmaAreaCodesRepositoryImpl @Inject constructor(
    private val room: AppDb
) : KmaAreaCodesRepository {
    override fun getAreaCode(latitude: Double, longitude: Double): Flow<KmaAreaCodeDto> = flow {
        val list = room.kmaAreaCodesDao().getAreaCodes(latitude, longitude)
        val criteriaLatLng = doubleArrayOf(latitude, longitude)
        var minDistance = Double.MAX_VALUE
        var distance = 0.0
        val compLatLng = DoubleArray(2)
        lateinit var nearbyKmaAreaCodeDto: KmaAreaCodeDto

        for (weatherAreaCodeDTO in list) {
            compLatLng[0] = weatherAreaCodeDTO.latitudeSecondsDivide100.toDouble()
            compLatLng[1] = weatherAreaCodeDTO.longitudeSecondsDivide100.toDouble()
            distance = LocationDistance.distance(
                criteriaLatLng[0], criteriaLatLng[1], compLatLng[0], compLatLng[1],
                LocationDistance.Unit.METER
            )
            if (distance < minDistance) {
                minDistance = distance
                nearbyKmaAreaCodeDto = weatherAreaCodeDTO
            }
        }

        emit(nearbyKmaAreaCodeDto)
    }

}