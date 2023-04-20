package com.lifedawn.bestweather.ui.weathers.simple.aqicn

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.asynclayoutinflater.view.AsyncLayoutInflater.OnInflateFinishedListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.ViewModelProvider
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.BundleKey
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto.Current.isHasO3
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto.Current.isHasPm10
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto.Current.isHasPm25
import com.lifedawn.bestweather.data.remote.retrofit.responses.aqicn.AqiCnGeolocalizedFeedResponse
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.getGradeColorId
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.getGradeDescription
import com.lifedawn.bestweather.databinding.AirQualityItemBinding
import com.lifedawn.bestweather.databinding.FragmentAirQualitySimpleBinding
import com.lifedawn.bestweather.databinding.LoadingViewAsyncBinding
import com.lifedawn.bestweather.ui.weathers.view.WeatherFragment
import com.lifedawn.bestweather.ui.weathers.detail.aqicn.DetailAirQualityFragment
import com.lifedawn.bestweather.ui.weathers.detail.aqicn.DetailAirQualityFragment.Companion.setResponse
import com.lifedawn.bestweather.ui.weathers.simple.interfaces.IWeatherValues
import com.lifedawn.bestweather.ui.weathers.viewmodel.WeatherFragmentViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class SimpleAirQualityFragment : Fragment(), IWeatherValues, OnInflateFinishedListener {
    private var binding: FragmentAirQualitySimpleBinding? = null
    private var asyncBinding: LoadingViewAsyncBinding? = null
    private var airQualityDto: AirQualityDto? = null
    private var aqiCnGeolocalizedFeedResponse: AqiCnGeolocalizedFeedResponse? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var zoneId: ZoneId? = null
    private var bundle: Bundle? = null
    private var weatherFragmentViewModel: WeatherFragmentViewModel? = null
    fun setAirQualityDto(airQualityDto: AirQualityDto?): SimpleAirQualityFragment {
        this.airQualityDto = airQualityDto
        return this
    }

    fun setAqiCnGeolocalizedFeedResponse(aqiCnGeolocalizedFeedResponse: AqiCnGeolocalizedFeedResponse?): SimpleAirQualityFragment {
        this.aqiCnGeolocalizedFeedResponse = aqiCnGeolocalizedFeedResponse
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = savedInstanceState ?: arguments
        latitude = bundle!!.getDouble(BundleKey.Latitude.name)
        longitude = bundle!!.getDouble(BundleKey.Longitude.name)
        zoneId = bundle!!.getSerializable(BundleKey.TimeZone.name) as ZoneId?
        weatherFragmentViewModel = ViewModelProvider(requireParentFragment()).get(
            WeatherFragmentViewModel::class.java
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        asyncBinding = LoadingViewAsyncBinding.inflate(inflater, container, false)
        val asyncLayoutInflater = AsyncLayoutInflater(requireContext())
        asyncLayoutInflater.inflate(R.layout.fragment_air_quality_simple, container, this)
        return asyncBinding!!.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        aqiCnGeolocalizedFeedResponse = null
        airQualityDto = null
        binding = null
        asyncBinding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun setValuesToViews() {
        //응답 실패한 경우
        if (!airQualityDto.isSuccessful()) {
            binding!!.progressResultView.onFailed(getString(R.string.error))
            binding!!.weatherCardViewHeader.detailForecast.visibility = View.GONE
        } else {
            binding!!.weatherCardViewHeader.detailForecast.visibility = View.VISIBLE
            binding!!.progressResultView.onSuccessful()

            //측정소와의 거리 계산 후 50km이상의 거리에 있으면 표시보류
            val noData = getString(R.string.noData)
            binding!!.measuringStationName.text = if (airQualityDto!!.cityName != null) airQualityDto!!.cityName else noData
            val overallGrade = airQualityDto!!.aqi
            val currentOverallDescription = getGradeDescription(overallGrade)
            val currentOverallColor = getGradeColorId(overallGrade)
            binding!!.currentAirquality.text = if (overallGrade == -1) noData else currentOverallDescription
            binding!!.currentAirquality.setTextColor(currentOverallColor)
            if (!airQualityDto!!.current!!.isHasPm10) {
                addGridItem(null, R.string.pm10_str, R.drawable.pm10)
            } else {
                addGridItem(airQualityDto!!.current!!.pm10, R.string.pm10_str, R.drawable.pm10)
            }
            if (!airQualityDto!!.current!!.isHasPm25) {
                addGridItem(null, R.string.pm25_str, R.drawable.pm25)
            } else {
                addGridItem(airQualityDto!!.current!!.pm25, R.string.pm25_str, R.drawable.pm25)
            }
            if (!airQualityDto!!.current!!.isHasO3) {
                addGridItem(null, R.string.o3_str, R.drawable.o3)
            } else {
                addGridItem(airQualityDto!!.current!!.o3, R.string.o3_str, R.drawable.o3)
            }
            if (!airQualityDto!!.current!!.isHasCo) {
                addGridItem(null, R.string.co_str, R.drawable.co)
            } else {
                addGridItem(airQualityDto!!.current!!.co, R.string.co_str, R.drawable.co)
            }
            if (!airQualityDto!!.current!!.isHasSo2) {
                addGridItem(null, R.string.so2_str, R.drawable.so2)
            } else {
                addGridItem(airQualityDto!!.current!!.so2, R.string.so2_str, R.drawable.so2)
            }
            if (!airQualityDto!!.current!!.isHasNo2) {
                addGridItem(null, R.string.no2_str, R.drawable.no2)
            } else {
                addGridItem(airQualityDto!!.current!!.no2, R.string.no2_str, R.drawable.no2)
            }
            val dateTimeFormatter = DateTimeFormatter.ofPattern("M.d E", Locale.getDefault())
            val forecastList = airQualityDto!!.dailyForecastList
            val textColor = Color.WHITE
            var layoutInflater = LayoutInflater.from(context)
            val labelView = layoutInflater!!.inflate(R.layout.air_quality_simple_forecast_item, null)
            labelView.findViewById<View>(R.id.date).visibility = View.INVISIBLE
            (labelView.findViewById<View>(R.id.pm10) as TextView).text = getString(R.string.air_quality)
            (labelView.findViewById<View>(R.id.pm25) as TextView).visibility = View.GONE
            (labelView.findViewById<View>(R.id.o3) as TextView).visibility = View.GONE
            (labelView.findViewById<View>(R.id.date) as TextView).setTextColor(textColor)
            (labelView.findViewById<View>(R.id.pm10) as TextView).setTextColor(textColor)
            //((TextView) labelView.findViewById(R.id.pm25)).setTextColor(textColor);
            //((TextView) labelView.findViewById(R.id.o3)).setTextColor(textColor);
            binding!!.forecast.addView(labelView)
            for (forecastObj in forecastList) {
                val forecastItemView = layoutInflater.inflate(R.layout.air_quality_simple_forecast_item, null)
                (forecastItemView.findViewById<View>(R.id.date) as TextView).text = forecastObj.date.format(dateTimeFormatter)
                (forecastItemView.findViewById<View>(R.id.date) as TextView).setTextColor(textColor)
                forecastItemView.findViewById<View>(R.id.pm25).visibility = View.GONE
                forecastItemView.findViewById<View>(R.id.o3).visibility = View.GONE
                var grade = -1
                if (forecastObj.isHasPm10()) {
                    grade = Math.max(grade, forecastObj.pm10!!.avg)
                }
                if (forecastObj.isHasPm25()) {
                    grade = Math.max(grade, forecastObj.pm25!!.avg)
                }
                if (forecastObj.isHasO3()) {
                    grade = Math.max(grade, forecastObj.o3!!.avg)
                }
                (forecastItemView.findViewById<View>(R.id.pm10) as TextView).text = if (grade == -1) noData else getGradeDescription(grade)
                (forecastItemView.findViewById<View>(R.id.pm10) as TextView).setTextColor(
                    if (grade == -1) ContextCompat.getColor(
                        requireContext().applicationContext,
                        R.color.not_data_color
                    ) else getGradeColorId(grade)
                )

                /*
				((TextView) forecastItemView.findViewById(R.id.pm25)).setText(
						!forecastObj.isHasPm25() ? noData : AqicnResponseProcessor.getGradeDescription(forecastObj.getPm25().getAvg()));
				((TextView) forecastItemView.findViewById(R.id.pm25)).setTextColor(!forecastObj.isHasPm25() ?
						ContextCompat.getColor(requireContext().getApplicationContext(),
								R.color.not_data_color) : AqicnResponseProcessor.getGradeColorId(forecastObj.getPm25().getAvg()));

				((TextView) forecastItemView.findViewById(R.id.o3)).setText(
						!forecastObj.isHasO3() ? noData : AqicnResponseProcessor.getGradeDescription(forecastObj.getO3().getAvg()));
				((TextView) forecastItemView.findViewById(R.id.o3)).setTextColor(!forecastObj.isHasO3() ?
						ContextCompat.getColor(requireContext().getApplicationContext(),
								R.color.not_data_color) : AqicnResponseProcessor.getGradeColorId(forecastObj.getO3().getAvg()));
				 */binding!!.forecast.addView(forecastItemView)
            }
            layoutInflater = null
        }
    }

    protected fun addGridItem(value: Int?, labelDescriptionId: Int, labelIconId: Int): View {
        val itemBinding = AirQualityItemBinding.inflate(layoutInflater)
        itemBinding.labelIcon.visibility = View.GONE
        itemBinding.label.setText(labelDescriptionId)
        itemBinding.label.setTextColor(Color.WHITE)
        itemBinding.valueInt.visibility = View.GONE

        //((TextView) gridItem.findViewById(R.id.value_int)).setText(value == null ? "?" : value.toString());
        //((TextView) gridItem.findViewById(R.id.value_int)).setTextColor(AppTheme.getTextColor(requireContext().getApplicationContext(), FragmentType.Simple));
        itemBinding.valueStr.text = if (value == null) getString(R.string.noData) else getGradeDescription(value)
        itemBinding.valueStr.setTextColor(
            if (value == null) ContextCompat.getColor(requireContext(), R.color.not_data_color) else getGradeColorId(
                value
            )
        )
        val cellCount = binding!!.grid.childCount
        val row = cellCount / binding!!.grid.columnCount
        val column = cellCount % binding!!.grid.columnCount
        val layoutParams = GridLayout.LayoutParams()
        layoutParams.columnSpec = GridLayout.spec(column, GridLayout.FILL, 1f)
        layoutParams.rowSpec = GridLayout.spec(row, GridLayout.FILL, 1f)
        binding!!.grid.addView(itemBinding.root, layoutParams)
        return itemBinding.root
    }

    override fun onInflateFinished(view: View, resid: Int, parent: ViewGroup?) {
        binding = FragmentAirQualitySimpleBinding.bind(view)
        asyncBinding!!.root.addView(binding!!.root)
        binding!!.progressResultView.setContentView(binding!!.group)
        binding!!.progressResultView.setTextColor(Color.WHITE)
        binding!!.weatherCardViewHeader.forecastName.setText(R.string.air_quality)
        binding!!.weatherCardViewHeader.compareForecast.visibility = View.GONE
        binding!!.weatherCardViewHeader.detailForecast.setOnClickListener {
            val detailAirQualityFragment = DetailAirQualityFragment()
            setResponse(aqiCnGeolocalizedFeedResponse)
            val bundle = Bundle()
            bundle.putSerializable(BundleKey.TimeZone.name, zoneId)
            bundle.putDouble(BundleKey.Latitude.name, latitude!!)
            bundle.putDouble(BundleKey.Longitude.name, longitude!!)
            detailAirQualityFragment.arguments = bundle
            val tag = getString(R.string.tag_detail_air_quality_fragment)
            val fragmentManager = parentFragment!!.parentFragmentManager
            fragmentManager.beginTransaction().hide(
                fragmentManager.findFragmentByTag(WeatherFragment::class.java.name)!!
            ).add(
                R.id.fragment_container,
                detailAirQualityFragment, tag
            ).addToBackStack(tag).commit()
        }
        setValuesToViews()
        asyncBinding!!.progressCircular.visibility = View.GONE
        asyncBinding!!.progressCircular.pauseAnimation()
        weatherFragmentViewModel!!.onResumeWithAsync(this)
    }
}