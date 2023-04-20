package com.lifedawn.bestweather.ui.widget.creator

import android.appwidget.AppWidgetManager
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil.onSuccessfulProcess
import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto
import com.lifedawn.bestweather.data.local.weather.models.CurrentConditionsDto
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.getGradeDescription
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.parseTextToAirQualityDto
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.WeatherResponseProcessor
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.WeatherRequestUtil.initWeatherSourceUniqueValues
import com.lifedawn.bestweather.ui.widget.OnDrawBitmapCallback
import com.lifedawn.bestweather.ui.widget.widgetprovider.EighthWidgetProvider
import java.time.ZoneId
import java.time.ZonedDateTime

class EighthWidgetCreator(context: Context, widgetUpdateCallback: WidgetUpdateCallback?, appWidgetId: Int) :
    AbstractWidgetCreator(context, widgetUpdateCallback, appWidgetId) {
    private val hourlyForecastCount = 7
    private val dailyForecastCount = 3
    override val requestWeatherDataTypeSet: Set<Any?>
        get() {
            val set: MutableSet<WeatherDataType?> = HashSet()
            set.add(WeatherDataType.currentConditions)
            set.add(WeatherDataType.hourlyForecast)
            set.add(WeatherDataType.airQuality)
            return set
        }

    override fun createTempViews(parentWidth: Int?, parentHeight: Int?): RemoteViews? {
        val remoteViews = createBaseRemoteViews()
        onSuccessfulProcess(remoteViews!!)
        drawViews(
            remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(),
            WeatherResponseProcessor.getTempCurrentConditionsDto(context),
            WeatherResponseProcessor.getTempHourlyForecastDtoList(context, hourlyForecastCount),
            null, WeatherResponseProcessor.getTempAirQualityDto(), null
        )
        return remoteViews
    }

    override fun widgetProviderClass(): Class<*> {
        return EighthWidgetProvider::class.java
    }

    fun setDataViews(
        remoteViews: RemoteViews?,
        addressName: String,
        lastRefreshDateTime: String,
        currentConditionsDto: CurrentConditionsDto?,
        hourlyForecastDtoList: List<HourlyForecastDto?>,
        dailyForecastDtoList: List<DailyForecastDto>?,
        airQualityDto: AirQualityDto?,
        onDrawBitmapCallback: OnDrawBitmapCallback?
    ) {
        drawViews(
            remoteViews, addressName, lastRefreshDateTime, currentConditionsDto, hourlyForecastDtoList, dailyForecastDtoList, airQualityDto,
            onDrawBitmapCallback
        )
    }

    private fun drawViews(
        remoteViews: RemoteViews?, addressName: String, lastRefreshDateTime: String, currentConditionsDto: CurrentConditionsDto?,
        hourlyForecastDtoList: List<HourlyForecastDto?>, dailyForecastDtoList: List<DailyForecastDto>?, airQualityDto: AirQualityDto?,
        onDrawBitmapCallback: OnDrawBitmapCallback?
    ) {
        val valuesRemoteViews = RemoteViews(context.packageName, R.layout.view_eighth_widget_values)
        valuesRemoteViews.setTextViewText(R.id.address, addressName)
        valuesRemoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter))
        val clockZoneId = ZoneId.systemDefault()
        valuesRemoteViews.setString(R.id.timeClock, "setTimeZone", clockZoneId.id)
        valuesRemoteViews.setString(R.id.dateClock, "setTimeZone", clockZoneId.id)

        //현재 날씨------------------------------------------------------
        valuesRemoteViews.setTextViewText(R.id.temperature, currentConditionsDto!!.temp.replace(tempDegree, "°"))
        valuesRemoteViews.setImageViewResource(R.id.weatherIcon, currentConditionsDto.getWeatherIcon())
        var airQuality: String? = null
        airQuality = if (airQualityDto.isSuccessful()) {
            getGradeDescription(airQualityDto!!.aqi)
        } else {
            context.getString(R.string.noData)
        }
        valuesRemoteViews.setTextViewText(R.id.airQuality, airQuality)

        /*
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("E");

		//----------------daily---------------------------------------------------------------
		for (int cell = 0; cell < dailyForecastCount; cell++) {
			RemoteViews dailyRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_forecast_item_in_linear);

			dailyRemoteViews.setTextViewText(R.id.dateTime, dailyForecastDtoList.get(cell).getDate().format(dateFormatter));

			if (dailyForecastDtoList.get(cell).isSingle()) {
				dailyRemoteViews.setImageViewResource(R.id.leftIcon, dailyForecastDtoList.get(cell).getSingleValues().getWeatherIcon());
				dailyRemoteViews.setViewVisibility(R.id.rightIcon, View.GONE);

				dailyRemoteViews.setTextViewText(R.id.pop, dailyForecastDtoList.get(cell).getSingleValues().getPop());
			} else {
				dailyRemoteViews.setImageViewResource(R.id.leftIcon, dailyForecastDtoList.get(cell).getAmValues().getWeatherIcon());
				dailyRemoteViews.setImageViewResource(R.id.rightIcon, dailyForecastDtoList.get(cell).getPmValues().getWeatherIcon());
				dailyRemoteViews.setTextViewText(R.id.pop, dailyForecastDtoList.get(cell).getAmValues().getPop() + "/" +
						dailyForecastDtoList.get(cell).getPmValues().getPop());
			}

			dailyRemoteViews.setTextViewText(R.id.temperature,
					dailyForecastDtoList.get(cell).getMinTemp() + "/" + dailyForecastDtoList.get(cell).getMaxTemp());

			dailyRemoteViews.setViewVisibility(R.id.rainVolumeLayout, View.GONE);
			dailyRemoteViews.setViewVisibility(R.id.snowVolumeLayout, View.GONE);

			valuesRemoteViews.addView(R.id.dailyForecast, dailyRemoteViews);
		}
		 */remoteViews!!.removeAllViews(R.id.noBitmapValuesView)
        remoteViews.addView(R.id.noBitmapValuesView, valuesRemoteViews)
        remoteViews.setViewVisibility(R.id.noBitmapValuesView, View.VISIBLE)
        remoteViews.setViewVisibility(R.id.bitmapValuesView, View.GONE)
    }

    override fun setDisplayClock(displayClock: Boolean) {
        widgetDto.isDisplayClock = displayClock
    }

    override fun setDataViewsOfSavedData() {
        var weatherProviderType = WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherProviderTypeSet())
        if (widgetDto.isTopPriorityKma && widgetDto.countryCode == "KR") weatherProviderType = WeatherProviderType.KMA_WEB
        val remoteViews = createRemoteViews()
        val jsonObject = JsonParser.parseString(widgetDto.responseText) as JsonObject
        initWeatherSourceUniqueValues(weatherProviderType, true, context)
        zoneId = ZoneId.of(widgetDto.timeZoneId)
        val currentConditionsDto = WeatherResponseProcessor.parseTextToCurrentConditionsDto(
            context, jsonObject,
            weatherProviderType, widgetDto.latitude, widgetDto.longitude, zoneId
        )
        val hourlyForecastDtoList = WeatherResponseProcessor.parseTextToHourlyForecastDtoList(
            context, jsonObject,
            weatherProviderType, widgetDto.latitude, widgetDto.longitude, zoneId
        )
        val airQualityDto = parseTextToAirQualityDto(jsonObject)
        setDataViews(
            remoteViews, widgetDto.addressName, widgetDto.lastRefreshDateTime, currentConditionsDto,
            hourlyForecastDtoList, null, airQualityDto, null
        )
        val appWidgetManager = AppWidgetManager.getInstance(context)
        onSuccessfulProcess(remoteViews!!)
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }

    override fun setResultViews(appWidgetId: Int, multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback?, zoneId: ZoneId?) {
        this.zoneId = zoneId
        val mainWeatherProviderType = WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherProviderTypeSet())
        val currentConditionsDto = WeatherResponseProcessor.getCurrentConditionsDto(
            context, multipleWeatherRestApiCallback,
            mainWeatherProviderType, this.zoneId
        )
        val hourlyForecastDtoList = WeatherResponseProcessor.getHourlyForecastDtoList(
            context, multipleWeatherRestApiCallback,
            mainWeatherProviderType, this.zoneId
        )
        val successful = currentConditionsDto != null && !hourlyForecastDtoList.isEmpty()
        if (successful) {
            val zoneOffset = currentConditionsDto!!.currentTime.offset
            widgetDto.timeZoneId = zoneId!!.id
            widgetDto.lastRefreshDateTime = multipleWeatherRestApiCallback.getRequestDateTime().toString()
            makeResponseTextToJson(
                multipleWeatherRestApiCallback,
                requestWeatherDataTypeSet,
                widgetDto.getWeatherProviderTypeSet(),
                widgetDto,
                zoneOffset
            )
        }
        widgetDto.isLoadSuccessful = successful
        super.setResultViews(appWidgetId, multipleWeatherRestApiCallback, zoneId)
    }
}