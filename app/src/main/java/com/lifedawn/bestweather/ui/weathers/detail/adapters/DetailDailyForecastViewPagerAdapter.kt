package com.lifedawn.bestweather.ui.weathers.detail.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.ViewModelProvider.get
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.WeatherValueLabels
import com.lifedawn.bestweather.commons.constants.WeatherValueType
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto
import com.lifedawn.bestweather.databinding.ItemviewDetailDailyForecastBinding
import com.lifedawn.bestweather.ui.weathers.detail.dto.GridItemDto
import java.time.format.DateTimeFormatter
import java.util.*

class DetailDailyForecastViewPagerAdapter : RecyclerView.Adapter<DetailDailyForecastViewPagerAdapter.ViewHolder>() {
    private val dateFormatter = DateTimeFormatter.ofPattern("M.d E")
    private var dailyForecastDtoList: List<DailyForecastDto>? = null
    fun setDailyForecastDtoList(dailyForecastDtoList: List<DailyForecastDto>?) {
        this.dailyForecastDtoList = dailyForecastDtoList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemviewDetailDailyForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(dailyForecastDtoList!![position])
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.clear()
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int {
        return dailyForecastDtoList!!.size
    }

    protected inner class ViewHolder(private val binding: ItemviewDetailDailyForecastBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun clear() {
            binding.amList.removeAllViews()
            binding.precipitationGridLayout.removeAllViews()
        }

        fun onBind(dailyForecastDto: DailyForecastDto) {
            binding.date.text = dailyForecastDto.date.format(dateFormatter)
            binding.minTemp.text = dailyForecastDto.minTemp
            binding.maxTemp.text = dailyForecastDto.maxTemp
            val labelValueItemList: MutableList<LabelValueItem> = ArrayList()
            setPrecipitationGridItems(dailyForecastDto)

            //날씨 아이콘, 최저/최고 기온, 강수확률, 강수량, 강우량, 강설량 설정
            if (dailyForecastDto.valuesList.size == 1) {
                binding.timezone.setText(R.string.allDay)
                Glide.with(binding.leftIcon).load(dailyForecastDto.valuesList[0].weatherIcon).into(binding.leftIcon)
                binding.leftIcon.visibility = View.VISIBLE
                binding.rightIcon.visibility = View.GONE
                binding.weatherDescription.text = dailyForecastDto.valuesList[0].weatherDescription
                addListItem(
                    labelValueItemList,
                    WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windDirection),
                    dailyForecastDto.valuesList[0].windDirection
                )
                addListItem(
                    labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windSpeed),
                    dailyForecastDto.valuesList[0].windSpeed
                )
                addListItem(
                    labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windStrength),
                    dailyForecastDto.valuesList[0].windStrength
                )
                addListItem(
                    labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windGust),
                    dailyForecastDto.valuesList[0].windGust
                )
                addListItem(
                    labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.pressure),
                    dailyForecastDto.valuesList[0].pressure
                )
                addListItem(
                    labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.humidity),
                    dailyForecastDto.valuesList[0].humidity
                )
                addListItem(
                    labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.dewPoint),
                    dailyForecastDto.valuesList[0].dewPointTemp
                )
                addListItem(
                    labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.cloudiness),
                    dailyForecastDto.valuesList[0].cloudiness
                )
                addListItem(
                    labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.uvIndex),
                    dailyForecastDto.valuesList[0].uvIndex
                )
            } else if (dailyForecastDto.valuesList.size == 2) {
                binding.timezone.setText(
                    String(
                        binding.root.context.getString(R.string.am) + " / " +
                                binding.root.context.getString(R.string.pm)
                    )
                )
                binding.weatherDescription.setText(
                    String(
                        dailyForecastDto.valuesList[0].weatherDescription + " / " +
                                dailyForecastDto.valuesList[1].weatherDescription
                    )
                )
                Glide.with(binding.leftIcon).load(dailyForecastDto.valuesList[0].weatherIcon).into(binding.leftIcon)
                Glide.with(binding.rightIcon).load(dailyForecastDto.valuesList[1].weatherIcon).into(binding.rightIcon)
                binding.leftIcon.visibility = View.VISIBLE
                binding.rightIcon.visibility = View.VISIBLE
                addListItem(
                    labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windDirection),
                    dailyForecastDto.valuesList[0].windDirection, dailyForecastDto.valuesList[1].windDirection
                )
                addListItem(
                    labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windSpeed),
                    dailyForecastDto.valuesList[0].windSpeed, dailyForecastDto.valuesList[1].windSpeed
                )
                addListItem(
                    labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windStrength),
                    dailyForecastDto.valuesList[0].windStrength, dailyForecastDto.valuesList[1].windStrength
                )
                addListItem(
                    labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windGust),
                    dailyForecastDto.valuesList[0].windGust, dailyForecastDto.valuesList[1].windGust
                )
                addListItem(
                    labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.pressure),
                    dailyForecastDto.valuesList[0].pressure, dailyForecastDto.valuesList[1].pressure
                )
                addListItem(
                    labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.humidity),
                    dailyForecastDto.valuesList[0].humidity, dailyForecastDto.valuesList[1].humidity
                )
                addListItem(
                    labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.dewPoint),
                    dailyForecastDto.valuesList[0].dewPointTemp, dailyForecastDto.valuesList[1].dewPointTemp
                )
                addListItem(
                    labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.cloudiness),
                    dailyForecastDto.valuesList[0].cloudiness, dailyForecastDto.valuesList[1].cloudiness
                )
                addListItem(
                    labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.uvIndex),
                    dailyForecastDto.valuesList[0].uvIndex, dailyForecastDto.valuesList[1].uvIndex
                )
            } else if (dailyForecastDto.valuesList.size == 4) {
                // 강수량, 날씨 아이콘, 날씨 설명, 풍향, 풍속
                binding.timezone.setText(
                    String(
                        binding.root.context.getString(R.string.am) + " / " +
                                binding.root.context.getString(R.string.pm)
                    )
                )
                binding.weatherDescription.setText(
                    String(
                        dailyForecastDto.valuesList[1].weatherDescription + " / " +
                                dailyForecastDto.valuesList[2].weatherDescription
                    )
                )
                Glide.with(binding.leftIcon).load(dailyForecastDto.valuesList[1].weatherIcon).into(binding.leftIcon)
                Glide.with(binding.rightIcon).load(dailyForecastDto.valuesList[2].weatherIcon).into(binding.rightIcon)
                binding.leftIcon.visibility = View.VISIBLE
                binding.rightIcon.visibility = View.VISIBLE
                addListItem(
                    labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windDirection),
                    dailyForecastDto.valuesList[1].windDirection,
                    dailyForecastDto.valuesList[2].windDirection
                )
                addListItem(
                    labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windSpeed),
                    dailyForecastDto.valuesList[1].windSpeed, dailyForecastDto.valuesList[2].windSpeed
                )
                addListItem(
                    labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windStrength),
                    dailyForecastDto.valuesList[1].windStrength,
                    dailyForecastDto.valuesList[2].windStrength
                )
            }
            var layoutInflater = LayoutInflater.from(binding.root.context)
            for (labelValueItem in labelValueItemList) {
                val view = layoutInflater!!.inflate(R.layout.value_itemview, null, false)
                (view.findViewById<View>(R.id.label) as TextView).text = labelValueItem.label
                (view.findViewById<View>(R.id.value) as TextView).text = labelValueItem.value
                binding.amList.addView(view)
            }
            layoutInflater = null
        }

        protected fun addGridItems(gridItemDtos: List<GridItemDto>) {
            var label: TextView? = null
            var value: TextView? = null
            var convertView: View? = null
            val blueColor = ContextCompat.getColor(binding.root.context, R.color.blue)
            var layoutInflater = LayoutInflater.from(binding.root.context)
            for (gridItem in gridItemDtos) {
                convertView = layoutInflater!!.inflate(R.layout.view_detail_weather_data_item, null, false)
                label = convertView.findViewById(R.id.label)
                value = convertView.findViewById(R.id.value)
                convertView.findViewById<View>(R.id.label_icon).visibility = View.GONE
                label.text = gridItem.label
                value.text = gridItem.value
                value.setTextColor(blueColor)
                val cellCount = binding.precipitationGridLayout.childCount
                val row = cellCount / binding.precipitationGridLayout.columnCount
                val column = cellCount % binding.precipitationGridLayout.columnCount
                val layoutParams = GridLayout.LayoutParams()
                layoutParams.columnSpec = GridLayout.spec(column, GridLayout.FILL, 1f)
                layoutParams.rowSpec = GridLayout.spec(row, GridLayout.FILL, 1f)
                binding.precipitationGridLayout.addView(convertView, layoutParams)
            }
            layoutInflater = null
        }

        private fun setPrecipitationGridItems(dailyForecastDto: DailyForecastDto) {
            val gridItemDtoList: MutableList<GridItemDto> = ArrayList()
            if (dailyForecastDto.valuesList.size == 1) {
                val (_, _, _, pop, pos, por, precipitationVolume, rainVolume, snowVolume, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, isHasRainVolume, isHasSnowVolume, isHasPrecipitationVolume) = dailyForecastDto.valuesList[0]
                if (pop != null) {
                    gridItemDtoList.add(
                        GridItemDto(
                            WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.pop),
                            pop, null
                        )
                    )
                }
                if (por != null) {
                    gridItemDtoList.add(
                        GridItemDto(
                            WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.por),
                            por, null
                        )
                    )
                }
                if (pos != null) {
                    gridItemDtoList.add(
                        GridItemDto(
                            WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.pos),
                            pos, null
                        )
                    )
                }
                if (isHasPrecipitationVolume && precipitationVolume != null) {
                    gridItemDtoList.add(
                        GridItemDto(
                            WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.precipitationVolume),
                            precipitationVolume, null
                        )
                    )
                }
                if (isHasRainVolume) {
                    gridItemDtoList.add(
                        GridItemDto(
                            WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.rainVolume),
                            rainVolume, null
                        )
                    )
                }
                if (isHasSnowVolume) {
                    gridItemDtoList.add(
                        GridItemDto(
                            WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.snowVolume),
                            snowVolume, null
                        )
                    )
                }
            } else {
                val valuesSize = dailyForecastDto.valuesList.size
                val (_, _, _, pop, pos, por, _, rainVolume, snowVolume, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, isHasRainVolume, isHasSnowVolume, isHasPrecipitationVolume) = dailyForecastDto.valuesList[if (valuesSize == 2) 0 else 1]
                val (_, _, _, pop1, pos1, por1, _, rainVolume1, snowVolume1, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, isHasRainVolume1, isHasSnowVolume1, isHasPrecipitationVolume1) = dailyForecastDto.valuesList[if (valuesSize == 2) 1 else 2]
                val divider = " / "
                if (pop != null || pop1 != null) {
                    gridItemDtoList.add(
                        GridItemDto(
                            WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.pop),
                            pop + divider + pop1,
                            null
                        )
                    )
                }
                if (por != null || por1 != null) {
                    gridItemDtoList.add(
                        GridItemDto(
                            WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.por),
                            por + divider + por1,
                            null
                        )
                    )
                }
                if (pos != null || pos1 != null) {
                    gridItemDtoList.add(
                        GridItemDto(
                            WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.pos),
                            pos + divider + pos1, null
                        )
                    )
                }
                if (isHasPrecipitationVolume || isHasPrecipitationVolume1) {
                    val mm = "mm"
                    val amVolume = dailyForecastDto.valuesList[0].precipitationVolume.replace(mm, "")
                        .toFloat() + dailyForecastDto.valuesList[1].precipitationVolume.replace(mm, "").toFloat()
                    val pmVolume = dailyForecastDto.valuesList[2].precipitationVolume.replace(
                        mm,
                        ""
                    ).toFloat() + dailyForecastDto.valuesList[3].precipitationVolume.replace(mm, "").toFloat()
                    gridItemDtoList.add(
                        GridItemDto(
                            WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.precipitationVolume),
                            String.format(Locale.getDefault(), "%.1fmm", amVolume) + divider + String.format(
                                Locale.getDefault(),
                                "%.1fmm",
                                pmVolume
                            ),
                            null
                        )
                    )
                }
                if (isHasRainVolume || isHasRainVolume1) {
                    gridItemDtoList.add(
                        GridItemDto(
                            WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.rainVolume),
                            rainVolume + divider + rainVolume1, null
                        )
                    )
                }
                if (isHasSnowVolume || isHasSnowVolume1) {
                    gridItemDtoList.add(
                        GridItemDto(
                            WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.snowVolume),
                            snowVolume + divider + snowVolume1, null
                        )
                    )
                }
            }
            addGridItems(gridItemDtoList)
        }
    }

    protected fun addListItem(list: MutableList<LabelValueItem>, label: String?, `val`: String?) {
        if (`val` != null) {
            list.add(LabelValueItem(label, `val`))
        }
    }

    protected fun addListItem(list: MutableList<LabelValueItem>, label: String?, val1: String?, val2: String?) {
        if (val1 != null || val2 != null) {
            list.add(LabelValueItem(label, "$val1 / $val2"))
        }
    }

    protected class LabelValueItem(var label: String?, var value: String) {
        fun setLabel(label: String?): LabelValueItem {
            this.label = label
            return this
        }

        fun setValue(value: String): LabelValueItem {
            this.value = value
            return this
        }
    }
}