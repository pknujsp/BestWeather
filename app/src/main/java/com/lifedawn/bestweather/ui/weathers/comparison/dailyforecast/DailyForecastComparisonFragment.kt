package com.lifedawn.bestweather.ui.weathers.comparison.dailyforecast

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.ArrayMap
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma
import com.lifedawn.bestweather.commons.views.ProgressDialog
import com.lifedawn.bestweather.ui.weathers.customview.DoubleWeatherIconView
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class DailyForecastComparisonFragment : BaseForecastComparisonFragment() {
    private var multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback? = null
    private var dailyForecastResponse: DailyForecastResponse? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        ProgressDialog.show(requireActivity(), getString(R.string.msg_refreshing_weather_data)) { v: View? ->
            getParentFragmentManager().popBackStack()
            if (multipleWeatherRestApiCallback != null) {
                multipleWeatherRestApiCallback.cancel()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rootScrollView.setVisibility(View.GONE)
        binding.toolbar.fragmentTitle.setText(R.string.comparison_daily_forecast)
        binding.addressName.setText(addressName)
        loadForecasts()
    }

    override fun onStart() {
        super.onStart()
    }

    @SuppressLint("DefaultLocale")
    private fun setValues(dailyForecastResponse: DailyForecastResponse) {
        val weatherValueRowHeight: Int = getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC).toInt()
        val context: Context = requireContext().getApplicationContext()
        val weatherProviderTypeList: MutableList<WeatherProviderType> = ArrayList<WeatherProviderType>()
        var kmaFinalDailyForecasts: MutableList<ForecastObj<DailyForecastDto?>>? = null
        var accuFinalDailyForecasts: MutableList<ForecastObj<DailyForecastDto?>>? = null
        var owmFinalDailyForecasts: MutableList<ForecastObj<DailyForecastDto?>>? = null
        var metNorwayFinalDailyForecasts: MutableList<ForecastObj<DailyForecastDto?>>? = null
        if (dailyForecastResponse.kmaDailyForecastList != null) {
            val dailyForecastDtoList: List<DailyForecastDto>? = dailyForecastResponse.kmaDailyForecastList
            kmaFinalDailyForecasts = ArrayList<ForecastObj<DailyForecastDto?>>()
            for (finalDailyForecast in dailyForecastDtoList) {
                kmaFinalDailyForecasts!!.add(ForecastObj<DailyForecastDto>(finalDailyForecast.date, finalDailyForecast))
            }
            weatherProviderTypeList.add(WeatherProviderType.KMA_WEB)
            binding.kma.setVisibility(View.VISIBLE)
        } else {
            binding.kma.setVisibility(View.GONE)
        }
        if (dailyForecastResponse.accuDailyForecastList != null) {
            val dailyForecastDtoList: List<DailyForecastDto>? = dailyForecastResponse.accuDailyForecastList
            accuFinalDailyForecasts = ArrayList<ForecastObj<DailyForecastDto?>>()
            for (item in dailyForecastDtoList) {
                accuFinalDailyForecasts!!.add(
                    ForecastObj<DailyForecastDto>(
                        item.date, item
                    )
                )
            }
            weatherProviderTypeList.add(WeatherProviderType.ACCU_WEATHER)
            binding.accu.setVisibility(View.VISIBLE)
        } else {
            binding.accu.setVisibility(View.GONE)
        }
        if (dailyForecastResponse.owmDailyForecastList != null) {
            val dailyForecastDtoList: List<DailyForecastDto>? = dailyForecastResponse.owmDailyForecastList
            owmFinalDailyForecasts = ArrayList<ForecastObj<DailyForecastDto?>>()
            for (daily in dailyForecastDtoList) {
                owmFinalDailyForecasts!!.add(ForecastObj<DailyForecastDto>(daily.date, daily))
            }
            weatherProviderTypeList.add(WeatherProviderType.OWM_ONECALL)
            binding.owm.setVisibility(View.VISIBLE)
        } else {
            binding.owm.setVisibility(View.GONE)
        }
        var idx_MetNorway_unavailableToMakeMinMaxTemp = 0
        if (dailyForecastResponse.metNorwayDailyForecastList != null) {
            val dailyForecastDtoList: List<DailyForecastDto>? = dailyForecastResponse.metNorwayDailyForecastList
            metNorwayFinalDailyForecasts = ArrayList<ForecastObj<DailyForecastDto?>>()
            for (daily in dailyForecastDtoList) {
                if (!daily.isAvailable_toMakeMinMaxTemp) break
                metNorwayFinalDailyForecasts!!.add(ForecastObj<DailyForecastDto>(daily.date, daily))
                idx_MetNorway_unavailableToMakeMinMaxTemp++
            }
            weatherProviderTypeList.add(WeatherProviderType.MET_NORWAY)
            binding.metNorway.setVisibility(View.VISIBLE)
        } else {
            binding.metNorway.setVisibility(View.GONE)
        }
        var now: ZonedDateTime = ZonedDateTime.now(zoneId).plusDays(10).withHour(0).withMinute(0).withSecond(0).withNano(0)
        var firstDateTime = ZonedDateTime.of(now.toLocalDateTime(), zoneId)
        now = now.minusDays(20)
        var lastDateTime = ZonedDateTime.of(now.toLocalDateTime(), zoneId)
        if (kmaFinalDailyForecasts != null) {
            if (kmaFinalDailyForecasts[0].dateTime.isBefore(firstDateTime)) {
                firstDateTime = ZonedDateTime.of(kmaFinalDailyForecasts[0].dateTime.toLocalDateTime(), zoneId)
            }
            if (kmaFinalDailyForecasts[kmaFinalDailyForecasts.size - 1].dateTime.isAfter(lastDateTime)) {
                lastDateTime = ZonedDateTime.of(kmaFinalDailyForecasts[kmaFinalDailyForecasts.size - 1].dateTime.toLocalDateTime(), zoneId)
            }
        }
        if (accuFinalDailyForecasts != null) {
            if (accuFinalDailyForecasts[0].dateTime.isBefore(firstDateTime)) {
                firstDateTime = ZonedDateTime.of(accuFinalDailyForecasts[0].dateTime.toLocalDateTime(), zoneId)
            }
            if (accuFinalDailyForecasts[accuFinalDailyForecasts.size - 1].dateTime.isAfter(lastDateTime)) {
                lastDateTime =
                    ZonedDateTime.of(accuFinalDailyForecasts[accuFinalDailyForecasts.size - 1].dateTime.toLocalDateTime(), zoneId)
            }
        }
        if (owmFinalDailyForecasts != null) {
            if (owmFinalDailyForecasts[0].dateTime.isBefore(firstDateTime)) {
                firstDateTime = ZonedDateTime.of(owmFinalDailyForecasts[0].dateTime.toLocalDateTime(), zoneId)
            }
            if (owmFinalDailyForecasts[owmFinalDailyForecasts.size - 1].dateTime.isAfter(lastDateTime)) {
                lastDateTime = ZonedDateTime.of(
                    owmFinalDailyForecasts[owmFinalDailyForecasts.size - 1].dateTime.toLocalDateTime(),
                    zoneId
                )
            }
        }
        if (metNorwayFinalDailyForecasts != null) {
            if (metNorwayFinalDailyForecasts[0].dateTime.isBefore(firstDateTime)) {
                firstDateTime = ZonedDateTime.of(metNorwayFinalDailyForecasts[0].dateTime.toLocalDateTime(), zoneId)
            }
            if (metNorwayFinalDailyForecasts[idx_MetNorway_unavailableToMakeMinMaxTemp - 1].dateTime.isAfter(lastDateTime)) {
                lastDateTime = ZonedDateTime.of(
                    metNorwayFinalDailyForecasts[idx_MetNorway_unavailableToMakeMinMaxTemp - 1].dateTime.toLocalDateTime(),
                    zoneId
                )
            }
        }
        val dateTimeList: MutableList<ZonedDateTime> = ArrayList()

        //firstDateTime부터 lastDateTime까지 추가
        now = ZonedDateTime.of(firstDateTime.toLocalDateTime(), zoneId)
        while (!now.isAfter(lastDateTime)) {
            dateTimeList.add(now)
            now = now.plusDays(1)
        }
        val columnsCount = dateTimeList.size
        val columnWidth: Int = getResources().getDimension(R.dimen.valueColumnWidthInSCDaily).toInt()
        val valueRowWidth = columnWidth * columnsCount

        //날짜, 날씨, 기온, 강수량, 강수확률
        val dateRow = TextsView(context, valueRowWidth, columnWidth, null)
        val weatherIconRows: MutableList<DoubleWeatherIconView> = ArrayList<DoubleWeatherIconView>()
        val rainVolumeRows: MutableList<IconTextView> = ArrayList<IconTextView>()
        val snowVolumeRows: MutableList<IconTextView> = ArrayList<IconTextView>()
        val probabilityOfPrecipitationRows: MutableList<IconTextView> = ArrayList<IconTextView>()
        val tempRows: MutableList<TextsView> = ArrayList<TextsView>()
        for (i in weatherProviderTypeList.indices) {
            var specificRowWidth = 0
            var beginColumnIndex = 0
            if (weatherProviderTypeList[i] === WeatherProviderType.KMA_WEB) {
                specificRowWidth = kmaFinalDailyForecasts!!.size * columnWidth
                for (idx in dateTimeList.indices) {
                    if (dateTimeList[idx] == kmaFinalDailyForecasts[0].dateTime) {
                        beginColumnIndex = idx
                        break
                    }
                }
            } else if (weatherProviderTypeList[i] === WeatherProviderType.ACCU_WEATHER) {
                specificRowWidth = accuFinalDailyForecasts!!.size * columnWidth
                for (idx in dateTimeList.indices) {
                    if (dateTimeList[idx] == accuFinalDailyForecasts[0].dateTime) {
                        beginColumnIndex = idx
                        break
                    }
                }
            } else if (weatherProviderTypeList[i] === WeatherProviderType.OWM_ONECALL) {
                specificRowWidth = owmFinalDailyForecasts!!.size * columnWidth
                for (idx in dateTimeList.indices) {
                    if (dateTimeList[idx] == owmFinalDailyForecasts[0].dateTime) {
                        beginColumnIndex = idx
                        break
                    }
                }
            } else if (weatherProviderTypeList[i] === WeatherProviderType.MET_NORWAY) {
                specificRowWidth = idx_MetNorway_unavailableToMakeMinMaxTemp * columnWidth
                for (idx in dateTimeList.indices) {
                    if (dateTimeList[idx] == metNorwayFinalDailyForecasts!![0].dateTime) {
                        beginColumnIndex = idx
                        break
                    }
                }
            }
            weatherIconRows.add(
                DoubleWeatherIconView(context, FragmentType.Comparison, specificRowWidth, weatherValueRowHeight, columnWidth)
            )
            weatherIconRows[i].setTag(R.id.begin_column_index, beginColumnIndex)
            tempRows.add(TextsView(context, specificRowWidth, columnWidth, null))
            tempRows[i].setTag(R.id.begin_column_index, beginColumnIndex)
            rainVolumeRows.add(
                IconTextView(context, FragmentType.Comparison, specificRowWidth, columnWidth, R.drawable.raindrop)
            )
            rainVolumeRows[i].setTag(R.id.begin_column_index, beginColumnIndex)
            snowVolumeRows.add(
                IconTextView(context, FragmentType.Comparison, specificRowWidth, columnWidth, R.drawable.snowparticle)
            )
            snowVolumeRows[i].setTag(R.id.begin_column_index, beginColumnIndex)
            probabilityOfPrecipitationRows.add(
                IconTextView(context, FragmentType.Comparison, specificRowWidth, columnWidth, R.drawable.pop)
            )
            probabilityOfPrecipitationRows[i].setTag(R.id.begin_column_index, beginColumnIndex)
        }

        //날짜
        val dateList: MutableList<String> = ArrayList()
        val dateTimeFormatter = DateTimeFormatter.ofPattern("M.d\nE")
        for (date in dateTimeList) {
            dateList.add(date.format(dateTimeFormatter))
        }
        dateRow.setValueList(dateList)
        dateRow.setValueTextColor(Color.BLACK)

        //날씨,기온,강수량,강수확률
        //kma, accu weather, owm 순서
        var temp: String? = null
        var pop: String? = null
        val cm = "cm"
        val mm = "mm"
        val degree = "°"
        val weatherSourceUnitObjList: MutableList<WeatherSourceUnitObj> = ArrayList<WeatherSourceUnitObj>()
        for (i in weatherProviderTypeList.indices) {
            val weatherIconObjList: MutableList<DoubleWeatherIconView.WeatherIconObj> = ArrayList()
            val tempList: MutableList<String?> = ArrayList()
            val probabilityOfPrecipitationList: MutableList<String?> = ArrayList()
            val rainVolumeList: MutableList<String> = ArrayList()
            val snowVolumeList: MutableList<String> = ArrayList()
            var haveSnow = false
            var haveRain = false
            if (weatherProviderTypeList[i] === WeatherProviderType.KMA_WEB) {
                for (item in kmaFinalDailyForecasts) {
                    temp = item.e.minTemp.replace(tempUnitText, degree) + " / " + item.e.maxTemp.replace(tempUnitText, degree)
                    tempList.add(temp)
                    if (item.e.valuesList.size == 1) {
                        pop = item.e.valuesList.get(0).pop
                        weatherIconObjList.add(
                            DoubleWeatherIconView.WeatherIconObj(
                                ContextCompat.getDrawable(context, item.e.valuesList.get(0).weatherIcon),
                                item.e.valuesList.get(0).weatherDescription
                            )
                        )
                    } else {
                        pop = item.e.valuesList.get(0).pop + " / " + item.e.valuesList.get(1).pop
                        weatherIconObjList.add(
                            DoubleWeatherIconView.WeatherIconObj(
                                ContextCompat.getDrawable(context, item.e.valuesList.get(0).weatherIcon),
                                ContextCompat.getDrawable(context, item.e.valuesList.get(1).weatherIcon),
                                item.e.valuesList.get(0).weatherDescription,
                                item.e.valuesList.get(1).weatherDescription
                            )
                        )
                    }
                    probabilityOfPrecipitationList.add(pop)
                }
            } else if (weatherProviderTypeList[i] === WeatherProviderType.ACCU_WEATHER) {
                for (item in accuFinalDailyForecasts) {
                    temp = item.e.minTemp.replace(tempUnitText, degree) + " / " + item.e.maxTemp.replace(tempUnitText, degree)
                    tempList.add(temp)
                    pop = item.e.valuesList.get(0).pop + " / " + item.e.valuesList.get(1).pop
                    probabilityOfPrecipitationList.add(pop)
                    rainVolumeList.add(
                        String.format(
                            "%.2f",
                            item.e.valuesList.get(0).rainVolume.replace(mm, "").toFloat() + item.e.valuesList.get(1).rainVolume.replace(
                                mm,
                                ""
                            ).toFloat()
                        )
                    )
                    snowVolumeList.add(
                        String.format(
                            "%.2f",
                            item.e.valuesList.get(0).snowVolume.replace(cm, "").toFloat() + item.e.valuesList.get(1).snowVolume.replace(
                                cm,
                                ""
                            ).toFloat()
                        )
                    )
                    if (!haveSnow) {
                        if (item.e.valuesList.get(0).isHasSnowVolume ||
                            item.e.valuesList.get(1).isHasSnowVolume
                        ) {
                            haveSnow = true
                        }
                    }
                    if (!haveRain) {
                        if (item.e.valuesList.get(0).isHasRainVolume ||
                            item.e.valuesList.get(1).isHasRainVolume
                        ) {
                            haveRain = true
                        }
                    }
                    weatherIconObjList.add(
                        DoubleWeatherIconView.WeatherIconObj(
                            ContextCompat.getDrawable(context, item.e.valuesList.get(0).weatherIcon),
                            ContextCompat.getDrawable(context, item.e.valuesList.get(1).weatherIcon),
                            item.e.valuesList.get(0).weatherDescription,
                            item.e.valuesList.get(1).weatherDescription
                        )
                    )
                }
            } else if (weatherProviderTypeList[i] === WeatherProviderType.OWM_ONECALL) {
                for (item in owmFinalDailyForecasts) {
                    temp = item.e.minTemp.replace(tempUnitText, degree) + " / " + item.e.maxTemp.replace(tempUnitText, degree)
                    tempList.add(temp)
                    pop = item.e.valuesList.get(0).pop
                    probabilityOfPrecipitationList.add(pop)
                    if (item.e.valuesList.get(0).isHasSnowVolume) {
                        if (!haveSnow) {
                            haveSnow = true
                        }
                    }
                    if (item.e.valuesList.get(0).isHasRainVolume) {
                        if (!haveRain) {
                            haveRain = true
                        }
                    }
                    snowVolumeList.add(item.e.valuesList.get(0).snowVolume.replace(mm, ""))
                    rainVolumeList.add(item.e.valuesList.get(0).rainVolume.replace(mm, ""))
                    weatherIconObjList.add(
                        DoubleWeatherIconView.WeatherIconObj(
                            ContextCompat.getDrawable(context, item.e.valuesList.get(0).weatherIcon),
                            item.e.valuesList.get(0).weatherDescription
                        )
                    )
                }
            } else if (weatherProviderTypeList[i] === WeatherProviderType.MET_NORWAY) {
                var idx = 0
                var precipitationVolume = 0f
                for (item in metNorwayFinalDailyForecasts) {
                    temp = item.e.minTemp.replace(tempUnitText, degree) + " / " + item.e.maxTemp.replace(tempUnitText, degree)
                    tempList.add(temp)
                    probabilityOfPrecipitationList.add("-")
                    if (item.e.valuesList.get(0).isHasPrecipitationVolume || item.e.valuesList.get(1).isHasPrecipitationVolume ||
                        item.e.valuesList.get(2).isHasPrecipitationVolume || item.e.valuesList.get(3).isHasPrecipitationVolume
                    ) {
                        if (!haveRain) {
                            haveRain = true
                        }
                    }
                    precipitationVolume = item.e.valuesList.get(0).precipitationVolume.replace(mm, "")
                        .toFloat() + item.e.valuesList.get(1).precipitationVolume.replace(mm, "")
                        .toFloat() + item.e.valuesList.get(2).precipitationVolume.replace(mm, "")
                        .toFloat() + item.e.valuesList.get(3).precipitationVolume.replace(mm, "").toFloat()
                    rainVolumeList.add(String.format(Locale.getDefault(), "%.1f", precipitationVolume))
                    weatherIconObjList.add(
                        DoubleWeatherIconView.WeatherIconObj(
                            ContextCompat.getDrawable(context, item.e.valuesList.get(1).weatherIcon),
                            ContextCompat.getDrawable(context, item.e.valuesList.get(2).weatherIcon),
                            item.e.valuesList.get(1).weatherDescription,
                            item.e.valuesList.get(2).weatherDescription
                        )
                    )
                    if (++idx == idx_MetNorway_unavailableToMakeMinMaxTemp) break
                }
            }
            weatherSourceUnitObjList.add(WeatherSourceUnitObj(weatherProviderTypeList[i], haveRain, haveSnow))
            weatherIconRows[i].setIcons(weatherIconObjList)
            tempRows[i].setValueList(tempList)
            probabilityOfPrecipitationRows[i].setValueList(probabilityOfPrecipitationList)
            rainVolumeRows[i].setValueList(rainVolumeList)
            if (haveSnow) {
                snowVolumeRows[i].setValueList(snowVolumeList)
            }
        }
        val rowLayoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        binding.datetime.addView(dateRow, rowLayoutParams)
        var view: LinearLayout? = null
        notScrolledViews = arrayOfNulls<NotScrolledView>(weatherProviderTypeList.size)
        val nonScrollRowLayoutParams = LinearLayout.LayoutParams(
            valueRowWidth,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        nonScrollRowLayoutParams.gravity = Gravity.CENTER_VERTICAL
        val nonScrollViewMargin: Int = getResources().getDimension(R.dimen.nonScrollViewTopBottomMargin).toInt()
        nonScrollRowLayoutParams.setMargins(nonScrollViewMargin, nonScrollViewMargin, 0, nonScrollViewMargin)
        val tempRowTopMargin: Int = getResources().getDimension(R.dimen.tempTopMargin).toInt()
        for (i in weatherProviderTypeList.indices) {
            val specificRowLayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            specificRowLayoutParams.leftMargin = columnWidth * weatherIconRows[i].getTag(R.id.begin_column_index) as Int
            val tempRowLayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            tempRowLayoutParams.leftMargin = columnWidth * weatherIconRows[i].getTag(R.id.begin_column_index) as Int
            tempRowLayoutParams.topMargin = tempRowTopMargin
            val iconTextRowLayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            iconTextRowLayoutParams.leftMargin = columnWidth * weatherIconRows[i].getTag(R.id.begin_column_index) as Int
            var sourceName: String?
            var logoId: Int
            when (weatherProviderTypeList[i]) {
                WeatherProviderType.KMA_WEB -> {
                    view = binding.kma
                    sourceName = getString(R.string.kma)
                    logoId = R.drawable.kmaicon
                }
                ACCU_WEATHER -> {
                    view = binding.accu
                    sourceName = getString(R.string.accu_weather)
                    logoId = R.drawable.accuicon
                }
                WeatherProviderType.OWM_ONECALL -> {
                    view = binding.owm
                    sourceName = getString(R.string.owm)
                    logoId = R.drawable.owmicon
                }
                else -> {
                    view = binding.metNorway
                    sourceName = getString(R.string.met)
                    logoId = R.drawable.metlogo
                }
            }
            notScrolledViews.get(i) = NotScrolledView(context)
            notScrolledViews.get(i).setImg(logoId)
            notScrolledViews.get(i).setText(sourceName)
            view.addView(notScrolledViews.get(i), nonScrollRowLayoutParams)
            view.addView(weatherIconRows[i], specificRowLayoutParams)
            view.addView(probabilityOfPrecipitationRows[i], iconTextRowLayoutParams)
            if (weatherProviderTypeList[i] !== WeatherProviderType.KMA_WEB) {
                view.addView(rainVolumeRows[i], iconTextRowLayoutParams)
                if (snowVolumeRows[i].getValueList() != null) {
                    view.addView(snowVolumeRows[i], iconTextRowLayoutParams)
                }
            }
            tempRows[i].setValueTextSize(17)
            tempRows[i].setValueTextColor(Color.BLACK)
            view.addView(tempRows[i], tempRowLayoutParams)
        }
        dateTimeList.clear()
        customViewList.addAll(weatherIconRows)
        customViewList.addAll(probabilityOfPrecipitationRows)
        customViewList.addAll(rainVolumeRows)
        customViewList.addAll(snowVolumeRows)
        customViewList.addAll(tempRows)
        customViewList.add(dateRow)
        customViewList.addAll(Arrays.asList<NotScrolledView>(*notScrolledViews))
        createValueUnitsDescription(weatherSourceUnitObjList)
    }

    private fun loadForecasts() {
        val request: ArrayMap<WeatherProviderType, RequestWeatherSource> = ArrayMap<WeatherProviderType, RequestWeatherSource>()

        //RequestAccu requestAccu = new RequestAccu();
        //requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST);

        /*
		RequestOwmIndividual requestOwmIndividual = new RequestOwmIndividual();
		requestOwmIndividual.addRequestServiceType(RetrofitClient.ServiceType.OWM_DAILY_FORECAST);

		 */
        val requestMet = RequestMet()
        requestMet.addRequestServiceType(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST)
        request[WeatherProviderType.MET_NORWAY] = requestMet
        val requestOwmOneCall = RequestOwmOneCall()
        requestOwmOneCall.addRequestServiceType(RetrofitClient.ServiceType.OWM_ONE_CALL)
        val exclude: MutableSet<OneCallApis> = HashSet<OneCallApis>()
        exclude.add(OwmOneCallParameter.OneCallApis.alerts)
        exclude.add(OwmOneCallParameter.OneCallApis.minutely)
        exclude.add(OwmOneCallParameter.OneCallApis.current)
        exclude.add(OwmOneCallParameter.OneCallApis.hourly)
        requestOwmOneCall.setExcludeApis(exclude)

        //request.put(WeatherDataSourceType.ACCU_WEATHER, requestAccu);
        request[WeatherProviderType.OWM_ONECALL] = requestOwmOneCall
        //request.put(WeatherDataSourceType.OWM_INDIVIDUAL, requestOwmIndividual);
        if (countryCode == "KR") {
            val requestKma = RequestKma()
            requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_WEB_FORECASTS)
            request[WeatherProviderType.KMA_WEB] = requestKma
        }
        multipleWeatherRestApiCallback = object : MultipleWeatherRestApiCallback() {
            override fun onResult() {
                setTable(this)
            }

            override fun onCanceled() {}
        }
        multipleWeatherRestApiCallback.setZoneId(zoneId)
        MyApplication.getExecutorService().submit(Runnable {
            MainProcessing.requestNewWeatherData(
                requireContext().getApplicationContext(),
                latitude,
                longitude,
                request,
                multipleWeatherRestApiCallback
            )
        })
    }

    override fun onDestroy() {
        if (multipleWeatherRestApiCallback != null) {
            multipleWeatherRestApiCallback.cancel()
            multipleWeatherRestApiCallback.clear()
        }
        if (dailyForecastResponse != null) dailyForecastResponse!!.clear()
        dailyForecastResponse = null
        multipleWeatherRestApiCallback = null
        super.onDestroy()
    }

    private fun setTable(multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback) {
        val responseMap: Map<WeatherProviderType, ArrayMap<ServiceType, MultipleWeatherRestApiCallback.ResponseResult>> =
            multipleWeatherRestApiCallback.responseMap
        var arrayMap: ArrayMap<ServiceType, MultipleWeatherRestApiCallback.ResponseResult>
        dailyForecastResponse = DailyForecastResponse()

        //kma api
        if (responseMap.containsKey(WeatherProviderType.KMA_API)) {
            arrayMap = responseMap[WeatherProviderType.KMA_API]!!
            val midLandFcstResponse: MultipleWeatherRestApiCallback.ResponseResult? = arrayMap[RetrofitClient.ServiceType.KMA_MID_LAND_FCST]
            val midTaFcstResponse: MultipleWeatherRestApiCallback.ResponseResult? = arrayMap[RetrofitClient.ServiceType.KMA_MID_TA_FCST]
            val vilageFcstResponse: MultipleWeatherRestApiCallback.ResponseResult? = arrayMap[RetrofitClient.ServiceType.KMA_VILAGE_FCST]
            val ultraSrtFcstResponse: MultipleWeatherRestApiCallback.ResponseResult? =
                arrayMap[RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST]
            if (midLandFcstResponse.isSuccessful() && midTaFcstResponse.isSuccessful() &&
                vilageFcstResponse.isSuccessful() && ultraSrtFcstResponse.isSuccessful()
            ) {
                val midLandFcstRoot: MidLandFcstResponse = midLandFcstResponse.getResponseObj() as MidLandFcstResponse
                val midTaRoot: MidTaResponse = midTaFcstResponse.getResponseObj() as MidTaResponse
                val vilageFcstRoot: VilageFcstResponse = vilageFcstResponse.getResponseObj() as VilageFcstResponse
                val ultraSrtFcstRoot: VilageFcstResponse = ultraSrtFcstResponse.getResponseObj() as VilageFcstResponse
                val finalHourlyForecasts: List<FinalHourlyForecast> = KmaResponseProcessor.getFinalHourlyForecastListByXML(
                    ultraSrtFcstRoot,
                    vilageFcstRoot
                )
                val finalDailyForecasts: List<FinalDailyForecast> = KmaResponseProcessor.getFinalDailyForecastListByXML(
                    midLandFcstRoot,
                    midTaRoot,
                    multipleWeatherRestApiCallback.getValue("tmFc").toLong()
                )
                KmaResponseProcessor.getDailyForecastListByXML(finalDailyForecasts, finalHourlyForecasts)
                dailyForecastResponse!!.kmaDailyForecastList = KmaResponseProcessor.makeDailyForecastDtoListOfXML(
                    finalDailyForecasts
                )
            } else {
                if (midLandFcstResponse.getT() != null) {
                    dailyForecastResponse!!.kmaThrowable = midLandFcstResponse.getT()
                } else if (midTaFcstResponse.getT() != null) {
                    dailyForecastResponse!!.kmaThrowable = midTaFcstResponse.getT()
                } else if (vilageFcstResponse.getT() != null) {
                    dailyForecastResponse!!.kmaThrowable = vilageFcstResponse.getT()
                } else {
                    dailyForecastResponse!!.kmaThrowable = ultraSrtFcstResponse.getT()
                }
            }
        }

        //kma web
        if (responseMap.containsKey(WeatherProviderType.KMA_WEB)) {
            arrayMap = responseMap[WeatherProviderType.KMA_WEB]!!
            val forecastsResponseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                arrayMap[RetrofitClient.ServiceType.KMA_WEB_FORECASTS]
            if (forecastsResponseResult.isSuccessful()) {
                val objects = forecastsResponseResult.getResponseObj() as Array<Any>
                val parsedKmaDailyForecasts: List<ParsedKmaDailyForecast> = objects[1] as List<ParsedKmaDailyForecast>
                dailyForecastResponse!!.kmaDailyForecastList = KmaResponseProcessor.makeDailyForecastDtoListOfWEB(
                    parsedKmaDailyForecasts
                )
            } else {
                dailyForecastResponse!!.kmaThrowable = forecastsResponseResult.getT()
            }
        }


        //accu
        if (responseMap.containsKey(WeatherProviderType.ACCU_WEATHER)) {
            arrayMap = responseMap[WeatherProviderType.ACCU_WEATHER]!!
            val accuDailyForecastResponse: MultipleWeatherRestApiCallback.ResponseResult? =
                arrayMap[RetrofitClient.ServiceType.ACCU_DAILY_FORECAST]
            if (accuDailyForecastResponse.isSuccessful()) {
                val dailyForecastsResponse: AccuDailyForecastsResponse =
                    arrayMap[RetrofitClient.ServiceType.ACCU_DAILY_FORECAST].getResponseObj() as AccuDailyForecastsResponse
                dailyForecastResponse!!.accuDailyForecastList = AccuWeatherResponseProcessor.makeDailyForecastDtoList(
                    requireContext().getApplicationContext(),
                    dailyForecastsResponse.getDailyForecasts()
                )
            } else {
                dailyForecastResponse!!.accuThrowable = accuDailyForecastResponse.getT()
            }
        }

        //owm onecall
        if (responseMap.containsKey(WeatherProviderType.OWM_ONECALL)) {
            arrayMap = responseMap[WeatherProviderType.OWM_ONECALL]!!
            val responseResult: MultipleWeatherRestApiCallback.ResponseResult? = arrayMap[RetrofitClient.ServiceType.OWM_ONE_CALL]
            if (responseResult.isSuccessful()) {
                dailyForecastResponse!!.owmDailyForecastList = OwmResponseProcessor.makeDailyForecastDtoListOneCall(
                    requireContext().getApplicationContext(),
                    responseResult.getResponseObj() as OwmOneCallResponse, zoneId
                )
            } else {
                dailyForecastResponse!!.owmThrowable = responseResult.getT()
            }
        }

        //owm individual
        if (responseMap.containsKey(WeatherProviderType.OWM_INDIVIDUAL)) {
            arrayMap = responseMap[WeatherProviderType.OWM_INDIVIDUAL]!!
            val responseResult: MultipleWeatherRestApiCallback.ResponseResult? = arrayMap[RetrofitClient.ServiceType.OWM_DAILY_FORECAST]
            if (responseResult.isSuccessful()) {
                dailyForecastResponse!!.owmDailyForecastList = OwmResponseProcessor.makeDailyForecastDtoListIndividual(
                    requireContext().getApplicationContext(),
                    responseResult.getResponseObj() as OwmDailyForecastResponse, zoneId
                )
            } else {
                dailyForecastResponse!!.owmThrowable = responseResult.getT()
            }
        }

        //met norway
        if (responseMap.containsKey(WeatherProviderType.MET_NORWAY)) {
            arrayMap = responseMap[WeatherProviderType.MET_NORWAY]!!
            val responseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                arrayMap[RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST]
            if (responseResult.isSuccessful()) {
                dailyForecastResponse!!.metNorwayDailyForecastList = MetNorwayResponseProcessor.makeDailyForecastDtoList(
                    requireContext().getApplicationContext(),
                    responseResult.getResponseObj() as LocationForecastResponse, zoneId
                )
            } else {
                dailyForecastResponse!!.metNorwayThrowable = responseResult.getT()
            }
        }
        if (getActivity() != null) {
            MainThreadWorker.runOnUiThread(Runnable {
                setValues(dailyForecastResponse!!)
                binding.rootScrollView.setVisibility(View.VISIBLE)
                ProgressDialog.clearDialogs()
            })
        }
    }

    private class DailyForecastResponse {
        var kmaDailyForecastList: MutableList<DailyForecastDto>? = null
        var accuDailyForecastList: MutableList<DailyForecastDto>? = null
        var owmDailyForecastList: MutableList<DailyForecastDto>? = null
        var metNorwayDailyForecastList: MutableList<DailyForecastDto>? = null
        var kmaThrowable: Throwable? = null
        var accuThrowable: Throwable? = null
        var owmThrowable: Throwable? = null
        var metNorwayThrowable: Throwable? = null
        fun clear() {
            if (kmaDailyForecastList != null) kmaDailyForecastList!!.clear()
            if (accuDailyForecastList != null) accuDailyForecastList!!.clear()
            if (owmDailyForecastList != null) owmDailyForecastList!!.clear()
            if (metNorwayDailyForecastList != null) metNorwayDailyForecastList!!.clear()
            kmaThrowable = null
            accuThrowable = null
            owmThrowable = null
            metNorwayThrowable = null
        }
    }
}