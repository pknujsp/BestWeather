package com.lifedawn.bestweather.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lifedawn.bestweather.room.dto.AlarmDto;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;

import java.util.List;

@Dao
public interface AlarmDao {
	@Query("SELECT * FROM alarm_table")
	List<AlarmDto> getAll();

	@Query("SELECT count(*) FROM alarm_table")
	int size();

	@Query("SELECT * FROM alarm_table WHERE id = :id")
	AlarmDto get(int id);

	@Delete
	void delete(AlarmDto alarmDto);

	@Insert(entity = AlarmDto.class)
	long add(AlarmDto alarmDto);

	@Update(onConflict = OnConflictStrategy.IGNORE)
	int update(AlarmDto alarmDto);
}
