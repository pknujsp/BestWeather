package com.lifedawn.bestweather.ui.weathers.simplefragment.hourlyforecast

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.BundleKey
import com.lifedawn.bestweather.commons.constants.WeatherValueType
import com.lifedawn.bestweather.data.MyApplication
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto
import com.lifedawn.bestweather.ui.weathers.FragmentType
import com.lifedawn.bestweather.ui.weathers.WeatherFragment
import com.lifedawn.bestweather.ui.weathers.comparison.hourlyforecast.HourlyForecastComparisonFragment
import com.lifedawn.bestweather.ui.weathers.detailfragment.base.BaseDetailHourlyForecastFragment.Companion.setHourlyForecastDtoList
import com.lifedawn.bestweather.ui.weathers.detailfragment.hourlyforecast.DetailHourlyForecastFragment
import com.lifedawn.bestweather.ui.weathers.simplefragment.base.BaseSimpleForecastFragment
import com.lifedawn.bestweather.ui.weathers.customview.*
import java.time.ZonedDateTime
import java.util.*

class SimpleHourlyForecastFragment : BaseSimpleForecastFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        needCompare = true
    }

    override fun onInflateFinished(view: View, resid: Int, parent: ViewGroup?) {
        super.onInflateFinished(view, resid, parent)
        binding!!.weatherCardViewHeader.forecastName.setText(R.string.hourly_forecast)
        binding!!.weatherCardViewHeader.compareForecast.setOnClickListener {
            if (availableNetwork()) {
                val comparisonFragment = HourlyForecastComparisonFragment()
                comparisonFragment.arguments = bundle
                val tag = getString(R.string.tag_comparison_fragment)
                val fragmentManager = parentFragment!!.parentFragmentManager
                fragmentManager.beginTransaction().hide(
                    fragmentManager.findFragmentByTag(WeatherFragment::class.java.name)!!
                ).add(
                    R.id.fragment_container,
                    comparisonFragment, tag
                ).addToBackStack(tag).commit()
            }
        }
        binding!!.weatherCardViewHeader.detailForecast.setOnClickListener {
            val detailHourlyForecastFragment = DetailHourlyForecastFragment()
            val arguments = Bundle()
            arguments.putString(BundleKey.AddressName.name, bundle!!.getString(BundleKey.AddressName.name))
            arguments.putSerializable(BundleKey.TimeZone.name, bundle!!.getSerializable(BundleKey.TimeZone.name))
            arguments.putDouble(BundleKey.Latitude.name, bundle!!.getDouble(BundleKey.Latitude.name))
            arguments.putDouble(BundleKey.Longitude.name, bundle!!.getDouble(BundleKey.Longitude.name))
            arguments.putSerializable(BundleKey.WeatherProvider.name, mainWeatherProviderType)
            val copiedList: List<HourlyForecastDto> = ArrayList(hourlyForecastDtoList)
            Collections.copy(copiedList, hourlyForecastDtoList)
            DetailHourlyForecastFragment.setHourlyForecastDtoList(copiedList)
            detailHourlyForecastFragment.arguments = arguments
            val tag = getString(R.string.tag_detail_hourly_forecast_fragment)
            val fragmentManager = parentFragment!!.parentFragmentManager
            fragmentManager.beginTransaction().hide(
                fragmentManager.findFragmentByTag(WeatherFragment::class.java.name)!!
            ).add(
                R.id.fragment_container,
                detailHourlyForecastFragment, tag
            ).addToBackStack(tag).commitAllowingStateLoss()
        }
        setValuesToViews()
    }

    fun setHourlyForecastDtoList(hourlyForecastDtoList: List<HourlyForecastDto>?) {
        Companion.hourlyForecastDtoList = hourlyForecastDtoList
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setValuesToViews() {
        MyApplication.getExecutorService().submit {
            val context = requireContext().applicationContext
            val weatherRowHeight = context.resources.getDimension(R.dimen.singleWeatherIconValueRowHeightInSC).toInt()
            val columnCount = hourlyForecastDtoList!!.size
            val columnWidth = context.resources.getDimension(R.dimen.valueColumnWidthInSCHourly).toInt()
            val viewWidth = columnCount * columnWidth
            dateRow = DateView(context, FragmentType.Simple, viewWidth, columnWidth)
            val weatherIconRow = SingleWeatherIconView(
                context, FragmentType.Simple, viewWidth, weatherRowHeight,
                columnWidth
            )
            val probabilityOfPrecipitationRow = IconTextView(
                context, FragmentType.Simple, viewWidth,
                columnWidth, R.drawable.pop
            )
            val rainVolumeRow = IconTextView(
                context, FragmentType.Simple, viewWidth,
                columnWidth, R.drawable.raindrop
            )
            val precipitationVolumeRow = IconTextView(
                context, FragmentType.Simple, viewWidth,
                columnWidth, R.drawable.raindrop
            )
            val snowVolumeRow = IconTextView(
                context, FragmentType.Simple, viewWidth,
                columnWidth, R.drawable.snowparticle
            )
            customViewList.add(weatherIconRow)
            customViewList.add(probabilityOfPrecipitationRow)
            customViewList.add(rainVolumeRow)
            customViewList.add(precipitationVolumeRow)
            customViewList.add(snowVolumeRow)
            val weatherIconObjList: MutableList<SingleWeatherIconView.WeatherIconObj> = ArrayList()
            val hourList: MutableList<String> = ArrayList()
            val tempList: MutableList<Int> = ArrayList()
            val popList: MutableList<String> = ArrayList()
            val rainVolumeList: MutableList<String> = ArrayList()
            val snowVolumeList: MutableList<String> = ArrayList()
            val precipitationVolumeList: MutableList<String> = ArrayList()
            val dateTimeList: MutableList<ZonedDateTime> = ArrayList()
            val precipitationVisibleList: MutableList<Boolean> = ArrayList()
            val mm = "mm"
            val cm = "cm"
            val degree = MyApplication.VALUE_UNIT_OBJ.tempUnitText
            var haveSnow = false
            var haveRain = false
            var havePrecipitation = false
            var hasNextNHoursPrecipitation = false
            var firstDateTime_hasNextNHours: ZonedDateTime? = null
            for ((hours, weatherIcon, weatherDescription, _, temp, pop, _, _, _, _, _, _, _, _, _, _, _, _, _, precipitationVolume, rainVolume, snowVolume, _, _, _, isHasPrecipitation, isHasNext6HoursPrecipitation, isHasRain, isHasSnow) in hourlyForecastDtoList!!) {
                dateTimeList.add(hours)
                hourList.add(hours.hour.toString())
                weatherIconObjList.add(
                    SingleWeatherIconView.WeatherIconObj(
                        ContextCompat.getDrawable(context, weatherIcon), weatherDescription
                    )
                )
                tempList.add(temp.replace(degree, "").toInt())
                if (pop != null) popList.add(pop)
                if (rainVolume != null) rainVolumeList.add(rainVolume.replace(mm, "").replace(cm, ""))
                if (snowVolume != null) snowVolumeList.add(snowVolume.replace(mm, "").replace(cm, ""))
                if (precipitationVolume != null) {
                    precipitationVolumeList.add(precipitationVolume.replace(mm, "").replace(cm, ""))
                    if (isHasNext6HoursPrecipitation && firstDateTime_hasNextNHours == null) {
                        firstDateTime_hasNextNHours = hours
                        hasNextNHoursPrecipitation = true
                    }
                }
                if (isHasSnow) {
                    if (!haveSnow) {
                        haveSnow = true
                    }
                }
                if (isHasRain) {
                    if (!haveRain) {
                        haveRain = true
                    }
                }
                if (isHasNext6HoursPrecipitation || isHasPrecipitation) {
                    if (!havePrecipitation) {
                        havePrecipitation = true
                    }
                }
                precipitationVisibleList.add(isHasSnow || isHasRain || isHasPrecipitation)
            }
            dateRow!!.init(dateTimeList)
            probabilityOfPrecipitationRow.valueList = popList
            rainVolumeRow.setValueList(rainVolumeList).setVisibleList(precipitationVisibleList)
            precipitationVolumeRow.setVisibleList(precipitationVisibleList)
            precipitationVolumeRow.valueList = precipitationVolumeList
            snowVolumeRow.setValueList(snowVolumeList).setVisibleList(precipitationVisibleList)
            weatherIconRow.setWeatherImgs(weatherIconObjList)
            val hourRow = TextsView(context, viewWidth, columnWidth, hourList)
            val tempRow = DetailSingleTemperatureView(context, tempList)
            customViewList.add(hourRow)
            customViewList.add(tempRow)
            tempRow.setLineColor(Color.WHITE)
            tempRow.setCircleColor(Color.WHITE)
            if (textSizeMap!!.containsKey(WeatherValueType.date)) {
                dateRow!!.setTextSize(textSizeMap!![WeatherValueType.date]!!)
            }
            if (textSizeMap!!.containsKey(WeatherValueType.time)) {
                hourRow.setValueTextSize(textSizeMap!![WeatherValueType.time]!!)
            }
            if (textSizeMap!!.containsKey(WeatherValueType.temp)) {
                tempRow.setTempTextSizeSp(textSizeMap!![WeatherValueType.temp]!!)
            } else {
                tempRow.setTempTextSizeSp(16)
            }
            if (textSizeMap!!.containsKey(WeatherValueType.pop)) {
                probabilityOfPrecipitationRow.setValueTextSize(textSizeMap!![WeatherValueType.pop]!!)
            }
            if (textSizeMap!!.containsKey(WeatherValueType.rainVolume)) {
                rainVolumeRow.setValueTextSize(textSizeMap!![WeatherValueType.rainVolume]!!)
            }
            if (textSizeMap!!.containsKey(WeatherValueType.snowVolume)) {
                snowVolumeRow.setValueTextSize(textSizeMap!![WeatherValueType.snowVolume]!!)
            }
            if (textColorMap!!.containsKey(WeatherValueType.date)) {
                dateRow!!.setTextColor(textColorMap!![WeatherValueType.date]!!)
            }
            if (textColorMap!!.containsKey(WeatherValueType.time)) {
                hourRow.setValueTextColor(textColorMap!![WeatherValueType.time]!!)
            }
            if (textColorMap!!.containsKey(WeatherValueType.temp)) {
                tempRow.setTextColor(textColorMap!![WeatherValueType.temp]!!)
            } else {
                tempRow.setTextColor(Color.WHITE)
            }
            if (textColorMap!!.containsKey(WeatherValueType.pop)) {
                probabilityOfPrecipitationRow.setTextColor(textColorMap!![WeatherValueType.pop]!!)
            }
            if (textColorMap!!.containsKey(WeatherValueType.rainVolume)) {
                rainVolumeRow.setTextColor(textColorMap!![WeatherValueType.rainVolume]!!)
            }
            if (textColorMap!!.containsKey(WeatherValueType.snowVolume)) {
                snowVolumeRow.setTextColor(textColorMap!![WeatherValueType.snowVolume]!!)
            }
            val rowLayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val finalHaveRain = haveRain
            val finalHaveSnow = haveSnow
            val finalHavePrecipitation = havePrecipitation
            val finalHasNextNHoursPrecipitation = hasNextNHoursPrecipitation
            val finalFirstDateTime_hasNextNHours = firstDateTime_hasNextNHours
            try {
                requireActivity().runOnUiThread {
                    binding!!.forecastView.addView(dateRow, rowLayoutParams)
                    binding!!.forecastView.addView(hourRow, rowLayoutParams)
                    binding!!.forecastView.addView(weatherIconRow, rowLayoutParams)
                    binding!!.forecastView.addView(probabilityOfPrecipitationRow, rowLayoutParams)
                    if (finalHaveRain) {
                        binding!!.forecastView.addView(rainVolumeRow, rowLayoutParams)
                    }
                    if (finalHaveSnow) {
                        binding!!.forecastView.addView(snowVolumeRow, rowLayoutParams)
                    }
                    if (finalHavePrecipitation) {
                        binding!!.forecastView.addView(precipitationVolumeRow, rowLayoutParams)
                    }
                    val tempRowLayoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        context.resources.getDimension(R.dimen.singleTemperatureRowHeightInCOMMON).toInt()
                    )
                    binding!!.forecastView.addView(tempRow, tempRowLayoutParams)
                    if (finalHasNextNHoursPrecipitation) {
                        createValueUnitsDescription(mainWeatherProviderType!!, true, finalHaveSnow, finalFirstDateTime_hasNextNHours!!, "6")
                    } else {
                        createValueUnitsDescription(mainWeatherProviderType!!, finalHaveRain, finalHaveSnow)
                    }
                    onFinishedSetData()
                }
            } catch (e: Exception) {
            }
        }
    }

    companion object {
        private var hourlyForecastDtoList: List<HourlyForecastDto>? = null
    }
}