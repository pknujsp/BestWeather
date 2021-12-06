package com.lifedawn.bestweather.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.lifedawn.bestweather.room.dao.AlarmDao;
import com.lifedawn.bestweather.room.dao.FavoriteAddressDao;
import com.lifedawn.bestweather.room.dao.KmaAreaCodesDao;
import com.lifedawn.bestweather.room.dto.AlarmDto;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.room.dto.KmaAreaCodeDto;

@Database(entities = {KmaAreaCodeDto.class, FavoriteAddressDto.class, AlarmDto.class}, version = 1, exportSchema = false)
public abstract class AppDb extends RoomDatabase {
	private static volatile AppDb instance = null;

	public abstract KmaAreaCodesDao kmaAreaCodesDao();

	public abstract FavoriteAddressDao favoriteAddressDao();

	public abstract AlarmDao alarmDao();

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