package com.lifedawn.bestweather.rainviewer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.TileProvider
import com.google.android.gms.maps.model.UrlTileProvider
import com.lifedawn.bestweather.rainviewer.model.RainViewerRepository
import com.lifedawn.bestweather.rainviewer.model.RainViewerResponseDto
import java.time.format.DateTimeFormatter

class RainViewerViewModel(application: Application) : AndroidViewModel(application), RainViewerRepository.IRainViewer {
    private val repository: RainViewerRepository = RainViewerRepository.INSTANCE!!
    val initMapLiveData: MutableLiveData<RainViewerResponseDto?> = repository.initMapLiveData

    val frames = ArrayList<RainViewerResponseDto.Data>()
    var lastFramePosition = 0
    val tileOverlays = HashMap<String, TileOverlay>()
    val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd E a hh:mm")

    val optionTileSize = 512 // can be 256 or 512.
    val optionColorScheme = 4 // from 0 to 8. Check the https://rainviewer.com/api/color-schemes.html for additional information
    val optionSmoothData = 1 // 0 - not smooth, 1 - smooth
    val optionSnowColors = 1 // 0 - do not show snow colors, 1 - show snow colors

    var animationPosition = 0
    var animationTimer = false

    var latitude = 0.0
    var longitude = 0.0

    var simpleMode = false

    override fun initMap() {
        repository.initMap()
    }

}