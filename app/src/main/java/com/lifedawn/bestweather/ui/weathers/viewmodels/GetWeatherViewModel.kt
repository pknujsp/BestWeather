package com.lifedawn.bestweather.ui.weathers.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lifedawn.bestweather.commons.classes.FusedLocation.MyLocationCallback
import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto
import com.lifedawn.bestweather.data.local.favoriteaddress.repository.FavoriteAddressRepositoryImpl
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import com.lifedawn.bestweather.data.remote.weather.commons.model.WeatherDataDto
import com.lifedawn.bestweather.data.remote.weather.kma.usecase.GetAreaCodeUseCase
import com.lifedawn.bestweather.data.remote.weather.kma.usecase.GetKmaWeatherUseCase
import com.lifedawn.bestweather.data.remote.weather.metnorway.usecase.GetMetNorwayWeatherUseCase
import com.lifedawn.bestweather.data.remote.weather.owm.usecase.GetOwmWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class GetWeatherViewModel @Inject constructor(
    private val kmaUseCase: GetKmaWeatherUseCase,
    private val metNorwayUseCase: GetMetNorwayWeatherUseCase,
    private val owmUseCase: GetOwmWeatherUseCase,
    private val kmaAreaCodeUseCase: GetAreaCodeUseCase
) : ViewModel() {

    private val _kmaFlow = MutableStateFlow<ApiResponse<WeatherDataDto>>(ApiResponse.Empty)
    val kmaFlow = _kmaFlow.asStateFlow()

    private val _owmFlow = MutableStateFlow<ApiResponse<WeatherDataDto>>(ApiResponse.Empty)
    val owmFlow = _owmFlow.asStateFlow()

    private val _metNorwayFlow = MutableStateFlow<ApiResponse<WeatherDataDto>>(ApiResponse.Empty)
    val metNorwayFlow = _metNorwayFlow.asStateFlow()


    fun getKmaWeatherData(
        weatherDataTypes: Set<WeatherDataType>,
        latitude: Double,
        longitude: Double,
        zoneId: ZoneId
    ) {
        _kmaFlow.value = ApiResponse.Loading

        CoroutineScope(Dispatchers.IO).launch {
            kmaAreaCodeUseCase.getAreaCode(latitude, longitude).collect { areaCode ->
                kmaUseCase.getWeatherData(weatherDataTypes, areaCode, latitude, longitude, zoneId).collect { result ->
                    _kmaFlow.value = ApiResponse.Success(result)
                }
            }

        }
    }

    fun getMetNorwayWeatherData(
        weatherDataTypes: Set<WeatherDataType>,
        latitude: Double,
        longitude: Double,
        zoneId: ZoneId
    ) {
        _metNorwayFlow.value = ApiResponse.Loading

        CoroutineScope(Dispatchers.IO).launch {
            metNorwayUseCase.getWeatherData(latitude, longitude, zoneId).collect { result ->
                _metNorwayFlow.value = ApiResponse.Success(result)
            }
        }
    }

    fun getOwmWeatherData(
        weatherDataTypes: Set<WeatherDataType>,
        latitude: Double,
        longitude: Double,
        zoneId: ZoneId
    ) {
        _owmFlow.value = ApiResponse.Loading

        CoroutineScope(Dispatchers.IO).launch {
            owmUseCase.getWeatherData(weatherDataTypes, latitude, longitude, zoneId).collect { result ->
                _owmFlow.emit(ApiResponse.Success(result))
            }
        }
    }

}