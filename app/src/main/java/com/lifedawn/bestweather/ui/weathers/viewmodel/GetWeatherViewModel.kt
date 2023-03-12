package com.lifedawn.bestweather.ui.weathers.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import com.lifedawn.bestweather.data.remote.weather.commons.model.WeatherDataDto
import com.lifedawn.bestweather.data.remote.weather.kma.usecase.GetAreaCodeUseCase
import com.lifedawn.bestweather.data.remote.weather.kma.usecase.GetKmaWeatherUseCase
import com.lifedawn.bestweather.data.remote.weather.metnorway.usecase.GetMetNorwayWeatherUseCase
import com.lifedawn.bestweather.data.remote.weather.owm.usecase.GetOwmWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
        viewModelScope.launch {
            _kmaFlow.value = ApiResponse.Loading

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
        viewModelScope.launch {
            _metNorwayFlow.value = ApiResponse.Loading

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
        viewModelScope.launch {
            _owmFlow.value = ApiResponse.Loading

            owmUseCase.getWeatherData(weatherDataTypes, latitude, longitude, zoneId).collect { result ->
                _owmFlow.emit(ApiResponse.Success(result))
            }
        }
    }

}