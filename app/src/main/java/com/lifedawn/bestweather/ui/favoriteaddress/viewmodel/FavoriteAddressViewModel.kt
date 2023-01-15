package com.lifedawn.bestweather.ui.favoriteaddress.viewmodel

import androidx.lifecycle.ViewModel
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

    fun getAll() {
        CoroutineScope(Dispatchers.IO).launch {
            _allFavoriteAddressListFlow.value = ApiResponse.Success(favoriteAddressRepository.getAll())
        }
    }

    fun get(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            _favoriteAddressFlow.value = ApiResponse.Success(favoriteAddressRepository.get(id))
        }
    }

    fun size() {
        CoroutineScope(Dispatchers.IO).launch {
            _sizeFlow.value = ApiResponse.Success(favoriteAddressRepository.size())
        }
    }

    fun contains(latitude: String, longitude: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _containsFlow.value = ApiResponse.Success(favoriteAddressRepository.contains(latitude, longitude))
        }
    }

    fun add(favoriteAddressDto: FavoriteAddressDto) {
        CoroutineScope(Dispatchers.IO).launch {
            _addFlow.value = ApiResponse.Success(favoriteAddressRepository.add(favoriteAddressDto))
        }
    }

    fun delete(favoriteAddressDto: FavoriteAddressDto) {
        CoroutineScope(Dispatchers.IO).launch {
            _deleteFlow.value = ApiResponse.Success(favoriteAddressRepository.delete(favoriteAddressDto))
        }
    }
}