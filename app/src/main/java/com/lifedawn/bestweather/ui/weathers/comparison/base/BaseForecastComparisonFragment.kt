package com.lifedawn.bestweather.ui.weathers.comparison.base

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.BundleKey
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.MyApplication
import com.lifedawn.bestweather.databinding.BaseLayoutForecastComparisonBinding
import com.lifedawn.bestweather.ui.weathers.customview.DateView
import com.lifedawn.bestweather.ui.weathers.customview.ICleaner
import com.lifedawn.bestweather.ui.weathers.customview.NotScrolledView
import java.time.ZoneId

open class BaseForecastComparisonFragment : Fragment() {
    protected var binding: BaseLayoutForecastComparisonBinding? = null
    protected var dateRow: DateView? = null
    protected var tempUnitText: String? = null
    protected var bundle: Bundle? = null
    protected var latitude: Double? = null
    protected var longitude: Double? = null
    protected var addressName: String? = null
    protected var countryCode: String? = null
    protected var zoneId: ZoneId? = null
    protected var notScrolledViews: Array<NotScrolledView>?
    protected var customViewList: MutableList<ICleaner> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = savedInstanceState ?: arguments
        latitude = bundle!!.getDouble(BundleKey.Latitude.name)
        longitude = bundle!!.getDouble(BundleKey.Longitude.name)
        addressName = bundle!!.getString(BundleKey.AddressName.name)
        countryCode = bundle!!.getString(BundleKey.CountryCode.name)
        zoneId = bundle!!.getSerializable(BundleKey.TimeZone.name) as ZoneId?
        tempUnitText = MyApplication.VALUE_UNIT_OBJ.tempUnitText
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BaseLayoutForecastComparisonBinding.inflate(inflater, container, false)
        val layoutParams = binding!!.toolbar.root.layoutParams as LinearLayout.LayoutParams
        layoutParams.topMargin = MyApplication.getStatusBarHeight()
        binding!!.toolbar.root.layoutParams = layoutParams
        return binding!!.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(bundle)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.adViewBelowScrollView.loadAd(AdRequest.Builder().build())
        binding!!.adViewBelowScrollView.adListener = object : AdListener() {
            override fun onAdClosed() {
                super.onAdClosed()
                binding!!.adViewBelowScrollView.loadAd(AdRequest.Builder().build())
            }
        }
        binding!!.toolbar.backBtn.setOnClickListener { parentFragmentManager.popBackStackImmediate() }
        binding!!.scrollview.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (dateRow != null) {
                dateRow!!.reDraw(scrollX)
            }
            if (notScrolledViews != null) {
                for (notScrolledView in notScrolledViews!!) {
                    notScrolledView.reDraw(scrollX)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        for (iCleaner in customViewList) {
            iCleaner?.clear()
        }
        customViewList.clear()
        binding!!.datetime.removeAllViews()
        binding!!.metNorway.removeAllViews()
        binding!!.accu.removeAllViews()
        binding!!.kma.removeAllViews()
        binding!!.owm.removeAllViews()
        binding!!.extraView.removeAllViews()
        binding = null
    }

    protected fun createValueUnitsDescription(weatherSourceUnitObjs: List<WeatherSourceUnitObj>) {
        var stringBuilder: StringBuilder? = StringBuilder()
        for (weatherSourceTypeObj in weatherSourceUnitObjs) {
            if (weatherSourceTypeObj.haveRain || weatherSourceTypeObj.haveSnow) {
                val rainUnit = "mm"
                var snowUnit: String? = null
                var weatherSourceTypeName: String? = null
                when (weatherSourceTypeObj.weatherProviderType) {
                    ACCU_WEATHER -> {
                        weatherSourceTypeName = getString(R.string.accu_weather)
                        snowUnit = "cm"
                    }
                    WeatherProviderType.OWM_ONECALL -> {
                        weatherSourceTypeName = getString(R.string.owm)
                        snowUnit = "mm"
                    }
                    WeatherProviderType.MET_NORWAY -> {
                        weatherSourceTypeName = getString(R.string.met)
                        snowUnit = "mm"
                    }
                    else -> {
                        weatherSourceTypeName = getString(R.string.kma)
                        snowUnit = "cm"
                    }
                }
                if (stringBuilder!!.length > 0) {
                    stringBuilder.append("\n")
                }
                stringBuilder.append(weatherSourceTypeName).append(": ")
                if (weatherSourceTypeObj.haveRain) {
                    stringBuilder.append(getString(R.string.rain)).append(" ").append(rainUnit)
                }
                if (weatherSourceTypeObj.haveSnow) {
                    if (weatherSourceTypeObj.haveRain) {
                        stringBuilder.append(" ")
                    }
                    stringBuilder.append(getString(R.string.snow)).append(" ").append(snowUnit)
                }
            }
        }
        if (stringBuilder!!.length > 0) {
            val textView = TextView(context)
            val layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.gravity = Gravity.RIGHT
            textView.layoutParams = layoutParams
            textView.setTextColor(Color.GRAY)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            textView.text = stringBuilder.toString()
            textView.includeFontPadding = false
            binding!!.extraView.removeAllViews()
            binding!!.extraView.addView(textView)
            binding!!.extraView.visibility = View.VISIBLE
        } else {
            binding!!.extraView.visibility = View.GONE
        }
        stringBuilder = null
    }

    protected class WeatherSourceUnitObj(val weatherProviderType: WeatherProviderType, val haveRain: Boolean, val haveSnow: Boolean)
}