package com.lifedawn.bestweather.data.local.room.dto

import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "favorite_address_table")
class FavoriteAddressDto {
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Int? = null
    @ColumnInfo var displayName: String? = null
    @ColumnInfo var countryName: String? = null
    @ColumnInfo var countryCode: String? = null
    @ColumnInfo var latitude: String? = null
    @ColumnInfo var longitude: String? = null
    @ColumnInfo var zoneId: String? = null
}