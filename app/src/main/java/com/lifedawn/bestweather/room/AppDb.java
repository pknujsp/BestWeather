package com.lifedawn.bestweather.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.lifedawn.bestweather.model.timezone.TimeZoneIdDao;
import com.lifedawn.bestweather.model.timezone.TimeZoneIdDto;
import com.lifedawn.bestweather.room.dao.AlarmDao;
import com.lifedawn.bestweather.room.dao.DailyPushNotificationDao;
import com.lifedawn.bestweather.room.dao.FavoriteAddressDao;
import com.lifedawn.bestweather.room.dao.KmaAreaCodesDao;
import com.lifedawn.bestweather.room.dao.WidgetDao;
import com.lifedawn.bestweather.room.dto.AlarmDto;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.room.dto.KmaAreaCodeDto;
import com.lifedawn.bestweather.room.dto.WidgetDto;

@Database(entities = {KmaAreaCodeDto.class, FavoriteAddressDto.class, AlarmDto.class, WidgetDto.class, DailyPushNotificationDto.class,
		TimeZoneIdDto.class},
		version = 1, exportSchema =
		false)
@TypeConverters(RoomTypeConverter.class)
public abstract class AppDb extends RoomDatabase {
	private static volatile AppDb instance = null;

	public abstract KmaAreaCodesDao kmaAreaCodesDao();

	public abstract FavoriteAddressDao favoriteAddressDao();

	public abstract TimeZoneIdDao timeZoneIdDao();

	public abstract AlarmDao alarmDao();

	public abstract WidgetDao widgetDao();

	public abstract DailyPushNotificationDao dailyPushNotificationDao();

	public static synchronized AppDb getInstance(Context context) {
		if (instance == null) {
			instance = Room.databaseBuilder(context, AppDb.class, "appdb")
					.createFromAsset("db/appdb.db").build();
		}
		return instance;
	}

	public static void closeInstance() {
		instance = null;
	}
}