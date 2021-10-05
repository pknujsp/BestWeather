package com.lifedawn.bestweather.room.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.lifedawn.bestweather.room.dto.KmaAreaCodeDto;

import java.util.List;

@Dao
public interface KmaAreaCodesDao {
	@Query("SELECT * FROM weather_area_code_table WHERE latitude_seconds_divide_100 >= :latitude-0.18 AND latitude_seconds_divide_100 <= "
			+ ":latitude+0.18 AND longitude_seconds_divide_100 >= :longitude-0.18 AND longitude_seconds_divide_100 <= :longitude+0.18")
	List<KmaAreaCodeDto> getAreaCodes(double latitude, double longitude);
}