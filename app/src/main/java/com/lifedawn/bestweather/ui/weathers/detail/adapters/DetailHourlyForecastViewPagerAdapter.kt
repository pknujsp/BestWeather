package com.lifedawn.bestweather.ui.weathers.detail.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.ViewModelProvider.get
import androidx.recyclerview.widget.RecyclerView
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.WeatherValueLabels
import com.lifedawn.bestweather.commons.constants.ValueUnits
import com.lifedawn.bestweather.commons.constants.WeatherValueType
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto
import com.lifedawn.bestweather.databinding.HeaderviewDetailHourlyforecastBinding
import com.lifedawn.bestweather.databinding.ItemviewDetailForecastBinding
import com.lifedawn.bestweather.ui.weathers.detail.dto.GridItemDto
import java.time.format.DateTimeFormatter

class DetailHourlyForecastViewPagerAdapter : RecyclerView.Adapter<DetailHourlyForecastViewPagerAdapter.ViewHolder>() {
    private val dateFormatter = DateTimeFormatter.ofPattern("M.d E")
    private val hoursFormatter: DateTimeFormatter
    private var hourlyForecastDtoList: List<HourlyForecastDto>? = null

    init {
        val clockUnit = MyApplication.VALUE_UNIT_OBJ.clockUnit
        hoursFormatter = DateTimeFormatter.ofPattern(if (clockUnit === ValueUnits.clock12) "h a" else "H")
    }

    fun setHourlyForecastDtoList(hourlyForecastDtoList: List<HourlyForecastDto>?): DetailHourlyForecastViewPagerAdapter {
        this.hourlyForecastDtoList = hourlyForecastDtoList
        return this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            ItemviewDetailForecastBinding.inflate(layoutInflater, parent, false),
            HeaderviewDetailHourlyforecastBinding.inflate(layoutInflater, null, false)
        )
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.clear()
        super.onViewRecycled(holder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(hourlyForecastDtoList!![position])
    }

    override fun getItemCount(): Int {
        return hourlyForecastDtoList!!.size
    }

    protected inner class ViewHolder(
        private val binding: ItemviewDetailForecastBinding,
        private val headerBinding: HeaderviewDetailHourlyforecastBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {
        private val feelsLikeTempLabel: String
        private val noData: String

        init {
            binding.header.addView(headerBinding.root)
            feelsLikeTempLabel = binding.root.context.getString(R.string.feelsLike) + ": "
            noData = binding.root.context.getString(R.string.noData)
        }

        fun clear() {
            headerBinding.precipitationGridLayout.removeAllViews()
            binding.detailGridView.removeAllViews()
        }

        fun onBind(hourlyForecastDto: HourlyForecastDto) {
            //header 화면 구성
            if (hourlyForecastDto.isHasNext6HoursPrecipitation) {
                var dateTime = hourlyForecastDto.hours
                var date = dateTime.format(dateFormatter)
                var time = dateTime.format(hoursFormatter)
                dateTime = dateTime.plusHours(6)
                date += " - " + dateTime.format(dateFormatter)
                time += " - " + dateTime.format(hoursFormatter)
                headerBinding.date.text = date
                headerBinding.hours.text = time
            } else {
                headerBinding.date.text = hourlyForecastDto.hours.format(dateFormatter)
                headerBinding.hours.text = hourlyForecastDto.hours.format(hoursFormatter)
            }
            headerBinding.weatherIcon.setImageResource(hourlyForecastDto.weatherIcon)
            headerBinding.temp.text = hourlyForecastDto.temp
            headerBinding.feelsLikeTemp.setText(String(feelsLikeTempLabel + hourlyForecastDto.feelsLikeTemp))
            headerBinding.weatherDescription.text = hourlyForecastDto.weatherDescription
            addPrecipitationGridItem(LayoutInflater.from(binding.root.context), hourlyForecastDto)

            //gridviewLayout
            //공통 - 날씨, 기온, 강수량, 강수확률, 풍향, 풍속, 바람세기, 습도
            val gridItemDtos: MutableList<GridItemDto> = ArrayList()
            if (hourlyForecastDto.precipitationType != null) {
                gridItemDtos.add(
                    GridItemDto(
                        WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.precipitationType),
                        hourlyForecastDto.precipitationType,
                        ContextCompat.getDrawable(binding.root.context, hourlyForecastDto.precipitationTypeIcon)
                    )
                )
            }
            gridItemDtos.add(
                GridItemDto(
                    WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windDirection),
                    hourlyForecastDto.windDirection ?: noData,
                    ContextCompat.getDrawable(binding.root.context, R.drawable.arrow), hourlyForecastDto.windDirectionVal + 180
                )
            )
            gridItemDtos.add(
                GridItemDto(
                    WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windSpeed),
                    hourlyForecastDto.windSpeed ?: noData, null
                )
            )
            gridItemDtos.add(
                GridItemDto(
                    WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windStrength),
                    hourlyForecastDto.windStrength ?: noData, null
                )
            )
            if (hourlyForecastDto.windGust != null) {
                gridItemDtos.add(
                    GridItemDto(
                        WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windGust),
                        hourlyForecastDto.windGust, null
                    )
                )
            }
            if (hourlyForecastDto.pressure != null) {
                gridItemDtos.add(
                    GridItemDto(
                        WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.pressure),
                        hourlyForecastDto.pressure, null
                    )
                )
            }
            if (hourlyForecastDto.humidity != null) {
                gridItemDtos.add(
                    GridItemDto(
                        WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.humidity),
                        hourlyForecastDto.humidity, null
                    )
                )
            }

            //나머지 - 돌풍, 기압, 이슬점, 운량, 시정, 자외선, 체감기온
            if (hourlyForecastDto.dewPointTemp != null) {
                gridItemDtos.add(
                    GridItemDto(
                        WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.dewPoint),
                        hourlyForecastDto.dewPointTemp, null
                    )
                )
            }
            if (hourlyForecastDto.cloudiness != null) {
                gridItemDtos.add(
                    GridItemDto(
                        WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.cloudiness),
                        hourlyForecastDto.cloudiness, null
                    )
                )
            }
            if (hourlyForecastDto.visibility != null) {
                gridItemDtos.add(
                    GridItemDto(
                        WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.visibility),
                        hourlyForecastDto.visibility, null
                    )
                )
            }
            if (hourlyForecastDto.uvIndex != null) {
                gridItemDtos.add(
                    GridItemDto(
                        WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.uvIndex),
                        hourlyForecastDto.uvIndex, null
                    )
                )
            }
            addGridItems(gridItemDtos)
        }

        private fun addPrecipitationGridItem(layoutInflater: LayoutInflater, hourlyForecastDto: HourlyForecastDto) {
            var gridItem = layoutInflater.inflate(R.layout.view_detail_weather_data_item, null)
            val blueColor = ContextCompat.getColor(binding.root.context, R.color.blue)

            //강수확률
            (gridItem.findViewById<View>(R.id.label) as TextView).setText(
                WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.pop)
            )
            (gridItem.findViewById<View>(R.id.value) as TextView).text =
                hourlyForecastDto.pop ?: "-"
            (gridItem.findViewById<View>(R.id.value) as TextView).setTextColor(blueColor)
            gridItem.findViewById<View>(R.id.label_icon).visibility = View.GONE
            headerBinding.precipitationGridLayout.addView(gridItem)

            //강우확률
            if (hourlyForecastDto.isHasPor) {
                gridItem = layoutInflater.inflate(R.layout.view_detail_weather_data_item, null)
                (gridItem.findViewById<View>(R.id.label) as TextView).setText(
                    WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.por)
                )
                (gridItem.findViewById<View>(R.id.value) as TextView).text = hourlyForecastDto.por
                (gridItem.findViewById<View>(R.id.value) as TextView).setTextColor(blueColor)
                gridItem.findViewById<View>(R.id.label_icon).visibility = View.GONE
                headerBinding.precipitationGridLayout.addView(gridItem)
            }

            //강설확률
            if (hourlyForecastDto.isHasPos) {
                gridItem = layoutInflater.inflate(R.layout.view_detail_weather_data_item, null)
                (gridItem.findViewById<View>(R.id.label) as TextView).setText(
                    WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.pos)
                )
                (gridItem.findViewById<View>(R.id.value) as TextView).text = hourlyForecastDto.pos
                (gridItem.findViewById<View>(R.id.value) as TextView).setTextColor(blueColor)
                gridItem.findViewById<View>(R.id.label_icon).visibility = View.GONE
                headerBinding.precipitationGridLayout.addView(gridItem)
            }

            //강수량
            if (hourlyForecastDto.isHasPrecipitation && hourlyForecastDto.precipitationVolume != null) {
                gridItem = layoutInflater.inflate(R.layout.view_detail_weather_data_item, null)
                (gridItem.findViewById<View>(R.id.label) as TextView).setText(
                    WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.precipitationVolume)
                )
                (gridItem.findViewById<View>(R.id.value) as TextView).text = hourlyForecastDto.precipitationVolume
                (gridItem.findViewById<View>(R.id.value) as TextView).setTextColor(blueColor)
                gridItem.findViewById<View>(R.id.label_icon).visibility = View.GONE
                headerBinding.precipitationGridLayout.addView(gridItem)
            }

            //강우량
            if (hourlyForecastDto.isHasRain) {
                gridItem = layoutInflater.inflate(R.layout.view_detail_weather_data_item, null)
                (gridItem.findViewById<View>(R.id.label) as TextView).setText(
                    WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.rainVolume)
                )
                (gridItem.findViewById<View>(R.id.value) as TextView).text =
                    hourlyForecastDto.rainVolume
                (gridItem.findViewById<View>(R.id.value) as TextView).setTextColor(blueColor)
                gridItem.findViewById<View>(R.id.label_icon).visibility = View.GONE
                headerBinding.precipitationGridLayout.addView(gridItem)
            }

            //강설량
            if (hourlyForecastDto.isHasSnow) {
                gridItem = layoutInflater.inflate(R.layout.view_detail_weather_data_item, null)
                (gridItem.findViewById<View>(R.id.label) as TextView).setText(
                    WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.snowVolume)
                )
                (gridItem.findViewById<View>(R.id.value) as TextView).text = hourlyForecastDto.snowVolume
                (gridItem.findViewById<View>(R.id.value) as TextView).setTextColor(blueColor)
                gridItem.findViewById<View>(R.id.label_icon).visibility = View.GONE
                headerBinding.precipitationGridLayout.addView(gridItem)
            }
        }

        private fun addGridItems(gridItemDtos: List<GridItemDto>) {
            var label: TextView? = null
            var value: TextView? = null
            var icon: ImageView? = null
            var convertView: View? = null
            var layoutInflater = LayoutInflater.from(binding.root.context)
            for (gridItem in gridItemDtos) {
                convertView = layoutInflater!!.inflate(R.layout.view_detail_weather_data_item, null, false)
                label = convertView.findViewById(R.id.label)
                value = convertView.findViewById(R.id.value)
                icon = convertView.findViewById(R.id.label_icon)
                label.text = gridItem.label
                value.text = gridItem.value
                if (gridItem.img != null) {
                    icon.setImageDrawable(gridItem.img)
                    if (gridItem.imgRotate != null) {
                        icon.rotation = gridItem.imgRotate!!.toFloat()
                    }
                } else {
                    icon.visibility = View.GONE
                }
                val cellCount = binding.detailGridView.childCount
                val row = cellCount / 3
                val column = cellCount % 3
                val layoutParams = GridLayout.LayoutParams()
                layoutParams.columnSpec = GridLayout.spec(column, GridLayout.FILL, 1f)
                layoutParams.rowSpec = GridLayout.spec(row, GridLayout.FILL, 1f)
                binding.detailGridView.addView(convertView, layoutParams)
            }
            layoutInflater = null
        }
    }
}