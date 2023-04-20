package com.lifedawn.bestweather.data.remote.weather.kma.usecase

import com.lifedawn.bestweather.data.local.room.queryinterfaces.KmaAreaCodesRepository
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetAreaCodeUseCaseImpl @Inject constructor(private val kmaAreaCodesRepository: KmaAreaCodesRepository):GetAreaCodeUseCase {

    override fun getAreaCode(latitude: Double, longitude: Double) = flow {
        kmaAreaCodesRepository.getAreaCode(latitude, longitude).collect() {
            emit(it.administrativeAreaCode)
        }
    }
}