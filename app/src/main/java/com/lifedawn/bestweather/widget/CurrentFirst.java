package com.lifedawn.bestweather.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.util.ArrayMap;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAccu;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAqicn;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwm;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.OneCallParameter;
import com.lifedawn.bestweather.retrofit.responses.accuweather.ValueUnit;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

public class CurrentFirst extends RootAppWidget {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

	}

	@Override
	public void onEnabled(Context context) {
	}

	@Override
	public void onDisabled(Context context) {
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
	}

	@Override
	public void onTimeTick(Context context) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ZonedDateTime now = ZonedDateTime.now();
		ComponentName componentName = new ComponentName(context.getPackageName(), CurrentFirst.class.getName());
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(context.getString(R.string.date_pattern));
		ValueUnits clockUnit =
				ValueUnits.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_unit_clock), ValueUnits.clock12.name()));
		DateTimeFormatter timeFormatter =
				DateTimeFormatter.ofPattern(context.getString(clockUnit == ValueUnits.clock12 ? R.string.clock_12_pattern :
						R.string.clock_24_pattern));

		for (int appWidgetId : appWidgetIds) {
			if (context.getSharedPreferences(ConfigureWidgetActivity.WidgetAttributes.WIDGET_ATTRIBUTES_ID_.name() + appWidgetId, Context.MODE_PRIVATE)
					.getBoolean(ConfigureWidgetActivity.WidgetAttributes.DISPLAY_DATETIME.name(), false)) {
				SharedPreferences sharedPreferences = context.getSharedPreferences(ConfigureWidgetActivity.WidgetAttributes.WIDGET_ATTRIBUTES_ID_.name() + appWidgetId, Context.MODE_PRIVATE);
				boolean displayLocalDateTime =
						sharedPreferences.getBoolean(ConfigureWidgetActivity.WidgetAttributes.DISPLAY_LOCAL_DATETIME.name(), false);

				String timeZoneId = ZoneId.systemDefault().getId();
				if (displayLocalDateTime) {
					timeZoneId = sharedPreferences.getString(WidgetDataKeys.TIMEZONE.name(), "");
				}
				ZonedDateTime zonedDateTime = ZonedDateTime.of(now.toLocalDateTime(), ZoneId.of(timeZoneId));

				RemoteViews remoteViews = createViews(context, appWidgetId,
						sharedPreferences.getInt(ConfigureWidgetActivity.WidgetAttributes.LAYOUT_ID.name(), 0));
				remoteViews.setTextViewText(R.id.date, zonedDateTime.format(dateFormatter));
				remoteViews.setTextViewText(R.id.time, zonedDateTime.format(timeFormatter));

				appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
			}
		}
	}

	@Override
	public void loadWeatherData(Context context, int appWidgetId) {
		super.loadWeatherData(context, appWidgetId);
		SharedPreferences widgetAttributes =
				context.getSharedPreferences(ConfigureWidgetActivity.WidgetAttributes.WIDGET_ATTRIBUTES_ID_.name() + appWidgetId,
						Context.MODE_PRIVATE);
		final WeatherSourceType weatherSourceType =
				WeatherSourceType.valueOf(widgetAttributes.getString(ConfigureWidgetActivity.WidgetAttributes.WEATHER_SOURCE_TYPE.name(),
						WeatherSourceType.OPEN_WEATHER_MAP.name()));
		ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources =
				makeRequestWeatherSources(weatherSourceType);

		if (weatherSourceType == WeatherSourceType.KMA) {
			RequestKma requestKma = (RequestKma) requestWeatherSources.get(weatherSourceType);
			requestKma.addRequestServiceType(RetrofitClient.ServiceType.ULTRA_SRT_NCST);
		} else if (weatherSourceType == WeatherSourceType.ACCU_WEATHER) {
			RequestAccu requestAccu = (RequestAccu) requestWeatherSources.get(weatherSourceType);
			requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS);
		} else if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			RequestOwm requestOwm = (RequestOwm) requestWeatherSources.get(weatherSourceType);
			Set<OneCallParameter.OneCallApis> excludeSet = new HashSet<>();
			excludeSet.add(OneCallParameter.OneCallApis.daily);
			excludeSet.add(OneCallParameter.OneCallApis.hourly);
			excludeSet.add(OneCallParameter.OneCallApis.minutely);
			excludeSet.add(OneCallParameter.OneCallApis.alerts);

			requestOwm.setExcludeApis(excludeSet);
			requestOwm.addRequestServiceType(RetrofitClient.ServiceType.OWM_ONE_CALL);
		}

		RequestAqicn requestAqicn = new RequestAqicn();
		requestAqicn.addRequestServiceType(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);
		requestWeatherSources.put(WeatherSourceType.AQICN, requestAqicn);

		Double latitude = Double.parseDouble(widgetAttributes.getString(WidgetDataKeys.LATITUDE.name(), "0.0"));
		Double longitude = Double.parseDouble(widgetAttributes.getString(WidgetDataKeys.LONGITUDE.name(), "0.0"));

		MainProcessing.requestNewWeatherData(context, latitude, longitude, requestWeatherSources, new MultipleJsonDownloader<JsonElement>() {
			@Override
			public void onResult() {
				initWeatherSourceUniqueValues(context, weatherSourceType);
				setResultViews(context, appWidgetId, this);
			}
		});
	}

	@Override
	public void setResultViews(Context context, int appWidgetId, @Nullable MultipleJsonDownloader<JsonElement> multipleJsonDownloader) {
		super.setResultViews(context, appWidgetId, multipleJsonDownloader);

		SharedPreferences widgetAttributes =
				context.getSharedPreferences(ConfigureWidgetActivity.WidgetAttributes.WIDGET_ATTRIBUTES_ID_.name() + appWidgetId,
						Context.MODE_PRIVATE);
		final WeatherSourceType requestWeatherSourceType =
				WeatherSourceType.valueOf(widgetAttributes.getString(ConfigureWidgetActivity.WidgetAttributes.WEATHER_SOURCE_TYPE.name(),
						WeatherSourceType.OPEN_WEATHER_MAP.name()));

		CurrentConditionsObj currentConditionsObj = getCurrentConditions(context, requestWeatherSourceType, multipleJsonDownloader,
				appWidgetId);
		HeaderObj headerObj = getHeader(context, multipleJsonDownloader, appWidgetId, currentConditionsObj.timeZone);

		RemoteViews remoteViews = createViews(context, appWidgetId,
				widgetAttributes.getInt(ConfigureWidgetActivity.WidgetAttributes.LAYOUT_ID.name(), 0));
		remoteViews.setTextViewText(R.id.address, headerObj.address);
		remoteViews.setTextViewText(R.id.refresh, headerObj.refreshDateTime);
		remoteViews.setTextViewText(R.id.current_temperature, currentConditionsObj.temp);

		if (currentConditionsObj.realFeelTemp == null) {
			remoteViews.setViewVisibility(R.id.current_realfeel_temperature, View.GONE);
		} else {
			remoteViews.setViewVisibility(R.id.current_realfeel_temperature, View.VISIBLE);
			remoteViews.setTextViewText(R.id.current_realfeel_temperature,
					context.getString(R.string.real_feel_temperature) + ": " + currentConditionsObj.realFeelTemp);
		}
		remoteViews.setTextViewText(R.id.current_airquality, currentConditionsObj.airQuality);
		remoteViews.setTextViewText(R.id.current_precipitation, currentConditionsObj.precipitation);
		remoteViews.setImageViewResource(R.id.current_weather_icon, currentConditionsObj.weatherIcon);

		setWatch(context, remoteViews, currentConditionsObj.timeZone);

		remoteViews.setViewVisibility(R.id.progressbar, View.GONE);
		remoteViews.setViewVisibility(R.id.content_container, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.warning_layout, View.GONE);

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	}
}