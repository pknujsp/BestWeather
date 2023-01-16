package com.lifedawn.bestweather.data.local.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteAddressDao {
    @get:Query("SELECT * FROM favorite_address_table") val all: Flow<List<FavoriteAddressDto>>

    @get:Query("SELECT * FROM favorite_address_table") val allData: Flow<List<FavoriteAddressDto>>

    @Query("SELECT count(*) FROM favorite_address_table")
    fun size(): Flow<Int>

    @Query("SELECT * FROM favorite_address_table WHERE id = :id")
    operator fun get(id: Int): Flow<FavoriteAddressDto?>

    @Query("SELECT EXISTS (SELECT * FROM favorite_address_table WHERE latitude =:latitude AND longitude =:longitude) AS SUCCESS")
    fun contains(latitude: String, longitude: String): Flow<Int>

    @Delete
    fun delete(favoriteAddressDto: FavoriteAddressDto)

    @Insert(entity = FavoriteAddressDto::class, onConflict = OnConflictStrategy.IGNORE)
    fun add(favoriteAddressDto: FavoriteAddressDto): Flow<Long>
}