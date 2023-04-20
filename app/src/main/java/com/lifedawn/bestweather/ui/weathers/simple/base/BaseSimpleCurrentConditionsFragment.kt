package com.lifedawn.bestweather.ui.weathers.simple.base

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.asynclayoutinflater.view.AsyncLayoutInflater.OnInflateFinishedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.BundleKey
import com.lifedawn.bestweather.commons.constants.ValueUnits
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.databinding.BaseLayoutSimpleCurrentConditionsBinding
import com.lifedawn.bestweather.databinding.LoadingViewAsyncBinding
import com.lifedawn.bestweather.ui.weathers.view.WeatherFragment.ITextColor
import com.lifedawn.bestweather.ui.weathers.simple.interfaces.IWeatherValues
import com.lifedawn.bestweather.ui.weathers.viewmodel.WeatherFragmentViewModel
import java.time.ZoneId

open class BaseSimpleCurrentConditionsFragment : Fragment(), IWeatherValues, ITextColor, OnInflateFinishedListener {
    protected var binding: BaseLayoutSimpleCurrentConditionsBinding? = null
    protected var asyncBinding: LoadingViewAsyncBinding? = null
    protected var tempUnit: ValueUnits? = null
    protected var latitude: Double? = null
    protected var longitude: Double? = null
    protected var addressName: String? = null
    protected var countryCode: String? = null
    protected var mainWeatherProviderType: WeatherProviderType? = null
    protected var zoneId: ZoneId? = null
    protected var bundle: Bundle? = null
    private var weatherFragmentViewModel: WeatherFragmentViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = savedInstanceState ?: arguments
        tempUnit = MyApplication.VALUE_UNIT_OBJ.tempUnit
        latitude = bundle!!.getDouble(BundleKey.Latitude.name)
        longitude = bundle!!.getDouble(BundleKey.Longitude.name)
        addressName = bundle!!.getString(BundleKey.AddressName.name)
        countryCode = bundle!!.getString(BundleKey.CountryCode.name)
        mainWeatherProviderType = bundle!!.getSerializable(
            BundleKey.WeatherProvider.name
        ) as WeatherProviderType?
        zoneId = bundle!!.getSerializable(BundleKey.TimeZone.name) as ZoneId?
        weatherFragmentViewModel = ViewModelProvider(requireParentFragment()).get(
            WeatherFragmentViewModel::class.java
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(bundle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        asyncBinding = LoadingViewAsyncBinding.inflate(inflater, container, false)
        val asyncLayoutInflater = AsyncLayoutInflater(requireContext())
        asyncLayoutInflater.inflate(
            R.layout.base_layout_simple_current_conditions,
            container
        ) { view: View, resid: Int, parent: ViewGroup? -> onInflateFinished(view, resid, parent) }
        return asyncBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun setValuesToViews() {}
    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding!!.windDirectionArrow.setImageDrawable(null)
        binding!!.weatherIcon.setImageDrawable(null)
        binding = null
        asyncBinding = null
    }

    override fun changeColor(color: Int) {
        if (binding != null) {
            binding!!.sky.setTextColor(color)
            binding!!.precipitation.setTextColor(color)
            binding!!.humidity.setTextColor(color)
            binding!!.windDirection.setTextColor(color)
            binding!!.wind.setTextColor(color)
            binding!!.airQualityLabel.setTextColor(color)
            binding!!.airQuality.setTextColor(color)
            binding!!.temperature.setTextColor(color)
            binding!!.tempUnit.setTextColor(color)
            binding!!.feelsLikeTempLabel.setTextColor(color)
            binding!!.feelsLikeTemp.setTextColor(color)
            binding!!.feelsLikeTempUnit.setTextColor(color)
            binding!!.tempDescription.setTextColor(color)
            binding!!.windDirectionArrow.imageTintList = ColorStateList.valueOf(color)
        }
    }

    override fun onInflateFinished(view: View, resid: Int, parent: ViewGroup?) {
        binding = BaseLayoutSimpleCurrentConditionsBinding.bind(view)
        asyncBinding!!.root.addView(binding!!.root)
        asyncBinding!!.progressCircular.visibility = View.GONE
        asyncBinding!!.progressCircular.pauseAnimation()
        weatherFragmentViewModel!!.onResumeWithAsync(this)
    }
}