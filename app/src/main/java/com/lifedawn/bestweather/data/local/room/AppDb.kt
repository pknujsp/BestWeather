package com.lifedawn.bestweather.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lifedawn.bestweather.data.local.room.dao.*
import com.lifedawn.bestweather.data.local.room.dto.*
import com.lifedawn.bestweather.data.local.timezone.model.TimeZoneIdDao
import com.lifedawn.bestweather.data.local.timezone.model.TimeZoneIdDto

@Database(
    entities = [KmaAreaCodeDto::class, FavoriteAddressDto::class, AlarmDto::class, WidgetDto::class, DailyPushNotificationDto::class, TimeZoneIdDto::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    RoomTypeConverter::class
)
abstract class AppDb : RoomDatabase() {
    abstract fun kmaAreaCodesDao(): KmaAreaCodesDao
    abstract fun favoriteAddressDao(): FavoriteAddressDao
    abstract fun timeZoneIdDao(): TimeZoneIdDao
    abstract fun alarmDao(): AlarmDao
    abstract fun widgetDao(): WidgetDao
    abstract fun dailyPushNotificationDao(): DailyPushNotificationDao
}