package com.lifedawn.bestweather.data.remote.weather.kma.usecase

import com.lifedawn.bestweather.data.local.room.queryinterfaces.KmaAreaCodesRepository
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetAreaCodeUseCase @Inject constructor(private val kmaAreaCodesRepository: KmaAreaCodesRepository) {
    fun getAreaCode(latitude: Double, longitude: Double) = flow {
        kmaAreaCodesRepository.getAreaCode(latitude, longitude).collect() {
            emit(it.administrativeAreaCode)
        }
    }
}