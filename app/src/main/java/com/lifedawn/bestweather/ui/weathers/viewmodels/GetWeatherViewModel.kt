package com.lifedawn.bestweather.ui.weathers.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lifedawn.bestweather.data.local.room.queryinterfaces.FavoriteAddressQuery
import com.lifedawn.bestweather.commons.classes.FusedLocation.MyLocationCallback
import com.lifedawn.bestweather.data.local.room.repository.FavoriteAddressRepository
import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import com.lifedawn.bestweather.data.remote.weather.commons.GetWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GetWeatherViewModel @Inject constructor(private val getWeatherUseCase: GetWeatherUseCase) : ViewModel() {
    var locationCallback: MyLocationCallback? = null
    private val favoriteAddressRepository: FavoriteAddressRepository
    private val currentLocationLiveData = MutableLiveData<String>()
    @JvmField val favoriteAddressListLiveData: LiveData<List<FavoriteAddressDto>>

    override fun getAll(callback: DbQueryCallback<List<FavoriteAddressDto>>) {
        favoriteAddressRepository.getAll(callback)
    }

    override fun get(id: Int, callback: DbQueryCallback<FavoriteAddressDto>) {
        favoriteAddressRepository[id, callback]
    }

    override fun size(callback: DbQueryCallback<Int>) {
        favoriteAddressRepository.size(callback)
    }

    override fun contains(latitude: String, longitude: String, callback: DbQueryCallback<Boolean>) {
        favoriteAddressRepository.contains(latitude, longitude, callback)
    }

    override fun add(favoriteAddressDto: FavoriteAddressDto, callback: DbQueryCallback<Long>) {
        favoriteAddressRepository.add(favoriteAddressDto, callback)
    }

    override fun delete(favoriteAddressDto: FavoriteAddressDto) {
        favoriteAddressRepository.delete(favoriteAddressDto)
    }

    override fun delete(favoriteAddressDto: FavoriteAddressDto, callback: DbQueryCallback<Boolean>) {
        favoriteAddressRepository.delete(favoriteAddressDto, callback)
    }

    fun getCurrentLocationLiveData(): LiveData<String> {
        return currentLocationLiveData
    }

    fun setCurrentLocationAddressName(addressName: String) {
        currentLocationLiveData.value = addressName
    }

    init {
        favoriteAddressRepository = FavoriteAddressRepository.getINSTANCE()
        favoriteAddressListLiveData = favoriteAddressRepository.allData
    }
}