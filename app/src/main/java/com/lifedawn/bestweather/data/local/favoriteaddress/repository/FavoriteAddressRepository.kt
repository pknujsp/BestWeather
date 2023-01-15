package com.lifedawn.bestweather.data.local.favoriteaddress.repository

import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto

interface FavoriteAddressRepository {
    suspend fun getAll(): List<FavoriteAddressDto>
    suspend fun get(id: Int): FavoriteAddressDto
    suspend fun size(): Int
    suspend fun contains(latitude: String, longitude: String): Boolean
    suspend fun add(favoriteAddressDto: FavoriteAddressDto): Long
    suspend fun delete(favoriteAddressDto: FavoriteAddressDto): Boolean
}