package com.lifedawn.bestweather.ui.weathers.simplefragment.dailyforecast

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.BundleKey
import com.lifedawn.bestweather.commons.constants.WeatherValueType
import com.lifedawn.bestweather.data.MyApplication
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto
import com.lifedawn.bestweather.ui.weathers.FragmentType
import com.lifedawn.bestweather.ui.weathers.WeatherFragment
import com.lifedawn.bestweather.ui.weathers.comparison.dailyforecast.DailyForecastComparisonFragment
import com.lifedawn.bestweather.ui.weathers.detailfragment.base.BaseDetailDailyForecastFragment
import com.lifedawn.bestweather.ui.weathers.detailfragment.dailyforecast.DetailDailyForecastFragment
import com.lifedawn.bestweather.ui.weathers.simplefragment.base.BaseSimpleForecastFragment
import com.lifedawn.bestweather.ui.weathers.customview.DetailDoubleTemperatureView
import com.lifedawn.bestweather.ui.weathers.customview.DoubleWeatherIconView
import com.lifedawn.bestweather.ui.weathers.customview.IconTextView
import com.lifedawn.bestweather.ui.weathers.customview.TextsView
import java.time.format.DateTimeFormatter
import java.util.*

class SimpleDailyForecastFragment : BaseSimpleForecastFragment() {
    fun setDailyForecastDtoList(dailyForecastDtoList: List<DailyForecastDto>?): SimpleDailyForecastFragment {
        Companion.dailyForecastDtoList = dailyForecastDtoList
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        needCompare = true
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onInflateFinished(view: View, resid: Int, parent: ViewGroup?) {
        super.onInflateFinished(view, resid, parent)
        binding!!.weatherCardViewHeader.forecastName.setText(R.string.daily_forecast)
        binding!!.weatherCardViewHeader.compareForecast.setOnClickListener {
            if (availableNetwork()) {
                val comparisonFragment = DailyForecastComparisonFragment()
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
            val arguments = Bundle()
            arguments.putString(BundleKey.AddressName.name, bundle!!.getString(BundleKey.AddressName.name))
            arguments.putSerializable(BundleKey.TimeZone.name, bundle!!.getSerializable(BundleKey.TimeZone.name))
            arguments.putSerializable(BundleKey.WeatherProvider.name, mainWeatherProviderType)
            val copiedList: List<DailyForecastDto> = ArrayList(dailyForecastDtoList)
            Collections.copy(copiedList, dailyForecastDtoList)
            val detailDailyForecastFragment = DetailDailyForecastFragment()
            BaseDetailDailyForecastFragment.setDailyForecastDtoList(copiedList)
            detailDailyForecastFragment.arguments = arguments
            val tag = getString(R.string.tag_detail_daily_forecast_fragment)
            val fragmentManager = parentFragment!!.parentFragmentManager
            fragmentManager.beginTransaction().hide(
                fragmentManager.findFragmentByTag(WeatherFragment::class.java.name)!!
            ).add(
                R.id.fragment_container,
                detailDailyForecastFragment, tag
            ).addToBackStack(tag).commit()
        }
        setValuesToViews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun setValuesToViews() {
        super.setValuesToViews()
        // 날짜 ,낮과 밤의 날씨상태, 강수확률, 강우량, 강설량, 최저/최고 기온
        MyApplication.getExecutorService().submit {
            val context = requireContext().applicationContext
            val WEATHER_ROW_HEIGHT = context.resources.getDimension(R.dimen.singleWeatherIconValueRowHeightInSC).toInt()
            val TEMP_ROW_HEIGHT = context.resources.getDimension(R.dimen.doubleTemperatureRowHeightInSC).toInt()
            val COLUMN_COUNT = dailyForecastDtoList!!.size
            val COLUMN_WIDTH = context.resources.getDimension(R.dimen.valueColumnWidthInSDailyOwm).toInt()
            val VIEW_WIDTH = COLUMN_COUNT * COLUMN_WIDTH
            val weatherIconRow = DoubleWeatherIconView(
                context, FragmentType.Simple, VIEW_WIDTH, WEATHER_ROW_HEIGHT,
                COLUMN_WIDTH
            )
            val probabilityOfPrecipitationRow = IconTextView(
                context, FragmentType.Simple, VIEW_WIDTH,
                COLUMN_WIDTH, R.drawable.pop
            )
            val rainVolumeRow = IconTextView(
                context, FragmentType.Simple, VIEW_WIDTH,
                COLUMN_WIDTH, R.drawable.raindrop
            )
            val snowVolumeRow = IconTextView(
                context, FragmentType.Simple, VIEW_WIDTH,
                COLUMN_WIDTH, R.drawable.snowparticle
            )
            customViewList.add(weatherIconRow)
            customViewList.add(probabilityOfPrecipitationRow)
            customViewList.add(rainVolumeRow)
            customViewList.add(snowVolumeRow)

            //시각 --------------------------------------------------------------------------
            val dateList: MutableList<String> = ArrayList()
            val dateTimeFormatter = DateTimeFormatter.ofPattern("M.d\nE")
            //날씨 아이콘
            val weatherIconObjList: MutableList<DoubleWeatherIconView.WeatherIconObj> = ArrayList()
            //기온, 강수확률, 강수량
            val minTempList: MutableList<Int> = ArrayList()
            val maxTempList: MutableList<Int> = ArrayList()
            val popList: MutableList<String> = ArrayList()
            val rainVolumeList: MutableList<String> = ArrayList()
            val snowVolumeList: MutableList<String> = ArrayList()
            val tempDegree = MyApplication.VALUE_UNIT_OBJ.tempUnitText
            val mm = "mm"
            val cm = "cm"
            var haveSnow = false
            var haveRain = false
            var rainVolume = 0f
            var snowVolume = 0f
            val zeroPrecipitationFormat = "%.1f"
            for ((date, valuesList, minTemp, maxTemp, _, _, isAvailable_toMakeMinMaxTemp) in dailyForecastDtoList!!) {
                if (!isAvailable_toMakeMinMaxTemp) break
                rainVolume = 0f
                snowVolume = 0f
                dateList.add(date.format(dateTimeFormatter))
                minTempList.add(minTemp.replace(tempDegree, "").toInt())
                maxTempList.add(maxTemp.replace(tempDegree, "").toInt())
                if (valuesList.size == 1) {
                    popList.add(valuesList[0].pop)
                    weatherIconObjList.add(
                        DoubleWeatherIconView.WeatherIconObj(
                            ContextCompat.getDrawable(
                                context,
                                valuesList[0].weatherIcon
                            ), valuesList[0].weatherDescription
                        )
                    )
                    if (valuesList[0].rainVolume != null) {
                        rainVolume = valuesList[0].rainVolume.replace(mm, "").replace(cm, "").toFloat()
                    }
                    if (valuesList[0].snowVolume != null) {
                        snowVolume = valuesList[0].snowVolume.replace(mm, "").replace(cm, "").toFloat()
                    }
                } else if (valuesList.size == 2) {
                    popList.add(valuesList[0].pop + "/" + valuesList[1].pop)
                    weatherIconObjList.add(
                        DoubleWeatherIconView.WeatherIconObj(
                            ContextCompat.getDrawable(
                                context,
                                valuesList[0].weatherIcon
                            ),
                            ContextCompat.getDrawable(context, valuesList[1].weatherIcon),
                            valuesList[0].weatherDescription,
                            valuesList[1].weatherDescription
                        )
                    )
                    if (valuesList[0].rainVolume != null || valuesList[1].rainVolume != null) {
                        rainVolume =
                            valuesList[0].rainVolume.replace(mm, "").replace(cm, "").toFloat() + valuesList[1].rainVolume.replace(mm, "")
                                .replace(cm, "").toFloat()
                    }
                    if (valuesList[0].snowVolume != null || valuesList[1].snowVolume != null) {
                        snowVolume =
                            valuesList[0].snowVolume.replace(mm, "").replace(cm, "").toFloat() + valuesList[1].snowVolume.replace(mm, "")
                                .replace(cm, "").toFloat()
                    }
                } else if (valuesList.size == 4) {
                    weatherIconObjList.add(
                        DoubleWeatherIconView.WeatherIconObj(
                            ContextCompat.getDrawable(
                                context,
                                valuesList[1].weatherIcon
                            ),
                            ContextCompat.getDrawable(context, valuesList[2].weatherIcon),
                            valuesList[1].weatherDescription,
                            valuesList[2].weatherDescription
                        )
                    )
                    if (valuesList[0].isHasPrecipitationVolume || valuesList[1].isHasPrecipitationVolume ||
                        valuesList[2].isHasPrecipitationVolume || valuesList[3].isHasPrecipitationVolume
                    ) {
                        rainVolume = valuesList[0].precipitationVolume.replace(mm, "").replace(cm, "")
                            .toFloat() + valuesList[1].precipitationVolume.replace(mm, "").replace(cm, "")
                            .toFloat() + valuesList[2].precipitationVolume.replace(mm, "").replace(cm, "")
                            .toFloat() + valuesList[3].precipitationVolume.replace(mm, "").replace(cm, "").toFloat()
                    }
                }
                rainVolumeList.add(String.format(Locale.getDefault(), zeroPrecipitationFormat, rainVolume))
                snowVolumeList.add(String.format(Locale.getDefault(), zeroPrecipitationFormat, snowVolume))
                if (!haveRain) {
                    if (rainVolume > 0f) {
                        haveRain = true
                    }
                }
                if (!haveSnow) {
                    if (snowVolume > 0f) {
                        haveSnow = true
                    }
                }
            }
            weatherIconRow.setIcons(weatherIconObjList)
            probabilityOfPrecipitationRow.valueList = popList
            rainVolumeRow.valueList = rainVolumeList
            snowVolumeRow.valueList = snowVolumeList
            val dateRow = TextsView(context, VIEW_WIDTH, COLUMN_WIDTH, dateList)
            val tempRow = DetailDoubleTemperatureView(
                requireContext().applicationContext, FragmentType.Simple, VIEW_WIDTH,
                TEMP_ROW_HEIGHT, COLUMN_WIDTH, minTempList, maxTempList
            )
            customViewList.add(dateRow)
            customViewList.add(tempRow)
            val rowLayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val dateRowLayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dateRowLayoutParams.gravity = Gravity.CENTER_VERTICAL
            dateRowLayoutParams.bottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics).toInt()
            if (textSizeMap!!.containsKey(WeatherValueType.date)) {
                dateRow.setValueTextSize(textSizeMap!![WeatherValueType.date]!!)
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
            if (textSizeMap!!.containsKey(WeatherValueType.temp)) {
                tempRow.setTempTextSize(textSizeMap!![WeatherValueType.temp]!!)
            }
            if (textColorMap!!.containsKey(WeatherValueType.date)) {
                dateRow.setValueTextColor(textColorMap!![WeatherValueType.date]!!)
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
            if (textColorMap!!.containsKey(WeatherValueType.temp)) {
                tempRow.setTextColor(textColorMap!![WeatherValueType.temp]!!)
            }
            val finalHaveRain = haveRain
            val finalHaveSnow = haveSnow
            try {
                requireActivity().runOnUiThread {
                    binding!!.forecastView.addView(dateRow, dateRowLayoutParams)
                    binding!!.forecastView.addView(weatherIconRow, rowLayoutParams)
                    binding!!.forecastView.addView(probabilityOfPrecipitationRow, rowLayoutParams)
                    if (finalHaveRain) {
                        binding!!.forecastView.addView(rainVolumeRow, rowLayoutParams)
                    }
                    if (finalHaveSnow) {
                        binding!!.forecastView.addView(snowVolumeRow, rowLayoutParams)
                    }
                    val tempRowLayoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    tempRowLayoutParams.topMargin = resources.getDimension(R.dimen.tempTopMargin).toInt()
                    binding!!.forecastView.addView(tempRow, tempRowLayoutParams)
                    createValueUnitsDescription(mainWeatherProviderType!!, finalHaveRain, finalHaveSnow)
                    onFinishedSetData()
                }
            } catch (e: Exception) {
            }
        }
    }

    companion object {
        private var dailyForecastDtoList: List<DailyForecastDto>? = null
    }
}