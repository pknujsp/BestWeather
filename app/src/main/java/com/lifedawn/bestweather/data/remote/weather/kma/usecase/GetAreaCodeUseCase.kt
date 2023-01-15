package com.lifedawn.bestweather.data.remote.weather.kma.usecase

import kotlinx.coroutines.flow.Flow

interface GetAreaCodeUseCase {

    fun getAreaCode(latitude: Double, longitude: Double): Flow<String>
}