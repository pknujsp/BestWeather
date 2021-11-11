package com.lifedawn.bestweather.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.classes.Gps;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAccu;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwm;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;

public class RootAppWidget extends AppWidgetProvider {
	public static final String tag = "appWidget";
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("M.d E HH:mm");

	protected Gps gps = new Gps(null, null, null);
	protected ValueUnits tempUnit;
	protected String tempUnitStr;
	protected static ArrayMap<Integer, ConfigureWidgetActivity.CustomAttributeObj> attributeArrayMap = new ArrayMap<>();
	protected static ArrayMap<Integer, WidgetDataObj> widgetDataObjArrayMap = new ArrayMap<>();

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		Log.e(tag, intent.getAction());
		String action = intent.getAction();

		if (action.equals(context.getString(R.string.ACTION_INIT))) {
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			Bundle bundle = intent.getExtras();
			final int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
			final int layoutId = bundle.getInt(context.getString(R.string.bundle_key_widget_layout_id));
			final Class<?> widgetClass = (Class<?>) bundle.getSerializable(context.getString(R.string.bundle_key_widgetname));

			ConfigureWidgetActivity.CustomAttributeObj customAttributeObj =
					(ConfigureWidgetActivity.CustomAttributeObj) bundle.getSerializable(context.getString(R.string.bundle_key_widget_custom_attributes));
			attributeArrayMap.put(appWidgetId, customAttributeObj);

			WidgetDataObj widgetDataObj = new WidgetDataObj();
			widgetDataObj.locationType = customAttributeObj.locationType;
			widgetDataObj.selectedAddressDto = customAttributeObj.selectedAddressDto;
			widgetDataObjArrayMap.put(appWidgetId, widgetDataObj);

			RemoteViews remoteViews = createViews(context, layoutId);
			remoteViews.setOnClickPendingIntent(R.id.content_container, getOnClickedPendingIntent(context, appWidgetId,
					widgetClass));
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
			init(context, appWidgetId, remoteViews);
		}

		if (tempUnit == null) {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			tempUnit = ValueUnits.enumOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
					ValueUnits.celsius.name()));
			tempUnitStr = ValueUnits.convertToStr(context, tempUnit);
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	public static RemoteViews createViews(Context context, int layoutId) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);
		return remoteViews;
	}

	public static PendingIntent getOnClickedPendingIntent(Context context, int appWidgetId, Class<?> className) {
		Intent intent = new Intent(context, className);
		intent.setAction(context.getString(R.string.ACTION_SHOW_DIALOG));

		Bundle bundle = new Bundle();
		bundle.putSerializable(context.getString(R.string.bundle_key_widgetname), className);
		bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		bundle.putString(context.getString(R.string.bundle_key_address_name), "ss");
		bundle.putSerializable(context.getString(R.string.bundle_key_location_type), LocationType.CurrentLocation);

		intent.putExtras(bundle);
		return PendingIntent.getBroadcast(
				context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public void loadCurrentLocation(Context context, int appWidgetId, RemoteViews remoteViews) {
		remoteViews.setViewVisibility(R.id.progressbar, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.content_container, View.GONE);

		Gps.LocationCallback locationCallback = new Gps.LocationCallback() {
			@Override
			public void onSuccessful(Location location) {
				widgetDataObjArrayMap.get(appWidgetId).location = location;
				widgetDataObjArrayMap.get(appWidgetId).latitude = location.getLatitude();
				widgetDataObjArrayMap.get(appWidgetId).longitude = location.getLongitude();

				Geocoding.geocoding(context, location.getLatitude(), location.getLongitude(), new Geocoding.GeocodingCallback() {
					@Override
					public void onGeocodingResult(List<Address> addressList) {
						widgetDataObjArrayMap.get(appWidgetId).addressName = addressList.get(0).getAddressLine(0);
						widgetDataObjArrayMap.get(appWidgetId).weatherSourceType =
								attributeArrayMap.get(appWidgetId).weatherSourceType;

						String countryCode = addressList.get(0).getCountryCode();
						if (countryCode.equals("KR")) {
							if (attributeArrayMap.get(appWidgetId).topPriorityKma) {
								//widgetDataObjArrayMap.get(appWidgetId).weatherSourceType = WeatherSourceType.KMA;
							}
						}

						Log.e(tag, countryCode);
						Log.e(tag, addressList.get(0).getAddressLine(0));
						loadWeatherData(context, appWidgetId, remoteViews);
					}
				});
			}

			@Override
			public void onFailed(Fail fail) {
				if (fail == Fail.REJECT_PERMISSION) {
					Toast.makeText(context, R.string.message_needs_location_permission, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(context, R.string.request_to_make_gps_on, Toast.LENGTH_SHORT).show();
				}
				remoteViews.setViewVisibility(R.id.progressbar, View.GONE);
				remoteViews.setViewVisibility(R.id.content_container, View.VISIBLE);
			}
		};

		if (gps.checkPermissionAndGpsEnabled(context, locationCallback)) {
			gps.runGps(context, locationCallback);
		}
	}

	public void init(Context context, int appWidgetId, RemoteViews remoteViews) {
		if (widgetDataObjArrayMap.get(appWidgetId).locationType == LocationType.CurrentLocation) {
			loadCurrentLocation(context, appWidgetId, remoteViews);
		} else {
			widgetDataObjArrayMap.get(appWidgetId).addressName = widgetDataObjArrayMap.get(appWidgetId).selectedAddressDto.getAddress();
			widgetDataObjArrayMap.get(appWidgetId).weatherSourceType = attributeArrayMap.get(appWidgetId).weatherSourceType;
			loadWeatherData(context,
					appWidgetId, remoteViews);
		}
	}

	public void loadWeatherData(Context context, int appWidgetId, RemoteViews remoteViews) {
		remoteViews.setViewVisibility(R.id.progressbar, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.content_container, View.GONE);
	}

	public ArrayMap<WeatherSourceType, RequestWeatherSource> makeRequestWeatherSources(WeatherSourceType weatherSourceType) {
		ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources = new ArrayMap<>();
		RequestWeatherSource requestWeatherSource = null;

		if (weatherSourceType == WeatherSourceType.ACCU_WEATHER) {
			requestWeatherSource = new RequestAccu();
		} else if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			requestWeatherSource = new RequestOwm();
		} else if (weatherSourceType == WeatherSourceType.KMA) {
			requestWeatherSource = new RequestKma();
		}
		requestWeatherSources.put(weatherSourceType, requestWeatherSource);
		return requestWeatherSources;
	}

	public void setResultViews(Context context, AppWidgetManager appWidgetManager,
	                           int appWidgetId, RemoteViews remoteViews, @Nullable MultipleJsonDownloader<JsonElement> multipleJsonDownloader) {
		Log.e(tag, "데이터 응답완료");
	}

	public static class WidgetDataObj {
		LocationType locationType;
		String addressName;
		Double latitude;
		Double longitude;
		TimeZone timeZone;
		Location location;
		WeatherSourceType weatherSourceType;
		FavoriteAddressDto selectedAddressDto;
		LocalDateTime updatedDateTime;
	}
}
