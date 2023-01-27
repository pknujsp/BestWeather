package com.lifedawn.bestweather.ui.weathers.comparison.hourlyforecast

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
import com.lifedawn.bestweather.ui.weathers.view.SingleWeatherIconView
import java.time.ZonedDateTime
import java.util.*

class HourlyForecastComparisonFragment : BaseForecastComparisonFragment() {
    private var multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback? = null
    private var hourlyForecastResponse: HourlyForecastResponse? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        ProgressDialog.show(getActivity(), getString(R.string.msg_refreshing_weather_data)) { v: View? ->
            getParentFragmentManager().popBackStackImmediate()
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
        binding.toolbar.fragmentTitle.setText(R.string.comparison_hourly_forecast)
        binding.addressName.setText(addressName)
        loadForecasts()
    }

    private fun setValuesToViews(hourlyForecastResponse: HourlyForecastResponse) {
        val weatherValueRowHeight: Int = getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC).toInt()
        val context: Context = requireContext().getApplicationContext()
        val weatherProviderTypeList: MutableList<WeatherProviderType> = ArrayList<WeatherProviderType>()
        var kmaFinalHourlyForecasts: MutableList<ForecastObj<HourlyForecastDto?>>? = null
        var accuFinalHourlyForecasts: MutableList<ForecastObj<HourlyForecastDto?>>? = null
        var owmFinalHourlyForecasts: MutableList<ForecastObj<HourlyForecastDto?>>? = null
        var metNorwayFinalHourlyForecasts: MutableList<ForecastObj<HourlyForecastDto?>>? = null
        if (hourlyForecastResponse.kmaSuccessful) {
            val hourlyForecastDtoList: List<HourlyForecastDto>? = hourlyForecastResponse.kmaHourlyForecastList
            kmaFinalHourlyForecasts = ArrayList<ForecastObj<HourlyForecastDto?>>()
            for (finalHourlyForecast in hourlyForecastDtoList) {
                kmaFinalHourlyForecasts!!.add(ForecastObj<HourlyForecastDto>(finalHourlyForecast.hours, finalHourlyForecast))
            }
            weatherProviderTypeList.add(WeatherProviderType.KMA_WEB)
            binding.kma.setVisibility(View.VISIBLE)
        } else {
            binding.kma.setVisibility(View.GONE)
        }
        if (hourlyForecastResponse.accuSuccessful) {
            val hourlyForecastDtoList: List<HourlyForecastDto>? = hourlyForecastResponse.accuHourlyForecastList
            accuFinalHourlyForecasts = ArrayList<ForecastObj<HourlyForecastDto?>>()
            for (finalHourlyForecast in hourlyForecastDtoList) {
                accuFinalHourlyForecasts!!.add(ForecastObj<HourlyForecastDto>(finalHourlyForecast.hours, finalHourlyForecast))
            }
            weatherProviderTypeList.add(WeatherProviderType.ACCU_WEATHER)
            binding.accu.setVisibility(View.VISIBLE)
        } else {
            binding.accu.setVisibility(View.GONE)
        }
        if (hourlyForecastResponse.owmSuccessful) {
            val hourlyForecastDtoList: List<HourlyForecastDto>? = hourlyForecastResponse.owmHourlyForecastList
            owmFinalHourlyForecasts = ArrayList<ForecastObj<HourlyForecastDto?>>()
            for (finalHourlyForecast in hourlyForecastDtoList) {
                owmFinalHourlyForecasts!!.add(ForecastObj<HourlyForecastDto>(finalHourlyForecast.hours, finalHourlyForecast))
            }
            weatherProviderTypeList.add(WeatherProviderType.OWM_ONECALL)
            binding.owm.setVisibility(View.VISIBLE)
        } else {
            binding.owm.setVisibility(View.GONE)
        }
        if (hourlyForecastResponse.metNorwaySuccessful) {
            val hourlyForecastDtoList: List<HourlyForecastDto>? = hourlyForecastResponse.metNorwayHourlyForecastList
            metNorwayFinalHourlyForecasts = ArrayList<ForecastObj<HourlyForecastDto?>>()
            for (finalHourlyForecast in hourlyForecastDtoList) {
                if (finalHourlyForecast.isHasNext6HoursPrecipitation) {
                    break
                }
                metNorwayFinalHourlyForecasts!!.add(ForecastObj<HourlyForecastDto>(finalHourlyForecast.hours, finalHourlyForecast))
            }
            weatherProviderTypeList.add(WeatherProviderType.MET_NORWAY)
            binding.metNorway.setVisibility(View.VISIBLE)
        } else {
            binding.metNorway.setVisibility(View.GONE)
        }
        var now: ZonedDateTime = ZonedDateTime.now(zoneId).plusDays(2).withMinute(0).withSecond(0).withNano(0)
        var firstDateTime = ZonedDateTime.of(now.toLocalDateTime(), zoneId)
        now = now.minusDays(5)
        var lastDateTime = ZonedDateTime.of(now.toLocalDateTime(), zoneId)
        if (kmaFinalHourlyForecasts != null) {
            if (kmaFinalHourlyForecasts[0].dateTime.isBefore(firstDateTime)) {
                firstDateTime = ZonedDateTime.of(kmaFinalHourlyForecasts[0].dateTime.toLocalDateTime(), zoneId)
            }
            if (kmaFinalHourlyForecasts[kmaFinalHourlyForecasts.size - 1].dateTime.isAfter(lastDateTime)) {
                lastDateTime =
                    ZonedDateTime.of(kmaFinalHourlyForecasts[kmaFinalHourlyForecasts.size - 1].dateTime.toLocalDateTime(), zoneId)
            }
        }
        if (accuFinalHourlyForecasts != null) {
            if (accuFinalHourlyForecasts[0].dateTime.isBefore(firstDateTime)) {
                firstDateTime = ZonedDateTime.of(accuFinalHourlyForecasts[0].dateTime.toLocalDateTime(), zoneId)
            }
            if (accuFinalHourlyForecasts[accuFinalHourlyForecasts.size - 1].dateTime.isAfter(lastDateTime)) {
                lastDateTime =
                    ZonedDateTime.of(accuFinalHourlyForecasts[accuFinalHourlyForecasts.size - 1].dateTime.toLocalDateTime(), zoneId)
            }
        }
        if (owmFinalHourlyForecasts != null) {
            if (owmFinalHourlyForecasts[0].dateTime.isBefore(firstDateTime)) {
                firstDateTime = ZonedDateTime.of(owmFinalHourlyForecasts[0].dateTime.toLocalDateTime(), zoneId)
            }
            if (owmFinalHourlyForecasts[owmFinalHourlyForecasts.size - 1].dateTime.isAfter(lastDateTime)) {
                lastDateTime =
                    ZonedDateTime.of(owmFinalHourlyForecasts[owmFinalHourlyForecasts.size - 1].dateTime.toLocalDateTime(), zoneId)
            }
        }
        if (metNorwayFinalHourlyForecasts != null) {
            if (metNorwayFinalHourlyForecasts[0].dateTime.isBefore(firstDateTime)) {
                firstDateTime = ZonedDateTime.of(metNorwayFinalHourlyForecasts[0].dateTime.toLocalDateTime(), zoneId)
            }
            if (metNorwayFinalHourlyForecasts[metNorwayFinalHourlyForecasts.size - 1].dateTime.isAfter(lastDateTime)) {
                lastDateTime = ZonedDateTime.of(
                    metNorwayFinalHourlyForecasts[metNorwayFinalHourlyForecasts.size - 1].dateTime.toLocalDateTime(),
                    zoneId
                )
            }
        }
        val dateTimeList: MutableList<ZonedDateTime> = ArrayList()
        val hourList: MutableList<String> = ArrayList()
        //firstDateTime부터 lastDateTime까지 추가
        now = ZonedDateTime.of(firstDateTime.toLocalDateTime(), zoneId)
        while (!now.isAfter(lastDateTime)) {
            dateTimeList.add(now)
            hourList.add(now.hour.toString())
            now = now.plusHours(1)
        }
        val columnsCount = dateTimeList.size
        val columnWidth: Int = getResources().getDimension(R.dimen.valueColumnWidthInSCHourly).toInt()
        val valueRowWidth = columnWidth * columnsCount

        //날짜, 시각, 날씨, 기온, 강수량, 강수확률
        dateRow = DateView(context, FragmentType.Comparison, valueRowWidth, columnWidth)
        val clockRow = TextsView(context, valueRowWidth, columnWidth, hourList)
        val weatherIconRows: Array<SingleWeatherIconView?> = arrayOfNulls<SingleWeatherIconView>(columnsCount)
        val tempRows: Array<TextsView?> = arrayOfNulls<TextsView>(columnsCount)
        val rainVolumeRows: Array<IconTextView?> = arrayOfNulls<IconTextView>(columnsCount)
        val probabilityOfPrecipitationRows: Array<IconTextView?> = arrayOfNulls<IconTextView>(columnsCount)
        val snowVolumeRows: Array<IconTextView?> = arrayOfNulls<IconTextView>(columnsCount)
        for (i in weatherProviderTypeList.indices) {
            var specificRowWidth = 0
            var beginColumnIndex = 0
            if (weatherProviderTypeList[i] === WeatherProviderType.KMA_WEB) {
                specificRowWidth = kmaFinalHourlyForecasts!!.size * columnWidth
                for (idx in dateTimeList.indices) {
                    if (dateTimeList[idx] == kmaFinalHourlyForecasts[0].dateTime) {
                        beginColumnIndex = idx
                        break
                    }
                }
            } else if (weatherProviderTypeList[i] === WeatherProviderType.ACCU_WEATHER) {
                specificRowWidth = accuFinalHourlyForecasts!!.size * columnWidth
                for (idx in dateTimeList.indices) {
                    if (dateTimeList[idx] == accuFinalHourlyForecasts[0].dateTime) {
                        beginColumnIndex = idx
                        break
                    }
                }
            } else if (weatherProviderTypeList[i] === WeatherProviderType.OWM_ONECALL) {
                specificRowWidth = owmFinalHourlyForecasts!!.size * columnWidth
                for (idx in dateTimeList.indices) {
                    if (dateTimeList[idx] == owmFinalHourlyForecasts[0].dateTime) {
                        beginColumnIndex = idx
                        break
                    }
                }
            } else if (weatherProviderTypeList[i] === WeatherProviderType.MET_NORWAY) {
                specificRowWidth = metNorwayFinalHourlyForecasts!!.size * columnWidth
                for (idx in dateTimeList.indices) {
                    if (dateTimeList[idx] == metNorwayFinalHourlyForecasts[0].dateTime) {
                        beginColumnIndex = idx
                        break
                    }
                }
            }
            weatherIconRows[i] = SingleWeatherIconView(
                context, FragmentType.Comparison, specificRowWidth, weatherValueRowHeight,
                columnWidth
            )
            weatherIconRows[i].setTag(R.id.begin_column_index, beginColumnIndex)
            tempRows[i] = TextsView(context, specificRowWidth, columnWidth, null)
            tempRows[i].setTag(R.id.begin_column_index, beginColumnIndex)
            rainVolumeRows[i] = IconTextView(
                context, FragmentType.Comparison, specificRowWidth,
                columnWidth, R.drawable.raindrop
            )
            rainVolumeRows[i].setTag(R.id.begin_column_index, beginColumnIndex)
            probabilityOfPrecipitationRows[i] = IconTextView(
                context, FragmentType.Comparison,
                specificRowWidth, columnWidth, R.drawable.pop
            )
            probabilityOfPrecipitationRows[i].setTag(R.id.begin_column_index, beginColumnIndex)
            snowVolumeRows[i] = IconTextView(
                context, FragmentType.Comparison, specificRowWidth,
                columnWidth, R.drawable.snowparticle
            )
            snowVolumeRows[i].setTag(R.id.begin_column_index, beginColumnIndex)
        }

        //날짜, 시각
        dateRow.init(dateTimeList)
        val cm = "cm"
        val mm = "mm"
        val degree = "°"
        val weatherSourceUnitObjList: MutableList<WeatherSourceUnitObj> = ArrayList<WeatherSourceUnitObj>()

        //날씨,기온,강수량,강수확률
        //kma, accu weather, owm, met Norway 순서
        for (i in weatherProviderTypeList.indices) {
            val weatherIconObjList: MutableList<SingleWeatherIconView.WeatherIconObj> = ArrayList()
            val tempList: MutableList<String> = ArrayList()
            val popList: MutableList<String> = ArrayList()
            val rainVolumeList: MutableList<String> = ArrayList()
            val snowVolumeList: MutableList<String> = ArrayList()
            var haveSnow = false
            var haveRain = false
            if (weatherProviderTypeList[i] === WeatherProviderType.KMA_WEB) {
                for (item in kmaFinalHourlyForecasts) {
                    weatherIconObjList.add(
                        SingleWeatherIconView.WeatherIconObj(
                            ContextCompat.getDrawable(context, item.e.weatherIcon), item.e.weatherDescription
                        )
                    )
                    tempList.add(item.e.temp.replace(tempUnitText, degree))
                    popList.add(item.e.pop)
                    rainVolumeList.add(item.e.rainVolume.replace(mm, ""))
                    if (item.e.isHasSnow) {
                        if (!haveSnow) {
                            haveSnow = true
                        }
                    }
                    if (item.e.isHasRain) {
                        if (!haveRain) {
                            haveRain = true
                        }
                    }
                    snowVolumeList.add(item.e.snowVolume.replace(cm, ""))
                }
            } else if (weatherProviderTypeList[i] === WeatherProviderType.ACCU_WEATHER) {
                for (item in accuFinalHourlyForecasts) {
                    dateTimeList.add(item.e.hours)
                    weatherIconObjList.add(
                        SingleWeatherIconView.WeatherIconObj(
                            ContextCompat.getDrawable(context, item.e.weatherIcon), item.e.weatherDescription
                        )
                    )
                    tempList.add(item.e.temp.replace(tempUnitText, degree))
                    popList.add(item.e.pop)
                    rainVolumeList.add(item.e.rainVolume.replace(mm, ""))
                    if (item.e.isHasSnow) {
                        if (!haveSnow) {
                            haveSnow = true
                        }
                    }
                    if (item.e.isHasRain) {
                        if (!haveRain) {
                            haveRain = true
                        }
                    }
                    snowVolumeList.add(item.e.snowVolume.replace(cm, ""))
                }
            } else if (weatherProviderTypeList[i] === WeatherProviderType.OWM_ONECALL) {
                for (item in owmFinalHourlyForecasts) {
                    dateTimeList.add(item.e.hours)
                    weatherIconObjList.add(
                        SingleWeatherIconView.WeatherIconObj(
                            ContextCompat.getDrawable(context, item.e.weatherIcon),
                            item.e.weatherDescription
                        )
                    )
                    tempList.add(item.e.temp.replace(tempUnitText, degree))
                    popList.add(item.e.pop)
                    rainVolumeList.add(item.e.rainVolume.replace("mm", ""))
                    if (item.e.isHasSnow) {
                        if (!haveSnow) {
                            haveSnow = true
                        }
                    }
                    if (item.e.isHasRain) {
                        if (!haveRain) {
                            haveRain = true
                        }
                    }
                    snowVolumeList.add(item.e.snowVolume.replace("mm", ""))
                }
            } else if (weatherProviderTypeList[i] === WeatherProviderType.MET_NORWAY) {
                for (item in metNorwayFinalHourlyForecasts) {
                    dateTimeList.add(item.e.hours)
                    weatherIconObjList.add(
                        SingleWeatherIconView.WeatherIconObj(
                            ContextCompat.getDrawable(context, item.e.weatherIcon),
                            item.e.weatherDescription
                        )
                    )
                    tempList.add(item.e.temp.replace(tempUnitText, degree))
                    popList.add("-")
                    rainVolumeList.add(item.e.precipitationVolume.replace("mm", ""))
                    if (item.e.isHasPrecipitation) {
                        if (!haveRain) {
                            haveRain = true
                        }
                    }
                }
            }
            weatherSourceUnitObjList.add(WeatherSourceUnitObj(weatherProviderTypeList[i], haveRain, haveSnow))
            weatherIconRows[i].setWeatherImgs(weatherIconObjList)
            tempRows[i].setValueList(tempList)
            probabilityOfPrecipitationRows[i].setValueList(popList)
            rainVolumeRows[i].setValueList(rainVolumeList)
            if (haveSnow) {
                snowVolumeRows[i].setValueList(snowVolumeList)
            }
        }
        createValueUnitsDescription(weatherSourceUnitObjList)
        val rowLayoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        clockRow.setValueTextColor(Color.BLACK)
        binding.datetime.addView(dateRow, rowLayoutParams)
        binding.datetime.addView(clockRow, rowLayoutParams)
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
            view.addView(rainVolumeRows[i], iconTextRowLayoutParams)
            if (snowVolumeRows[i].getValueList() != null) {
                view.addView(snowVolumeRows[i], iconTextRowLayoutParams)
            }
            tempRows[i].setValueTextSize(17)
            tempRows[i].setValueTextColor(Color.BLACK)
            view.addView(tempRows[i], tempRowLayoutParams)
        }
        customViewList.add(dateRow)
        customViewList.add(clockRow)
        customViewList.addAll(Arrays.asList<TextsView>(*tempRows))
        customViewList.addAll(Arrays.asList<IconTextView>(*rainVolumeRows))
        customViewList.addAll(Arrays.asList<IconTextView>(*probabilityOfPrecipitationRows))
        customViewList.addAll(Arrays.asList<SingleWeatherIconView>(*weatherIconRows))
        customViewList.addAll(Arrays.asList<IconTextView>(*snowVolumeRows))
        customViewList.addAll(Arrays.asList<NotScrolledView>(*notScrolledViews))
    }

    private fun loadForecasts() {
        MyApplication.getExecutorService().submit(Runnable {
            val request: ArrayMap<WeatherProviderType, RequestWeatherSource> = ArrayMap<WeatherProviderType, RequestWeatherSource>()

            //RequestAccu requestAccu = new RequestAccu();
            //requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST);
            val requestOwmOneCall = RequestOwmOneCall()
            requestOwmOneCall.addRequestServiceType(RetrofitClient.ServiceType.OWM_ONE_CALL)
            val exclude: MutableSet<OneCallApis> = HashSet<OneCallApis>()
            exclude.add(OwmOneCallParameter.OneCallApis.alerts)
            exclude.add(OwmOneCallParameter.OneCallApis.minutely)
            exclude.add(OwmOneCallParameter.OneCallApis.current)
            exclude.add(OwmOneCallParameter.OneCallApis.daily)
            requestOwmOneCall.setExcludeApis(exclude)

            //request.put(WeatherDataSourceType.ACCU_WEATHER, requestAccu);
            request[WeatherProviderType.OWM_ONECALL] = requestOwmOneCall
            val requestMet = RequestMet()
            requestMet.addRequestServiceType(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST)
            request[WeatherProviderType.MET_NORWAY] = requestMet

            /*
		RequestOwmIndividual requestOwmIndividual = new RequestOwmIndividual();
		requestOwmIndividual.addRequestServiceType(RetrofitClient.ServiceType.OWM_HOURLY_FORECAST);
		request.put(WeatherDataSourceType.OWM_INDIVIDUAL,requestOwmIndividual);

		 */if (countryCode == "KR") {
            val requestKma = RequestKma()
            requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_WEB_FORECASTS)
            request[WeatherProviderType.KMA_WEB] = requestKma
        }
            multipleWeatherRestApiCallback = object : MultipleWeatherRestApiCallback() {
                override fun onResult() {
                    setTable(multipleWeatherRestApiCallback, latitude, longitude)
                }

                override fun onCanceled() {}
            }
            multipleWeatherRestApiCallback.setZoneId(zoneId)
            MainProcessing.requestNewWeatherData(
                requireContext().getApplicationContext(),
                latitude,
                longitude,
                request,
                multipleWeatherRestApiCallback
            )
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (multipleWeatherRestApiCallback != null) {
            multipleWeatherRestApiCallback.cancel()
            multipleWeatherRestApiCallback.clear()
        }
        if (hourlyForecastResponse != null) hourlyForecastResponse!!.clear()
        hourlyForecastResponse = null
        multipleWeatherRestApiCallback = null
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun setTable(multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback?, latitude: Double, longitude: Double) {
        val responseMap: Map<WeatherProviderType, ArrayMap<ServiceType, MultipleWeatherRestApiCallback.ResponseResult>> =
            multipleWeatherRestApiCallback.responseMap
        var arrayMap: ArrayMap<ServiceType, MultipleWeatherRestApiCallback.ResponseResult>
        hourlyForecastResponse = HourlyForecastResponse()
        val context: Context = requireContext().getApplicationContext()

        //kma api
        if (responseMap.containsKey(WeatherProviderType.KMA_API)) {
            arrayMap = responseMap[WeatherProviderType.KMA_API]!!
            val ultraSrtFcstResponse: MultipleWeatherRestApiCallback.ResponseResult? =
                arrayMap[RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST]
            val vilageFcstResponse: MultipleWeatherRestApiCallback.ResponseResult? = arrayMap[RetrofitClient.ServiceType.KMA_VILAGE_FCST]
            if (ultraSrtFcstResponse.isSuccessful() && vilageFcstResponse.isSuccessful()) {
                val ultraSrtFcstRoot: VilageFcstResponse = ultraSrtFcstResponse.getResponseObj() as VilageFcstResponse
                val vilageFcstRoot: VilageFcstResponse = vilageFcstResponse.getResponseObj() as VilageFcstResponse
                hourlyForecastResponse!!.kmaSuccessful = true
                hourlyForecastResponse!!.kmaHourlyForecastList = KmaResponseProcessor.makeHourlyForecastDtoListOfXML(
                    context, KmaResponseProcessor.getFinalHourlyForecastListByXML(
                        ultraSrtFcstRoot,
                        vilageFcstRoot
                    ), latitude, longitude
                )
            } else {
                if (!ultraSrtFcstResponse.isSuccessful()) {
                    hourlyForecastResponse!!.kmaThrowable = ultraSrtFcstResponse.getT()
                } else {
                    hourlyForecastResponse!!.kmaThrowable = vilageFcstResponse.getT()
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
                val parsedKmaHourlyForecasts: List<ParsedKmaHourlyForecast> = objects[0] as List<ParsedKmaHourlyForecast>
                hourlyForecastResponse!!.kmaSuccessful = true
                hourlyForecastResponse!!.kmaHourlyForecastList = KmaResponseProcessor.makeHourlyForecastDtoListOfWEB(
                    context,
                    parsedKmaHourlyForecasts, latitude, longitude
                )
            } else {
                hourlyForecastResponse!!.kmaThrowable = forecastsResponseResult.getT()
            }
        }


        //accu
        if (responseMap.containsKey(WeatherProviderType.ACCU_WEATHER)) {
            arrayMap = responseMap[WeatherProviderType.ACCU_WEATHER]!!
            val accuHourlyForecastResponse: MultipleWeatherRestApiCallback.ResponseResult? =
                arrayMap[RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST]
            if (accuHourlyForecastResponse.isSuccessful()) {
                val hourlyForecastsResponse: AccuHourlyForecastsResponse =
                    accuHourlyForecastResponse.getResponseObj() as AccuHourlyForecastsResponse
                hourlyForecastResponse!!.accuSuccessful = true
                hourlyForecastResponse!!.accuHourlyForecastList = AccuWeatherResponseProcessor.makeHourlyForecastDtoList(
                    context,
                    hourlyForecastsResponse.getItems()
                )
            } else {
                hourlyForecastResponse!!.accuThrowable = accuHourlyForecastResponse.getT()
            }
        }
        //owm onecall
        if (responseMap.containsKey(WeatherProviderType.OWM_ONECALL)) {
            arrayMap = responseMap[WeatherProviderType.OWM_ONECALL]!!
            val responseResult: MultipleWeatherRestApiCallback.ResponseResult? = arrayMap[RetrofitClient.ServiceType.OWM_ONE_CALL]
            if (responseResult.isSuccessful()) {
                hourlyForecastResponse!!.owmSuccessful = true
                hourlyForecastResponse!!.owmHourlyForecastList = OwmResponseProcessor.makeHourlyForecastDtoListOneCall(
                    context,
                    responseResult.getResponseObj() as OwmOneCallResponse, zoneId
                )
            } else {
                hourlyForecastResponse!!.owmThrowable = responseResult.getT()
            }
        }
        //owm individual
        if (responseMap.containsKey(WeatherProviderType.OWM_INDIVIDUAL)) {
            arrayMap = responseMap[WeatherProviderType.OWM_INDIVIDUAL]!!
            val responseResult: MultipleWeatherRestApiCallback.ResponseResult? = arrayMap[RetrofitClient.ServiceType.OWM_HOURLY_FORECAST]
            if (responseResult.isSuccessful()) {
                hourlyForecastResponse!!.owmSuccessful = true
                hourlyForecastResponse!!.owmHourlyForecastList = OwmResponseProcessor.makeHourlyForecastDtoListIndividual(
                    context,
                    responseResult.getResponseObj() as OwmHourlyForecastResponse, zoneId
                )
            } else {
                hourlyForecastResponse!!.owmThrowable = responseResult.getT()
            }
        }

        // met norway
        if (responseMap.containsKey(WeatherProviderType.MET_NORWAY)) {
            arrayMap = responseMap[WeatherProviderType.MET_NORWAY]!!
            val responseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                arrayMap[RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST]
            if (responseResult.isSuccessful()) {
                hourlyForecastResponse!!.metNorwaySuccessful = true
                hourlyForecastResponse!!.metNorwayHourlyForecastList = MetNorwayResponseProcessor.makeHourlyForecastDtoList(
                    context,
                    responseResult.getResponseObj() as LocationForecastResponse, zoneId
                )
            } else {
                hourlyForecastResponse!!.metNorwayThrowable = responseResult.getT()
            }
        }
        try {
            if (getActivity() != null) {
                MainThreadWorker.runOnUiThread(Runnable {
                    setValuesToViews(hourlyForecastResponse!!)
                    binding.rootScrollView.setVisibility(View.VISIBLE)
                    ProgressDialog.clearDialogs()
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private class HourlyForecastResponse {
        var kmaHourlyForecastList: MutableList<HourlyForecastDto>? = null
        var accuHourlyForecastList: MutableList<HourlyForecastDto>? = null
        var owmHourlyForecastList: MutableList<HourlyForecastDto>? = null
        var metNorwayHourlyForecastList: MutableList<HourlyForecastDto>? = null
        var kmaSuccessful = false
        var accuSuccessful = false
        var owmSuccessful = false
        var metNorwaySuccessful = false
        var kmaThrowable: Throwable? = null
        var accuThrowable: Throwable? = null
        var owmThrowable: Throwable? = null
        var metNorwayThrowable: Throwable? = null
        fun clear() {
            if (kmaHourlyForecastList != null) kmaHourlyForecastList!!.clear()
            if (accuHourlyForecastList != null) accuHourlyForecastList!!.clear()
            if (owmHourlyForecastList != null) owmHourlyForecastList!!.clear()
            if (metNorwayHourlyForecastList != null) metNorwayHourlyForecastList!!.clear()
            kmaHourlyForecastList = null
            accuHourlyForecastList = null
            owmHourlyForecastList = null
            metNorwayHourlyForecastList = null
            kmaThrowable = null
            owmThrowable = null
            accuThrowable = null
            metNorwayThrowable = null
        }
    }
}