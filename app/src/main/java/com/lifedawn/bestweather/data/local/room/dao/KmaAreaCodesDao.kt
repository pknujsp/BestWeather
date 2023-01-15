package com.lifedawn.bestweather.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.lifedawn.bestweather.data.local.room.dto.KmaAreaCodeDto

@Dao
interface KmaAreaCodesDao {
    @Query(
        "SELECT * FROM weather_area_code_table WHERE latitude_seconds_divide_100 >= :latitude-0.15 AND latitude_seconds_divide_100 <= "
                + ":latitude+0.15 AND longitude_seconds_divide_100 >= :longitude-0.15 AND longitude_seconds_divide_100 <= :longitude+0.15"
    )
    fun getAreaCodes(latitude: Double, longitude: Double): List<KmaAreaCodeDto>
}