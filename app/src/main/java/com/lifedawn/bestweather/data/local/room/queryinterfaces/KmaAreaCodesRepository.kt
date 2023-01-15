package com.lifedawn.bestweather.data.local.room.queryinterfaces

import com.lifedawn.bestweather.data.local.room.dto.KmaAreaCodeDto
import kotlinx.coroutines.flow.Flow

interface KmaAreaCodesRepository {
    fun getAreaCode(latitude: Double, longitude: Double): Flow<KmaAreaCodeDto>
}