package com.lifedawn.bestweather.rainviewer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.TileOverlay
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.lifedawn.bestweather.rainviewer.model.RainViewerRepositoryImpl
import com.lifedawn.bestweather.rainviewer.model.RainViewerResponseDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.format.DateTimeFormatter

class RainViewerViewModel() : ViewModel() {
    private val _rainViewerData: MutableLiveData<RainViewerResponseDto?> = MutableLiveData<RainViewerResponseDto?>()
    val rainViewerData: MutableLiveData<RainViewerResponseDto?>
        get() = _rainViewerData

    val frames = ArrayList<RainViewerResponseDto.Data>()
    var lastFramePosition = 0
    val tileOverlays = HashMap<String, TileOverlay>()
    val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd E a hh:mm")

    val optionTileSize = 512 // can be 256 or 512.
    val optionColorScheme = 3 // from 0 to 8. Check the https://rainviewer.com/api/color-schemes.html for additional information
    val optionSmoothData = 1 // 0 - not smooth, 1 - smooth
    val optionSnowColors = 1 // 0 - do not show snow colors, 1 - show snow colors

    var animationPosition = 0
    var animationTimer = false

    var latitude = 0.0
    var longitude = 0.0

    var simpleMode = false

    fun initMap() {
        RainViewerRepositoryImpl.initMap(object : Callback<JsonElement> {
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                if (response.isSuccessful) {
                    val responseDto: RainViewerResponseDto = Gson().fromJson(response.body(),
                            RainViewerResponseDto::class.java)
                    _rainViewerData.postValue(responseDto)
                } else {
                    //fail
                    _rainViewerData.postValue(null)
                }
            }

            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                _rainViewerData.postValue(null)
            }
        })
    }

}