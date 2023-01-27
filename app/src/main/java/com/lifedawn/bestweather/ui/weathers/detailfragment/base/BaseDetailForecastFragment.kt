package com.lifedawn.bestweather.ui.weathers.detailfragment.base

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.BundleKey
import com.lifedawn.bestweather.commons.constants.ValueUnits
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.commons.interfaces.OnClickedListViewItemListener
import com.lifedawn.bestweather.commons.views.BaseFragment
import com.lifedawn.bestweather.data.MyApplication
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto
import com.lifedawn.bestweather.databinding.BaseLayoutDetailForecastBinding
import com.lifedawn.bestweather.databinding.ViewDetailDailyForecastListBinding
import com.lifedawn.bestweather.databinding.ViewDetailHourlyForecastListBinding
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

abstract class BaseDetailForecastFragment() : BaseFragment<BaseLayoutDetailForecastBinding>(
    R.layout.base_layout_detail_forecast
), OnClickedListViewItemListener<Int?> {
    protected var tempUnit: ValueUnits? = null
    protected var windUnit: ValueUnits? = null
    protected var visibilityUnit: ValueUnits? = null
    protected var clockUnit: ValueUnits? = null
    protected var addressName: String? = null
    protected var zoneId: ZoneId? = null
    protected var latitude: Double? = null
    protected var longitude: Double? = null
    protected var executorService = MyApplication.getExecutorService()
    protected var mainWeatherProviderType: WeatherProviderType? = null
    protected var bundle: Bundle? = null
    protected var clickableItem = true
    private val fragmentLifecycleCallbacks: FragmentManager.FragmentLifecycleCallbacks =
        object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
                super.onFragmentStarted(fm, f)
                this@BaseDetailForecastFragment.onFragmentStarted(f)
            }
        }

    protected open fun onFragmentStarted(fragment: Fragment?) {
        clickableItem = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false)
        tempUnit = MyApplication.VALUE_UNIT_OBJ.tempUnit
        windUnit = MyApplication.VALUE_UNIT_OBJ.windUnit
        visibilityUnit = MyApplication.VALUE_UNIT_OBJ.visibilityUnit
        clockUnit = MyApplication.VALUE_UNIT_OBJ.clockUnit
        bundle = savedInstanceState ?: arguments
        addressName = bundle!!.getString(BundleKey.AddressName.name)
        zoneId = bundle!!.getSerializable(BundleKey.TimeZone.name) as ZoneId?
        latitude = bundle!!.getDouble(BundleKey.Latitude.name)
        longitude = bundle!!.getDouble(BundleKey.Longitude.name)
        mainWeatherProviderType = bundle!!.getSerializable(
            BundleKey.WeatherProvider.name
        ) as WeatherProviderType?
    }


    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adRequest = AdRequest.Builder().build()
        binding!!.adViewBottom.loadAd(adRequest)
        setDataViewsByList()
    }

    override fun onDestroy() {
        childFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks)
        super.onDestroy()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(bundle)
    }

    protected abstract fun setDataViewsByList()
    protected abstract fun setDataViewsByTable()
    override fun onClickedItem(position: Int) {}
    class HourlyForecastListAdapter(private val onClickedForecastItem: OnClickedListViewItemListener<Int>?, showDataType: ShowDataType) :
        RecyclerView.Adapter<HourlyForecastListAdapter.ViewHolder>() {
        var hourlyForecastDtoList: List<HourlyForecastDto> = ArrayList()
        private val dateFormatter = DateTimeFormatter.ofPattern("M.d E")
        private val hourFormatter = DateTimeFormatter.ofPattern("H")
        private val tempDegree: String
        private val degree = "°"
        private val showDataType: ShowDataType

        enum class ShowDataType {
            Precipitation, Humidity, Wind
        }

        init {
            tempDegree = MyApplication.VALUE_UNIT_OBJ.tempUnitText
            setHasStableIds(true)
            this.showDataType = showDataType
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        fun setHourlyForecastDtoList(hourlyForecastDtoList: List<HourlyForecastDto>) {
            this.hourlyForecastDtoList = hourlyForecastDtoList
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ViewDetailHourlyForecastListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.onBind(hourlyForecastDtoList[position])
        }

        override fun onViewDetachedFromWindow(holder: ViewHolder) {
            super.onViewDetachedFromWindow(holder)
            Glide.with(holder.itemView.context).clear(holder.binding.weatherIcon)
        }

        override fun getItemCount(): Int {
            return hourlyForecastDtoList.size
        }

        inner class ViewHolder(val binding: ViewDetailHourlyForecastListBinding) : RecyclerView.ViewHolder(binding.root) {
            private val noData: String

            init {
                binding.root.setOnClickListener(View.OnClickListener {
                    onClickedForecastItem!!.onClickedItem(
                        bindingAdapterPosition
                    )
                })
                noData = binding.root.context.getString(R.string.noData)
            }

            fun onBind(hourlyForecastDto: HourlyForecastDto) {
                binding.date.text = hourlyForecastDto.hours.format(dateFormatter)
                binding.hours.text = hourlyForecastDto.hours.format(hourFormatter)
                Glide.with(binding.weatherIcon).load(hourlyForecastDto.weatherIcon)
                    .into(binding.weatherIcon)
                binding.temp.text = hourlyForecastDto.temp.replace(tempDegree, degree)
                binding.pop.text = if (hourlyForecastDto.pop == null) "-" else hourlyForecastDto.pop
                if (showDataType == ShowDataType.Precipitation) {
                    if (hourlyForecastDto.isHasSnow) {
                        binding.snowVolume.text = hourlyForecastDto.snowVolume
                        binding.snowVolumeLayout.visibility = View.VISIBLE
                    } else {
                        binding.snowVolumeLayout.visibility = View.GONE
                    }
                    if (hourlyForecastDto.isHasRain || hourlyForecastDto.isHasPrecipitation) {
                        binding.rainVolume.text =
                            if (hourlyForecastDto.isHasRain) hourlyForecastDto.rainVolume else hourlyForecastDto.precipitationVolume
                        binding.rainVolumeLayout.visibility = View.VISIBLE
                    } else {
                        binding.rainVolumeLayout.visibility = View.GONE
                    }
                    binding.topIcon.setImageResource(R.drawable.raindrop)
                    binding.topIcon.visibility = View.VISIBLE
                    binding.bottomIcon.setImageResource(R.drawable.snowparticle)
                    binding.bottomIcon.rotation = 0f
                } else if (showDataType == ShowDataType.Wind) {
                    binding.topIcon.visibility = View.GONE
                    binding.bottomIcon.setImageResource(R.drawable.arrow)
                    binding.bottomIcon.rotation = (hourlyForecastDto.windDirectionVal + 180).toFloat()
                    binding.rainVolume.text = if (hourlyForecastDto.windSpeed == null) noData else hourlyForecastDto.windSpeed
                    binding.snowVolume.text = if (hourlyForecastDto.windDirection == null) noData else hourlyForecastDto.windDirection
                    binding.rainVolumeLayout.visibility = View.VISIBLE
                    binding.snowVolumeLayout.visibility = View.VISIBLE
                } else if (showDataType == ShowDataType.Humidity) {
                    binding.rainVolumeLayout.visibility = View.VISIBLE
                    binding.snowVolumeLayout.visibility = View.GONE
                    binding.topIcon.visibility = View.VISIBLE
                    binding.topIcon.setImageResource(R.drawable.humidity)
                    binding.rainVolume.text = hourlyForecastDto.humidity
                }
            }
        }
    }

    class DailyForecastListAdapter(private val onClickedForecastItem: OnClickedListViewItemListener<Int>?) :
        RecyclerView.Adapter<DailyForecastListAdapter.ViewHolder>() {
        @JvmField var dailyForecastDtoList: List<DailyForecastDto> = ArrayList()
        private val tempDegree: String
        private val degree = "°"
        private var hasPrecipitationVolume = false
        private val dateFormatter = DateTimeFormatter.ofPattern("M.d")
        private val dayFormatter = DateTimeFormatter.ofPattern("E")

        init {
            setHasStableIds(true)
            tempDegree = MyApplication.VALUE_UNIT_OBJ.tempUnitText
        }

        fun setDailyForecastDtoList(dailyForecastDtoList: List<DailyForecastDto>, hasPrecipitationVolume: Boolean) {
            this.dailyForecastDtoList = dailyForecastDtoList
            this.hasPrecipitationVolume = hasPrecipitationVolume
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(ViewDetailDailyForecastListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(
            holder: ViewHolder,
            position: Int
        ) {
            holder.onBind(dailyForecastDtoList[position])
        }

        override fun onViewDetachedFromWindow(holder: ViewHolder) {
            super.onViewDetachedFromWindow(holder)
            val context = holder.binding.root.context.applicationContext
            Glide.with(context).clear(holder.binding.leftWeatherIcon)
            Glide.with(context).clear(holder.binding.rightWeatherIcon)
        }

        override fun getItemCount(): Int {
            return dailyForecastDtoList.size
        }

        inner class ViewHolder(val binding: ViewDetailDailyForecastListBinding) : RecyclerView.ViewHolder(binding.root) {
            init {
                binding.root.setOnClickListener({ v: View? -> onClickedForecastItem!!.onClickedItem(getBindingAdapterPosition()) })
            }

            fun onBind(daily: DailyForecastDto) {
                binding.volumeLayout.visibility = if (hasPrecipitationVolume) View.VISIBLE else View.GONE
                binding.date.text = daily.date.format(dateFormatter)
                binding.day.text = daily.date.format(dayFormatter)
                if (daily.valuesList.size == 1) {
                    binding.pop.text = daily.valuesList.get(0).pop
                    if (!daily.valuesList[0].isHasSnowVolume) {
                        binding.snowVolumeLayout.visibility = View.GONE
                    } else {
                        binding.snowVolume.text = daily.valuesList.get(0).snowVolume
                        binding.snowVolumeLayout.visibility = View.VISIBLE
                    }
                    if (!daily.valuesList[0].isHasRainVolume) {
                        binding.rainVolumeLayout.visibility = View.GONE
                    } else {
                        binding.rainVolume.text = daily.valuesList.get(0).rainVolume
                        binding.rainVolumeLayout.visibility = View.VISIBLE
                    }
                    binding.leftWeatherIcon.setImageResource(daily.valuesList[0].weatherIcon)
                    binding.rightWeatherIcon.visibility = View.GONE
                } else if (daily.valuesList.size == 2) {
                    binding.leftWeatherIcon.setImageResource(daily.valuesList[0].weatherIcon)
                    binding.rightWeatherIcon.setImageResource(daily.valuesList[1].weatherIcon)
                    binding.rightWeatherIcon.visibility = View.VISIBLE
                    val pop = daily.valuesList[0].pop + " / " + daily.valuesList[1].pop
                    binding.pop.text = pop
                    if (!daily.valuesList[0].isHasSnowVolume &&
                        !daily.valuesList[1].isHasSnowVolume
                    ) {
                        binding.snowVolumeLayout.visibility = View.GONE
                    } else {
                        val snow = (daily.valuesList[0].snowVolume + " / " +
                                daily.valuesList[1].snowVolume)
                        binding.snowVolume.text = snow
                        binding.snowVolumeLayout.visibility = View.VISIBLE
                    }
                    if (!daily.valuesList[0].isHasRainVolume &&
                        !daily.valuesList[1].isHasRainVolume
                    ) {
                        binding.rainVolumeLayout.visibility = View.GONE
                    } else {
                        val rain = (daily.valuesList[0].rainVolume + " / " +
                                daily.valuesList[1].rainVolume)
                        binding.rainVolume.text = rain
                        binding.rainVolumeLayout.visibility = View.VISIBLE
                    }
                } else if (daily.valuesList.size == 4) {
                    binding.leftWeatherIcon.setImageResource(daily.valuesList[1].weatherIcon)
                    binding.rightWeatherIcon.setImageResource(daily.valuesList[2].weatherIcon)
                    binding.rightWeatherIcon.visibility = View.VISIBLE
                    binding.pop.text = "-"
                    if ((daily.valuesList[0].isHasPrecipitationVolume || daily.valuesList[1].isHasPrecipitationVolume ||
                                daily.valuesList[2].isHasPrecipitationVolume || daily.valuesList[3].isHasPrecipitationVolume)
                    ) {
                        val mm = "mm"
                        val leftVol = (daily.valuesList[0].precipitationVolume.replace(mm, "")
                            .toFloat() + daily.valuesList[1].precipitationVolume.replace(mm, "").toFloat())
                        val rightVol = (daily.valuesList[2].precipitationVolume.replace(mm, "")
                            .toFloat() + daily.valuesList[3].precipitationVolume.replace(mm, "").toFloat())
                        val rain = (String.format(Locale.getDefault(), "%.1fmm", leftVol) + " / " + String.format(
                            Locale.getDefault(),
                            "%.1fmm",
                            rightVol
                        ))
                        binding.rainVolume.text = rain
                        binding.rainVolumeLayout.visibility = View.VISIBLE
                    } else {
                        binding.rainVolumeLayout.visibility = View.GONE
                    }
                    binding.snowVolumeLayout.visibility = View.GONE
                }
                binding.minTemp.text = daily.minTemp.replace(tempDegree, degree)
                binding.maxTemp.text = daily.maxTemp.replace(tempDegree, degree)
            }
        }
    }
}