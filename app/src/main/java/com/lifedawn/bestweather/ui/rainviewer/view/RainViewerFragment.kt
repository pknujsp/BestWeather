package com.lifedawn.bestweather.ui.rainviewer.view

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.FusedLocation
import com.lifedawn.bestweather.commons.classes.FusedLocation.MyLocationCallback
import com.lifedawn.bestweather.commons.classes.LocationLifeCycleObserver
import com.lifedawn.bestweather.commons.classes.MainThreadWorker
import com.lifedawn.bestweather.commons.constants.BundleKey
import com.lifedawn.bestweather.commons.views.BaseFragment
import com.lifedawn.bestweather.data.MyApplication
import com.lifedawn.bestweather.data.remote.rainviewer.dto.RainViewerResponseDto
import com.lifedawn.bestweather.databinding.FragmentRainViewerBinding
import com.lifedawn.bestweather.ui.rainviewer.viewmodel.RainViewerViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.net.URL
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@AndroidEntryPoint
class RainViewerFragment : BaseFragment<FragmentRainViewerBinding>(R.layout.fragment_rain_viewer), OnMapReadyCallback,
    GoogleMap.OnCameraIdleListener {

    private val rainViewerViewModel: RainViewerViewModel by viewModels()
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocation: FusedLocation
    private var locationLifeCycleObserver: LocationLifeCycleObserver? = null
    private val timer: Timer = Timer()
    private var timerTask: TimerTask? = null
    private var mapContentPadding: Int = 0

    private lateinit var marker: MarkerOptions

    companion object {
        const val TAG = "RainViewerFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationLifeCycleObserver = LocationLifeCycleObserver(requireActivity().activityResultRegistry, requireActivity())
        lifecycle.addObserver(locationLifeCycleObserver!!)

        fusedLocation = FusedLocation(requireContext().applicationContext)
        arguments?.apply {
            rainViewerViewModel.latitude = getDouble(BundleKey.Latitude.name)
            rainViewerViewModel.longitude = getDouble(BundleKey.Longitude.name)
            rainViewerViewModel.simpleMode = getBoolean("simpleMode", false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressResultView.setContentView(
            binding.mapButtons.root, binding.previousBtn, binding.datetime, binding.nextBtn, binding
                .mapFragmentContainer
        )
        binding.progressResultView.setBtnOnClickListener {
            rainViewerViewModel.initMap()
        }

        val layoutParams = binding.toolbar.root.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.topMargin = MyApplication.getStatusBarHeight()
        binding.toolbar.root.layoutParams = layoutParams

        if (rainViewerViewModel.simpleMode) {
            binding.toolbar.root.visibility = View.GONE
            binding.mapButtons.currentLocationBtn.visibility = View.GONE
        } else {
            binding.toolbar.fragmentTitle.text = getString(R.string.radar)
            binding.toolbar.backBtn.setOnClickListener { view -> parentFragmentManager.popBackStack() }
        }
        binding.datetime.text = null

        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (binding.root.height > 0 && binding.datetime.top > 0) {
                    binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    mapContentPadding = (binding.root.height - binding.datetime.top + TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        8f, requireContext().resources.displayMetrics
                    )).toInt()

                    val mapFragment = SupportMapFragment()
                    childFragmentManager.beginTransaction().add(binding.mapFragmentContainer.id, mapFragment).commitNow()
                    mapFragment.getMapAsync(this@RainViewerFragment)
                }
            }
        })

    }


    private fun addMarker() {
        googleMap.addMarker(marker)
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        googleMap.setPadding(0, mapContentPadding, 0, mapContentPadding)

        val latLng = LatLng(rainViewerViewModel.latitude, rainViewerViewModel.longitude)

        val size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 26f, resources.displayMetrics).toInt()
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.location)!!
        drawable.setTintList(ColorStateList.valueOf(Color.BLUE))
        val bitmap = drawable.toBitmap(size, size, Bitmap.Config.ARGB_8888)

        marker = MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(bitmap)).alpha(0.65f)
        addMarker()

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 6f))

        googleMap.uiSettings.isZoomControlsEnabled = false
        googleMap.uiSettings.isRotateGesturesEnabled = false
        googleMap.uiSettings.isScrollGesturesEnabled = !rainViewerViewModel.simpleMode
        googleMap.uiSettings.isZoomGesturesEnabled = !rainViewerViewModel.simpleMode
        googleMap.uiSettings.isMapToolbarEnabled = false

        googleMap.setOnCameraIdleListener(if (rainViewerViewModel.simpleMode) this else null)

        binding.mapButtons.zoomInBtn.setOnClickListener { googleMap.animateCamera(CameraUpdateFactory.zoomIn()) }

        binding.mapButtons.zoomOutBtn.setOnClickListener { googleMap.animateCamera(CameraUpdateFactory.zoomOut()) }

        binding.mapButtons.currentLocationBtn.setOnClickListener(View.OnClickListener {
            fusedLocation.findCurrentLocation(object : MyLocationCallback {
                override fun onSuccessful(locationResult: LocationResult) {
                    val result = getBestLocation(locationResult)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(result.latitude, result.longitude), 10f))
                }

                override fun onFailed(fail: MyLocationCallback.Fail) {
                    if (fail == MyLocationCallback.Fail.DISABLED_GPS) {
                        fusedLocation.onDisabledGps(requireActivity(), locationLifeCycleObserver) {
                            if (fusedLocation.isOnGps) {
                                binding.mapButtons.currentLocationBtn.callOnClick()
                            }
                        }
                    } else if (fail == MyLocationCallback.Fail.DENIED_LOCATION_PERMISSIONS) {
                        fusedLocation.onRejectPermissions(requireActivity(), locationLifeCycleObserver, {
                            if (fusedLocation.checkDefaultPermissions()) {
                                binding.mapButtons.currentLocationBtn.callOnClick()
                            }
                        }) { result -> //gps사용 권한
                            //허가남 : 현재 위치 다시 파악
                            //거부됨 : 작업 취소
                            //계속 거부 체크됨 : 작업 취소
                            if (!result.containsValue(false)) {
                                binding.mapButtons.currentLocationBtn.callOnClick()
                            }
                        }
                    }
                }
            }, false)
        })


        lifecycleScope.launchWhenCreated {
            rainViewerViewModel.rainViewerDataFlow.collect {
                it?.apply {
                    initialize(this)
                }
            }
        }

        rainViewerViewModel.initMap()

        binding.previousBtn.setOnClickListener {
            stop()
            showFrame(rainViewerViewModel.animationPosition - 1)
        }

        binding.nextBtn.setOnClickListener {
            stop()
            showFrame(rainViewerViewModel.animationPosition + 1)
        }

        binding.datetime.setOnClickListener {
            playStop()
        }

    }

    override fun onDestroy() {
        timerTask?.cancel()
        super.onDestroy()
    }


    private fun initialize(rainViewerResponseDto: RainViewerResponseDto?) {
        googleMap.clear()
        addMarker()

        rainViewerViewModel.frames.clear()
        rainViewerViewModel.tileOverlays.clear()
        rainViewerViewModel.animationPosition = 0

        if (rainViewerResponseDto != null) {
            if (rainViewerResponseDto.radar.past.size > 0)
                rainViewerViewModel.frames.addAll(rainViewerResponseDto.radar.past)
            if (rainViewerResponseDto.radar.nowcast.size > 0)
                rainViewerViewModel.frames.addAll(rainViewerResponseDto.radar.nowcast)

            rainViewerViewModel.lastFramePosition = rainViewerResponseDto.radar.past.size - 1
            showFrame(rainViewerViewModel.lastFramePosition)
            binding.progressResultView.onSuccessful()
        } else {
            binding.progressResultView.onFailed(getString(R.string.failed_load_radar_data));
        }
    }

    private fun showFrame(position: Int) {
        changeRadarPosition(position)
    }

    private fun changeRadarPosition(position: Int) {
        var pos = position
        if (pos >= rainViewerViewModel.frames.size)
            pos = 0
        else if (pos < 0)
            pos = rainViewerViewModel.frames.size - 1

        val nextFrame = rainViewerViewModel.frames[pos]
        rainViewerViewModel.animationPosition = pos

        addLayer(nextFrame)

        val now = ZonedDateTime.now().toEpochSecond()
        val pastOrForecast = if (nextFrame.time > now) getString(R.string.forecast) else getString(R.string.past)
        val dateTime: String = "$pastOrForecast : " + ZonedDateTime.ofInstant(
            Instant.ofEpochSecond(nextFrame.time.toLong()),
            ZoneId.systemDefault()
        ).format(rainViewerViewModel.dateTimeFormatter)

        binding.datetime.text = dateTime
    }

    private fun addLayer(frame: RainViewerResponseDto.Data) {
        val colorScheme = rainViewerViewModel.optionColorScheme
        val smooth = rainViewerViewModel.optionSmoothData
        val snow = rainViewerViewModel.optionSnowColors
        val tileSize = rainViewerViewModel.optionTileSize

        val tileProvider = object : UrlTileProvider(tileSize, tileSize) {
            override fun getTileUrl(x: Int, y: Int, zoom: Int): URL? {
                val url: String = rainViewerViewModel.rainViewerDataFlow.value!!.host + frame.path +
                        "/$tileSize/$zoom/$x/$y/$colorScheme/$smooth" + "_$snow.png"

                return try {
                    URL(url)
                } catch (e: Exception) {
                    throw AssertionError(e)
                }
            }
        }

        googleMap.clear()
        addMarker()

        rainViewerViewModel.tileOverlays[frame.path]?.clearTileCache()
        rainViewerViewModel.tileOverlays[frame.path]?.remove()

        googleMap.addTileOverlay(
            TileOverlayOptions().tileProvider(tileProvider)
                .transparency(0.1f)
        )?.let { rainViewerViewModel.tileOverlays.put(frame.path, it) }

    }

    private fun stop(): Boolean {
        if (rainViewerViewModel.animationTimer) {
            binding.datetime.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_play_arrow_24), null, null,
                null
            )
            timerTask?.cancel()
            rainViewerViewModel.animationTimer = false
            return true
        }
        return false
    }

    private fun play() {
        if (!rainViewerViewModel.animationTimer)
            rainViewerViewModel.animationTimer = setTimeOut(900)
        else
            showFrame(rainViewerViewModel.animationPosition + 1)
    }

    private fun playStop() {
        if (!stop()) {
            binding.datetime.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_baseline_stop_24
                ), null, null, null
            )
            play()
        }
    }

    private fun setTimeOut(ms: Int): Boolean {
        timerTask?.cancel()
        timerTask = object : TimerTask() {
            override fun run() {
                MainThreadWorker.runOnUiThread {
                    play()
                }
            }
        }
        timer.schedule(timerTask, 100, ms.toLong())

        return true
    }


    override fun onCameraIdle() {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(rainViewerViewModel.latitude, rainViewerViewModel.longitude)))
    }
}