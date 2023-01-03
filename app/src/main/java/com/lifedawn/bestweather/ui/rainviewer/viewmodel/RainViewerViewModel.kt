package com.lifedawn.bestweather.ui.rainviewer.viewmodel

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.TileOverlay
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.lifedawn.bestweather.data.remote.rainviewer.dto.RainViewerResponseDto
import com.lifedawn.bestweather.data.remote.rainviewer.repository.RainViewerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class RainViewerViewModel @Inject constructor(private val rainViewerRepository: RainViewerRepository) : ViewModel() {
    val rainViewerDataFlow = MutableStateFlow<RainViewerResponseDto?>(null)


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
        rainViewerRepository.initMap(object : Callback<JsonElement> {
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                if (response.isSuccessful) {
                    val responseDto: RainViewerResponseDto = Gson().fromJson(
                        response.body(), RainViewerResponseDto::class.java
                    )
                    rainViewerDataFlow.value = responseDto
                } else {
                    rainViewerDataFlow.value = null
                }
            }

            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                rainViewerDataFlow.value = null
            }
        })
    }

}