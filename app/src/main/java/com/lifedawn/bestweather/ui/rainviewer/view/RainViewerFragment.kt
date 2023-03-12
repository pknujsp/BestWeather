package com.lifedawn.bestweather.ui.rainviewer.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.MarkerOptions
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.LocationLifeCycleObserver
import com.lifedawn.bestweather.commons.classes.location.FusedLocation
import com.lifedawn.bestweather.commons.constants.BundleKey
import com.lifedawn.bestweather.commons.views.base.BaseFragment
import com.lifedawn.bestweather.databinding.FragmentRainViewerBinding
import com.lifedawn.bestweather.ui.rainviewer.viewmodel.RainViewerViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class RainViewerFragment : BaseFragment<FragmentRainViewerBinding>(R.layout.fragment_rain_viewer), OnMapReadyCallback,
    GoogleMap.OnCameraIdleListener {
    @Inject private lateinit var fusedLocation: FusedLocation

    private lateinit var marker: MarkerOptions
    private val rainViewerViewModel: RainViewerViewModel by viewModels()
    private lateinit var googleMap: GoogleMap
    private var locationLifeCycleObserver: LocationLifeCycleObserver? = null
    private val timer: Timer = Timer()
    private var timerTask: TimerTask? = null
    private var mapContentPadding: Int = 0

    companion object {
        const val TAG = "RainViewerFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationLifeCycleObserver = LocationLifeCycleObserver(requireActivity().activityResultRegistry, requireActivity())
        lifecycle.addObserver(locationLifeCycleObserver!!)

        arguments?.apply {
            rainViewerViewModel.latitude = getDouble(BundleKey.Latitude.name)
            rainViewerViewModel.longitude = getDouble(BundleKey.Longitude.name)
            rainViewerViewModel.simpleMode = getBoolean("simpleMode", false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme{
                    test()
                }
            }
        }

        return view
    }

    @Preview
    @Composable
    private fun test(){
        Text(text = "Test")
    }

    override fun onMapReady(p0: GoogleMap) {
        TODO("Not yet implemented")
    }

    override fun onCameraIdle() {
        TODO("Not yet implemented")
    }


}