package com.lifedawn.bestweather.data.local.room.dao

import androidx.room.*
import com.lifedawn.bestweather.data.local.room.dto.WidgetDto
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetDao {
    @Insert
    fun add(widgetDto: WidgetDto): Flow<Long>

    @Query("SELECT * FROM widget_table WHERE appWidgetId = :appWidgetId")
    operator fun get(appWidgetId: Int): Flow<WidgetDto?>

    @Query("SELECT * FROM widget_table WHERE id = :widgetDtoId")
    operator fun get(widgetDtoId: Long): Flow<WidgetDto?>

    @get:Query("SELECT * FROM widget_table") val all: Flow<List<WidgetDto>>

    @Query("SELECT * FROM widget_table WHERE widgetProviderClassName = :widgetProviderClassName")
    fun getAll(widgetProviderClassName: String): Flow<List<WidgetDto>>

    @Query("DELETE FROM widget_table WHERE appWidgetId = :appWidgetId")
    fun delete(appWidgetId: Int)

    @Update(onConflict = OnConflictStrategy.IGNORE, entity = WidgetDto::class)
    fun update(widgetDto: WidgetDto): Flow<Int>
}