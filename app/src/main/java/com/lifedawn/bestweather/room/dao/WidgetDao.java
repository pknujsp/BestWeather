package com.lifedawn.bestweather.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lifedawn.bestweather.room.dto.WidgetDto;

@Dao
public interface WidgetDao {
	@Insert
	long add(WidgetDto widgetDto);

	@Query("SELECT * FROM widget_table WHERE appWidgetId = :appWidgetId")
	WidgetDto get(int appWidgetId);

	@Query("SELECT * FROM widget_table WHERE id = :widgetDtoId")
	WidgetDto get(long widgetDtoId);

	@Update(onConflict = OnConflictStrategy.IGNORE, entity = WidgetDto.class)
	long update(WidgetDto widgetDto);
}
