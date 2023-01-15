package com.lifedawn.bestweather.ui.favoriteaddress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifedawn.bestweather.data.local.favoriteaddress.repository.FavoriteAddressRepository
import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteAddressViewModel @Inject constructor(
    private val favoriteAddressRepository: FavoriteAddressRepository
) : ViewModel() {
    private val _allFavoriteAddressListFlow = MutableStateFlow<ApiResponse<List<FavoriteAddressDto>>>(ApiResponse.Empty)
    val allFavoriteAddressListFlow = _allFavoriteAddressListFlow.asStateFlow()

    private val _favoriteAddressFlow = MutableStateFlow<ApiResponse<FavoriteAddressDto>>(ApiResponse.Empty)
    val favoriteAddressFlow = _favoriteAddressFlow.asStateFlow()

    private val _sizeFlow = MutableStateFlow<ApiResponse<Int>>(ApiResponse.Empty)
    val sizeFlow = _sizeFlow.asStateFlow()

    private val _containsFlow = MutableStateFlow<ApiResponse<Boolean>>(ApiResponse.Empty)
    val containsFlow = _containsFlow.asStateFlow()

    private val _addFlow = MutableStateFlow<ApiResponse<Long>>(ApiResponse.Empty)
    val addFlow = _addFlow.asStateFlow()

    private val _deleteFlow = MutableStateFlow<ApiResponse<Boolean>>(ApiResponse.Empty)
    val deleteFlow = _deleteFlow.asStateFlow()

    fun getAll() = viewModelScope.launch {
        _allFavoriteAddressListFlow.value = ApiResponse.Success(favoriteAddressRepository.getAll())
    }


    fun get(id: Int) = viewModelScope.launch {
        _favoriteAddressFlow.value = ApiResponse.Success(favoriteAddressRepository.get(id))
    }


    fun size() = viewModelScope.launch {
        _sizeFlow.value = ApiResponse.Success(favoriteAddressRepository.size())
    }


    fun contains(latitude: String, longitude: String) = viewModelScope.launch {
        _containsFlow.value = ApiResponse.Success(favoriteAddressRepository.contains(latitude, longitude))
    }


    fun add(favoriteAddressDto: FavoriteAddressDto) = viewModelScope.launch {
        _addFlow.value = ApiResponse.Success(favoriteAddressRepository.add(favoriteAddressDto))
    }


    fun delete(favoriteAddressDto: FavoriteAddressDto) = viewModelScope.launch {
        _deleteFlow.value = ApiResponse.Success(favoriteAddressRepository.delete(favoriteAddressDto))
    }

}