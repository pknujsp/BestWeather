package com.lifedawn.bestweather.room.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.lifedawn.bestweather.room.dto.KmaAreaCodeDto;

import java.util.List;

@Dao
public interface KmaAreaCodesDao {
	@Query("SELECT * FROM kma_area_codes_table WHERE latitude_seconds_divide_100 >= :latitude-0.19 AND latitude_seconds_divide_100 <= " +
			":latitude+0.19" +
			" AND longitude_seconds_divide_100 >= :longitude-0.19 AND longitude_seconds_divide_100 <= :longitude+0.19")
	List<KmaAreaCodeDto> getAreaCodes(double latitude, double longitude);
}