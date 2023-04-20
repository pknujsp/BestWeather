package com.lifedawn.bestweather.data.local.room.dao

import androidx.room.*
import com.lifedawn.bestweather.data.local.room.dto.DailyPushNotificationDto
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyPushNotificationDao {
    @get:Query("SELECT * FROM daily_push_notifications_table") val all: Flow<List<DailyPushNotificationDto>>

    @Query("SELECT * FROM daily_push_notifications_table")
    fun list(): Flow<List<DailyPushNotificationDto>>

    @Query("SELECT count(*) FROM daily_push_notifications_table")
    fun size(): Flow<Int>

    @Query("SELECT * FROM daily_push_notifications_table WHERE id = :id")
    operator fun get(id: Int): Flow<DailyPushNotificationDto>

    @Delete
    fun delete(dailyPushNotificationDto: DailyPushNotificationDto)

    @Insert(entity = DailyPushNotificationDto::class)
    fun add(dailyPushNotificationDto: DailyPushNotificationDto): Flow<Long>

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(dailyPushNotificationDto: DailyPushNotificationDto): Flow<Int>
}