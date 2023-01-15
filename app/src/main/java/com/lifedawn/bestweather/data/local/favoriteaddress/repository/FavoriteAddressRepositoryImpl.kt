package com.lifedawn.bestweather.data.local.favoriteaddress.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.lifedawn.bestweather.data.MyApplication
import com.lifedawn.bestweather.data.local.room.AppDb
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import com.lifedawn.bestweather.data.local.room.dao.FavoriteAddressDao
import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FavoriteAddressRepositoryImpl @Inject constructor(
    private val favoriteAddressDao: FavoriteAddressDao
) :
    FavoriteAddressRepository {


    override suspend fun getAll(): List<FavoriteAddressDto> = suspendCoroutine { continuation ->
        continuation.resume(favoriteAddressDao.all)
    }

    override suspend fun get(id: Int): FavoriteAddressDto = suspendCoroutine { continuation ->
        continuation.resume(favoriteAddressDao[id])
    }

    override suspend fun size(): Int = suspendCoroutine { continuation ->
        continuation.resume(favoriteAddressDao.size())
    }

    override suspend fun contains(latitude: String, longitude: String): Boolean = suspendCoroutine { continuation ->
        continuation.resume(favoriteAddressDao.contains(latitude, longitude) == 1)
    }

    override suspend fun add(favoriteAddressDto: FavoriteAddressDto): Long = suspendCoroutine { continuation ->
        continuation.resume(favoriteAddressDao.add(favoriteAddressDto))
    }


    override suspend fun delete(favoriteAddressDto: FavoriteAddressDto): Boolean = suspendCoroutine { continuation ->
        favoriteAddressDao.delete(favoriteAddressDto)
        continuation.resume(true)
    }

}