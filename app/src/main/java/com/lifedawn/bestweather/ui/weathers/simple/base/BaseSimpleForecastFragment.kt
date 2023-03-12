package com.lifedawn.bestweather.ui.weathers.simple.base

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.asynclayoutinflater.view.AsyncLayoutInflater.OnInflateFinishedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.NetworkStatus
import com.lifedawn.bestweather.commons.constants.BundleKey
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.commons.constants.WeatherValueType
import com.lifedawn.bestweather.databinding.BaseLayoutSimpleForecastBinding
import com.lifedawn.bestweather.databinding.LoadingViewAsyncBinding
import com.lifedawn.bestweather.ui.weathers.simple.interfaces.IWeatherValues
import com.lifedawn.bestweather.ui.weathers.customview.DateView
import com.lifedawn.bestweather.ui.weathers.customview.ICleaner
import com.lifedawn.bestweather.ui.weathers.viewmodel.WeatherFragmentViewModel
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

open class BaseSimpleForecastFragment : Fragment(), IWeatherValues, OnInflateFinishedListener {
    protected var binding: BaseLayoutSimpleForecastBinding? = null
    protected var asyncBinding: LoadingViewAsyncBinding? = null
    protected var dateRow: DateView? = null
    protected var countryCode: String? = null
    protected var mainWeatherProviderType: WeatherProviderType? = null
    protected var needCompare = false
    protected var textSizeMap: MutableMap<WeatherValueType, Int>? = HashMap()
    protected var textColorMap: MutableMap<WeatherValueType, Int>? = HashMap()
    protected var cardBackgroundColor: Int? = null
    protected var networkStatus: NetworkStatus? = null
    protected var bundle: Bundle? = null
    protected var latitude: Double? = null
    protected var longitude: Double? = null
    protected var zoneId: ZoneId? = null
    protected var weatherFragmentViewModel: WeatherFragmentViewModel? = null
    protected var customViewList: MutableList<ICleaner> = ArrayList()
    protected var headerVisibility = View.VISIBLE
    fun setTextSizeMap(textSizeMap: MutableMap<WeatherValueType, Int>?) {
        this.textSizeMap = textSizeMap
    }

    fun setTextColorMap(textColorMap: MutableMap<WeatherValueType, Int>?) {
        this.textColorMap = textColorMap
    }

    fun setCardBackgroundColor(cardBackgroundColor: Int?) {
        this.cardBackgroundColor = cardBackgroundColor
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = savedInstanceState ?: arguments
        countryCode = MyApplication.getLocaleCountryCode()
        mainWeatherProviderType = bundle!!.getSerializable(BundleKey.WeatherProvider.name) as WeatherProviderType?
        latitude = bundle!!.getDouble(BundleKey.Latitude.name, 0.0)
        longitude = bundle!!.getDouble(BundleKey.Longitude.name, 0.0)
        zoneId = bundle!!.getSerializable(BundleKey.TimeZone.name) as ZoneId?
        networkStatus = NetworkStatus.getInstance(requireContext().applicationContext)
        weatherFragmentViewModel = ViewModelProvider(requireParentFragment()).get(
            WeatherFragmentViewModel::class.java
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        asyncBinding = LoadingViewAsyncBinding.inflate(inflater, container, false)
        val asyncLayoutInflater = AsyncLayoutInflater(requireContext())
        asyncLayoutInflater.inflate(R.layout.base_layout_simple_forecast, container, this)
        return asyncBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        for (iCleaner in customViewList) {
            iCleaner?.clear()
        }
        customViewList.clear()
        textColorMap!!.clear()
        textColorMap = null
        textSizeMap!!.clear()
        textSizeMap = null
        binding!!.forecastView.removeAllViews()
        binding = null
        asyncBinding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(bundle)
    }

    override fun setValuesToViews() {}
    protected fun availableNetwork(): Boolean {
        return if (networkStatus!!.networkAvailable()) {
            true
        } else {
            Toast.makeText(context, R.string.disconnected_network, Toast.LENGTH_SHORT)
                .show()
            false
        }
    }

    protected fun createValueUnitsDescription(weatherProviderType: WeatherProviderType, haveRain: Boolean, haveSnow: Boolean) {
        binding!!.extraView.removeAllViews()
        if (haveRain || haveSnow) {
            val rainUnit = "mm"
            var snowUnit: String? = null
            snowUnit =
                if (weatherProviderType === WeatherProviderType.OWM_ONECALL || weatherProviderType === WeatherProviderType.MET_NORWAY) {
                    "mm"
                } else {
                    "cm"
                }
            val stringBuilder = StringBuilder()
            if (haveRain) {
                stringBuilder.append(getString(R.string.rain)).append(" - ").append(rainUnit)
            }
            if (haveSnow) {
                if (stringBuilder.length > 0) {
                    stringBuilder.append(", ")
                }
                stringBuilder.append(getString(R.string.snow)).append(" - ").append(snowUnit)
            }
            val textView = TextView(requireContext().applicationContext)
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
            binding!!.extraView.addView(textView)
            binding!!.extraView.visibility = View.VISIBLE
        } else {
            binding!!.extraView.visibility = View.GONE
        }
    }

    protected fun createValueUnitsDescription(
        weatherProviderType: WeatherProviderType, haveRain: Boolean, haveSnow: Boolean,
        firstDateTime_hasNextNHoursPrecipitation: ZonedDateTime,
        hourAmount: String
    ) {
        createValueUnitsDescription(weatherProviderType, haveRain, haveSnow)
        if (haveRain || haveSnow) {
            var dateTimeFormatter: DateTimeFormatter? = null
            var txt: String? = null
            if (countryCode == "KR") {
                dateTimeFormatter = DateTimeFormatter.ofPattern("d일 E HH시")
                txt = firstDateTime_hasNextNHoursPrecipitation.format(dateTimeFormatter) +
                        " 이후로 표시되는 강수량은 직후 " + hourAmount + "시간 동안의 강수량입니다"
            } else {
                dateTimeFormatter = DateTimeFormatter.ofPattern("HH EEEE d")
                txt = "The precipitation shown from " +
                        firstDateTime_hasNextNHoursPrecipitation.format(dateTimeFormatter) +
                        " is the precipitation for the next " + hourAmount + " hours"
            }
            val textView = TextView(requireContext().applicationContext)
            val layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.gravity = Gravity.RIGHT
            textView.layoutParams = layoutParams
            textView.setTextColor(Color.GRAY)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            textView.text = txt
            textView.includeFontPadding = false
            binding!!.extraView.addView(textView)
        }
    }

    override fun onInflateFinished(view: View, resid: Int, parent: ViewGroup?) {
        binding = BaseLayoutSimpleForecastBinding.bind(view)
        binding!!.root.visibility = View.GONE
        asyncBinding!!.root.addView(binding!!.root)
        if (countryCode != "KR") {
            binding!!.weatherCardViewHeader.compareForecast.visibility = View.GONE
        } else {
            binding!!.weatherCardViewHeader.compareForecast.visibility = View.VISIBLE
        }
        if (cardBackgroundColor != null) {
            binding!!.card.setBackgroundColor(cardBackgroundColor!!)
        }
        binding!!.weatherCardViewHeader.root.visibility = headerVisibility
        binding!!.scrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (dateRow != null) {
                dateRow!!.reDraw(scrollX)
            }
        }
    }

    protected fun onFinishedSetData() {
        binding!!.root.visibility = View.VISIBLE
        asyncBinding!!.progressCircular.visibility = View.GONE
        asyncBinding!!.progressCircular.pauseAnimation()
        weatherFragmentViewModel!!.onResumeWithAsync(this)
    }
}