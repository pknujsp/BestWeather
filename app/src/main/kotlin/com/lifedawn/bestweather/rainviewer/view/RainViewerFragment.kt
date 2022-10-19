package com.lifedawn.bestweather.rainviewer.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.UrlTileProvider
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.FusedLocation
import com.lifedawn.bestweather.commons.classes.LocationLifeCycleObserver
import com.lifedawn.bestweather.commons.enums.BundleKey
import com.lifedawn.bestweather.databinding.FragmentRainViewerBinding
import com.lifedawn.bestweather.rainviewer.model.RainViewerResponseDto
import com.lifedawn.bestweather.rainviewer.viewmodel.RainViewerViewModel
import java.net.MalformedURLException
import java.net.URL
import java.time.ZonedDateTime

class RainViewerFragment : Fragment(), OnMapReadyCallback {
    private lateinit var binding: FragmentRainViewerBinding
    private lateinit var rainViewerViewModel: RainViewerViewModel
    private lateinit var googleMap: GoogleMap
    private val fusedLocation = FusedLocation.getInstance(context)
    private var locationLifeCycleObserver: LocationLifeCycleObserver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rainViewerViewModel = ViewModelProvider(this)[RainViewerViewModel::class.java]

        locationLifeCycleObserver = LocationLifeCycleObserver(requireActivity().activityResultRegistry, requireActivity())
        lifecycle.addObserver(locationLifeCycleObserver!!)

        arguments?.apply {
            rainViewerViewModel.latitude = getDouble(BundleKey.Latitude.name)
            rainViewerViewModel.longitude = getDouble(BundleKey.Longitude.name)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentRainViewerBinding.inflate(inflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(rainViewerViewModel.latitude, rainViewerViewModel.longitude), 5f))

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isRotateGesturesEnabled = false
        googleMap.uiSettings.isScrollGesturesEnabled = false

        googleMap.setOnMyLocationButtonClickListener { false }


        googleMap.setOnMyLocationButtonClickListener {
            if (!fusedLocation.isOnGps) {
                fusedLocation.onDisabledGps(requireActivity(), locationLifeCycleObserver) { }
            }
            false
        }

        try {
            googleMap.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            fusedLocation.onRejectPermissions(requireActivity(), locationLifeCycleObserver, {
                if (fusedLocation.checkDefaultPermissions()) {
                    googleMap.isMyLocationEnabled = true
                }
            }) { result -> //gps사용 권한
                //허가남 : 현재 위치 다시 파악
                //거부됨 : 작업 취소
                //계속 거부 체크됨 : 작업 취소
                googleMap.isMyLocationEnabled = !result.containsValue(false)
            }
        }

        rainViewerViewModel.initMapLiveData.observe(viewLifecycleOwner) {
            initialize(it)
        }

        rainViewerViewModel.initMap()


    }

    override fun onDestroy() {
        super.onDestroy()
    }


    private fun initialize(rainViewerResponseDto: RainViewerResponseDto?) {
        googleMap.clear()
        rainViewerViewModel.mapFrames.clear()
        rainViewerViewModel.radarLayers.clear()
        rainViewerViewModel.animationPosition = 0

        if (rainViewerResponseDto == null) {
            return
        } else {
            rainViewerViewModel.mapFrames.addAll(rainViewerResponseDto.radar.past)
            if (rainViewerResponseDto.radar.nowcast.size > 0) {
                rainViewerViewModel.mapFrames.addAll(rainViewerResponseDto.radar.nowcast)
            }

            rainViewerViewModel.lastPastFramePosition = rainViewerResponseDto.radar.past.size - 1
            showFrame(rainViewerViewModel.lastPastFramePosition)
        }
    }

    private fun showFrame(nextPosition: Int) {
        val preloadingDirection = nextPosition - if (rainViewerViewModel.animationPosition > 0) 1 else -1
        changeRadarPosition(nextPosition)
        changeRadarPosition(nextPosition + preloadingDirection, true)
    }

    private fun changeRadarPosition(position: Int, preloadOnly: Boolean = false) {
        var pos = position
        while (pos >= rainViewerViewModel.mapFrames.size) {
            pos -= rainViewerViewModel.mapFrames.size
        }
        while (pos < 0) {
            pos += rainViewerViewModel.mapFrames.size
        }

        val currentFrame = rainViewerViewModel.mapFrames[rainViewerViewModel.animationPosition]
        val nextFrame = rainViewerViewModel.mapFrames[pos]

        addLayer(nextFrame)

        if (preloadOnly)
            return

        rainViewerViewModel.animationPosition = pos

        if (rainViewerViewModel.radarLayers[currentFrame.path] != null) {
            rainViewerViewModel.radarLayers[currentFrame.path]?.transparency(0.001f)
        }
        rainViewerViewModel.radarLayers[nextFrame.path]?.transparency(1.0f)

        val pastOrForecast = if (nextFrame.time > ZonedDateTime.now().second) "FORECAST" else "PAST"
        //   document.getElementById("timestamp").innerHTML = pastOrForecast + ': ' + (new Date(nextFrame.time * 1000)).toString();
    }

    private fun addLayer(frame: RainViewerResponseDto.Data) {
        if (rainViewerViewModel.radarLayers[frame.path] == null) {
            val colorScheme = rainViewerViewModel.optionColorScheme
            val smooth = rainViewerViewModel.optionSmoothData
            val snow = rainViewerViewModel.optionSnowColors
            val tileSize = rainViewerViewModel.optionTileSize

            rainViewerViewModel.radarLayers[frame.path] = TileOverlayOptions().tileProvider(object : UrlTileProvider(tileSize, tileSize) {
                override fun getTileUrl(x: Int, y: Int, zoom: Int): URL? {
                    val url: String = rainViewerViewModel.initMapLiveData.value!!.host + frame.path +
                            "/$tileSize/$zoom/$x/$y/$colorScheme/" + smooth + "_" + snow + ".png"

                    return try {
                        URL(url)
                    } catch (e: MalformedURLException) {
                        throw AssertionError(e)
                    }
                }

            }).transparency(0.001f)

            googleMap.addTileOverlay(rainViewerViewModel.radarLayers[frame.path]!!)
        }

    }

    private fun checkTileExists(x: Int, y: Int, zoom: Int): Boolean {
        val minZoom = 5
        val maxZoom = 16
        return zoom in minZoom..maxZoom
    }
}