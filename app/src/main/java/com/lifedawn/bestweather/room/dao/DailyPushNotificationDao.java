package com.lifedawn.bestweather.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lifedawn.bestweather.room.dto.AlarmDto;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;

import java.util.List;

@Dao
public interface DailyPushNotificationDao {
	@Query("SELECT * FROM daily_push_notifications_table")
	List<DailyPushNotificationDto> getAll();

	@Query("SELECT * FROM daily_push_notifications_table")
	LiveData<List<DailyPushNotificationDto>> list();

	@Query("SELECT count(*) FROM daily_push_notifications_table")
	int size();

	@Query("SELECT * FROM daily_push_notifications_table WHERE id = :id")
	DailyPushNotificationDto get(int id);

	@Delete
	void delete(DailyPushNotificationDto dailyPushNotificationDto);

	@Insert(entity = DailyPushNotificationDto.class)
	long add(DailyPushNotificationDto dailyPushNotificationDto);

	@Update(onConflict = OnConflictStrategy.IGNORE)
	int update(DailyPushNotificationDto dailyPushNotificationDto);
}
