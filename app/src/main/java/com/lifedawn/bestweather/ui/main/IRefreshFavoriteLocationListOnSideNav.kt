package com.lifedawn.bestweather.ui.main

import android.os.Bundle
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import java.io.Serializable

interface IRefreshFavoriteLocationListOnSideNav : Serializable {
    fun onRefreshedFavoriteLocationsList(requestKey: String?, bundle: Bundle)
    fun refreshFavorites(callback: DbQueryCallback<List<FavoriteAddressDto?>?>)
    fun createLocationsList(result: List<FavoriteAddressDto>)
    fun onResultMapFragment(newFavoriteAddressDto: FavoriteAddressDto?)
}