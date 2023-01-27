package com.lifedawn.bestweather.ui.weathers.simplefragment.sunsetrise

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.asynclayoutinflater.view.AsyncLayoutInflater.OnInflateFinishedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.BundleKey
import com.lifedawn.bestweather.databinding.FragmentSunsetriseBinding
import com.lifedawn.bestweather.databinding.LoadingViewAsyncBinding
import com.lifedawn.bestweather.ui.weathers.WeatherFragment
import com.lifedawn.bestweather.ui.weathers.detailfragment.sunsetrise.DetailSunRiseSetFragment
import com.lifedawn.bestweather.ui.weathers.simplefragment.interfaces.IWeatherValues
import com.lifedawn.bestweather.ui.weathers.viewmodels.WeatherFragmentViewModel
import com.luckycatlabs.sunrisesunset.dto.Location
import java.time.ZoneId

class SunsetriseFragment : Fragment(), IWeatherValues, OnInflateFinishedListener {
    private var binding: FragmentSunsetriseBinding? = null
    private var asyncBinding: LoadingViewAsyncBinding? = null
    private var sunSetRiseViewGroup: SunSetRiseViewGroup? = null
    private var location: Location? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var zoneId: ZoneId? = null
    private var bundle: Bundle? = null
    private var registeredReceiver = false
    private var weatherFragmentViewModel: WeatherFragmentViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = savedInstanceState ?: arguments
        latitude = bundle!!.getDouble(BundleKey.Latitude.name)
        longitude = bundle!!.getDouble(BundleKey.Longitude.name)
        zoneId = bundle!!.getSerializable(BundleKey.TimeZone.name) as ZoneId?
        location = Location(latitude!!, longitude!!)
        weatherFragmentViewModel = ViewModelProvider(requireParentFragment()).get(
            WeatherFragmentViewModel::class.java
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        asyncBinding = LoadingViewAsyncBinding.inflate(inflater, container, false)
        val asyncLayoutInflater = AsyncLayoutInflater(requireContext())
        asyncLayoutInflater.inflate(R.layout.fragment_sunsetrise, container, this)
        return asyncBinding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        asyncBinding = null
        binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(bundle)
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (isAdded && intent.action != null) {
                if (Intent.ACTION_TIME_TICK == intent.action) {
                    sunSetRiseViewGroup!!.refresh()
                }
            }
        }
    }

    override fun setValuesToViews() {}
    override fun onDestroy() {
        if (registeredReceiver) {
            try {
                requireActivity().unregisterReceiver(broadcastReceiver)
            } catch (e: Exception) {
            }
        }
        super.onDestroy()
    }

    override fun onInflateFinished(view: View, resid: Int, parent: ViewGroup?) {
        binding = FragmentSunsetriseBinding.bind(view)
        asyncBinding!!.root.addView(binding!!.root)
        binding!!.weatherCardViewHeader.detailForecast.visibility = View.VISIBLE
        binding!!.weatherCardViewHeader.compareForecast.visibility = View.INVISIBLE
        binding!!.weatherCardViewHeader.forecastName.setText(R.string.sun_set_rise)
        binding!!.weatherCardViewHeader.detailForecast.setOnClickListener { v: View? ->
            val detailSunRiseSetFragment = DetailSunRiseSetFragment()
            detailSunRiseSetFragment.arguments = bundle
            val tag = DetailSunRiseSetFragment::class.java.name
            val fragmentManager = parentFragment!!.parentFragmentManager
            fragmentManager.beginTransaction().hide(
                fragmentManager.findFragmentByTag(WeatherFragment::class.java.name)!!
            ).add(
                R.id.fragment_container,
                detailSunRiseSetFragment, tag
            ).addToBackStack(tag).commit()
        }
        sunSetRiseViewGroup =
            SunSetRiseViewGroup(requireContext().applicationContext, location, zoneId) { calcSuccessful: Boolean, night: Boolean ->
                if (calcSuccessful) {
                    if (!registeredReceiver) {
                        registeredReceiver = true

                        //onSunRiseSetListener.onCalcResult(true, night);
                        val intentFilter = IntentFilter()
                        intentFilter.addAction(Intent.ACTION_TIME_TICK)
                        requireActivity().registerReceiver(broadcastReceiver, intentFilter)
                        binding!!.weatherCardViewHeader.detailForecast.visibility = View.VISIBLE
                    }
                } else {
                    binding!!.weatherCardViewHeader.detailForecast.visibility = View.GONE
                    if (registeredReceiver) {
                        registeredReceiver = false
                        requireActivity().unregisterReceiver(broadcastReceiver)
                    } else {
                        //onSunRiseSetListener.onCalcResult(false, false);
                    }
                }
            }
        binding!!.rootLayout.addView(
            sunSetRiseViewGroup,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )
        asyncBinding!!.progressCircular.visibility = View.GONE
        asyncBinding!!.progressCircular.pauseAnimation()
        weatherFragmentViewModel!!.onResumeWithAsync(this)
    }
}