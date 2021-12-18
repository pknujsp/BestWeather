package com.lifedawn.bestweather.widget.creator;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.ArrayMap;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.gson.JsonObject;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.room.repository.WidgetRepository;
import com.lifedawn.bestweather.theme.AppTheme;
import com.lifedawn.bestweather.widget.DialogActivity;

import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;

public abstract class AbstractWidgetCreator {
	protected final int appWidgetId;
	protected final ValueUnits tempUnit;
	protected final ValueUnits clockUnit;
	protected final String tempDegree = "°";

	protected Context context;

	protected WidgetUpdateCallback widgetUpdateCallback;
	protected WidgetDto widgetDto;
	protected WidgetRepository widgetRepository;

	public AbstractWidgetCreator setWidgetDto(WidgetDto widgetDto) {
		this.widgetDto = widgetDto;
		setTextSize(widgetDto.getTextSizeAmount());
		return this;
	}

	public AbstractWidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
		this.context = context;
		this.widgetUpdateCallback = widgetUpdateCallback;
		this.appWidgetId = appWidgetId;

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		tempUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
				ValueUnits.celsius.name()));
		clockUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_clock),
				ValueUnits.clock12.name()));

		widgetRepository = new WidgetRepository(context);
	}


	public WidgetDto loadDefaultSettings() {
		widgetDto = new WidgetDto();
		widgetDto.setAppWidgetId(appWidgetId);
		widgetDto.setBackgroundAlpha(100);
		widgetDto.setDisplayClock(true);
		widgetDto.setDisplayLocalClock(false);
		widgetDto.setLocationType(LocationType.CurrentLocation.name());
		widgetDto.setWeatherSourceType(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_key_accu_weather), false)
				? WeatherSourceType.ACCU_WEATHER.name() : WeatherSourceType.OPEN_WEATHER_MAP.name());
		widgetDto.setTextSizeAmount(0);
		widgetDto.setTopPriorityKma(false);
		widgetDto.setUpdateIntervalMillis(0);

		setTextSize(widgetDto.getTextSizeAmount());

		return widgetDto;
	}

	public void loadSavedSettings(@Nullable DbQueryCallback<WidgetDto> callback) {
		widgetRepository.get(appWidgetId, new DbQueryCallback<WidgetDto>() {
			@Override
			public void onResultSuccessful(WidgetDto result) {
				widgetDto = result;

				if (widgetDto == null) {
					return;
				}

				setTextSize(widgetDto.getTextSizeAmount());
				if (callback != null) {
					callback.onResultSuccessful(result);
				}
			}

			@Override
			public void onResultNoData() {

			}
		});
	}

	public void saveSettings(WidgetDto widgetDto, @Nullable DbQueryCallback<WidgetDto> callback) {
		widgetRepository.add(widgetDto, new DbQueryCallback<WidgetDto>() {
			@Override
			public void onResultSuccessful(WidgetDto result) {
				if (callback != null) {
					callback.onResultSuccessful(result);
				}
			}

			@Override
			public void onResultNoData() {

			}
		});
	}

	public void updateSettings(WidgetDto widgetDto, @Nullable DbQueryCallback<WidgetDto> callback) {
		widgetRepository.update(widgetDto, new DbQueryCallback<WidgetDto>() {
			@Override
			public void onResultSuccessful(WidgetDto result) {
				if (callback != null) {
					callback.onResultSuccessful(result);
				}
			}

			@Override
			public void onResultNoData() {

			}
		});
	}

	protected void setBackgroundAlpha(RemoteViews remoteViews, int backgroundAlpha) {
		float opacity = widgetDto.getBackgroundAlpha() / 100f;
		int newBackgroundColor = (int) (opacity * 0xFF) << 24 | AppTheme.getColor(context, R.color.widgetBackgroundColor);
		remoteViews.setInt(R.id.content_container, "setBackgroundColor", newBackgroundColor);
	}


	public PendingIntent getOnClickedPendingIntent(RemoteViews remoteViews) {
		Intent intent = new Intent(context, DialogActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		Bundle bundle = new Bundle();
		bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		bundle.putParcelable(WidgetNotiConstants.WidgetAttributes.REMOTE_VIEWS.name(), remoteViews);
		intent.putExtras(bundle);

		return PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	protected int getWidgetSizeInDp(AppWidgetManager appWidgetManager, String key) {
		return appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(key, 0);
	}

	/**
	 * @param
	 * @return {widgetWidthPx, widgetHeightPx} 반환
	 */
	protected int[] getWidgetExactSizeInPx(AppWidgetManager appWidgetManager) {
		int widgetWidth = 0;
		int widgetHeight = 0;

		int portrait = context.getResources().getConfiguration().orientation;
		if (portrait == Configuration.ORIENTATION_PORTRAIT) {
			widgetWidth = getWidgetSizeInDp(appWidgetManager, AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
			widgetHeight = getWidgetSizeInDp(appWidgetManager, AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
		} else {
			widgetWidth = getWidgetSizeInDp(appWidgetManager, AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
			widgetHeight = getWidgetSizeInDp(appWidgetManager, AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
		}

		final float density = context.getResources().getDisplayMetrics().density;
		final float widgetWidthPx = widgetWidth * density;
		final float widgetHeightPx = widgetHeight * density;

		return new int[]{(int) widgetWidthPx, (int) widgetHeightPx};
	}

	public void makeResponseTextToJson(MultipleJsonDownloader multipleJsonDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet,
	                                   WeatherSourceType weatherSourceType, WidgetDto widgetDto, ZoneOffset zoneOffset) {
		//json형태로 저장
		Map<WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult>> arrayMap =
				multipleJsonDownloader.getResponseMap();
		ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult> requestWeatherSourceArr =
				arrayMap.get(weatherSourceType);

		final JsonObject jsonObject = new JsonObject();

		//owm이면 onecall이므로 한번만 수행
		if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			String text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.OWM_ONE_CALL).getResponseText();
			jsonObject.addProperty(RetrofitClient.ServiceType.OWM_ONE_CALL.name(), text);
		} else {
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.currentConditions)) {
				if (weatherSourceType == WeatherSourceType.KMA) {
					String text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.ULTRA_SRT_NCST).getResponseText();
					jsonObject.addProperty(RetrofitClient.ServiceType.ULTRA_SRT_NCST.name(), text);

				} else if (weatherSourceType == WeatherSourceType.ACCU_WEATHER) {
					String text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS).getResponseText();
					jsonObject.addProperty(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS.name(), text);
				}
			}

			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.hourlyForecast)) {
				if (weatherSourceType == WeatherSourceType.KMA) {
					String text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.ULTRA_SRT_FCST).getResponseText();
					jsonObject.addProperty(RetrofitClient.ServiceType.ULTRA_SRT_FCST.name(), text);

					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.VILAGE_FCST).getResponseText();
					jsonObject.addProperty(RetrofitClient.ServiceType.VILAGE_FCST.name(), text);
				} else if (weatherSourceType == WeatherSourceType.ACCU_WEATHER) {
					String text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.ACCU_12_HOURLY).getResponseText();
					jsonObject.addProperty(RetrofitClient.ServiceType.ACCU_12_HOURLY.name(), text);
				}
			}

			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.dailyForecast)) {
				if (weatherSourceType == WeatherSourceType.KMA) {
					String text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.MID_TA_FCST).getResponseText();
					jsonObject.addProperty(RetrofitClient.ServiceType.MID_TA_FCST.name(), text);

					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.MID_LAND_FCST).getResponseText();
					jsonObject.addProperty(RetrofitClient.ServiceType.MID_LAND_FCST.name(), text);

					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.ULTRA_SRT_FCST).getResponseText();
					jsonObject.addProperty(RetrofitClient.ServiceType.ULTRA_SRT_FCST.name(), text);

					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.VILAGE_FCST).getResponseText();
					jsonObject.addProperty(RetrofitClient.ServiceType.VILAGE_FCST.name(), text);

					long tmFc = Long.parseLong(multipleJsonDownloader.get("tmFc"));
					jsonObject.addProperty("tmFc", tmFc);
				} else if (weatherSourceType == WeatherSourceType.ACCU_WEATHER) {
					String text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY).getResponseText();
					jsonObject.addProperty(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY.name(), text);
				}
			}
		}

		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.airQuality)) {
			String text = arrayMap.get(WeatherSourceType.AQICN).get(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED).getResponseText();
			jsonObject.addProperty(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED.name(), text);
		}

		if (zoneOffset != null) {
			jsonObject.addProperty("zoneOffset", zoneOffset.getId());
		}
		widgetDto.setResponseText(jsonObject.toString());
	}

	public void drawBitmap(RemoteViews remoteViews, Bitmap bitmap) {
		remoteViews.setImageViewBitmap(R.id.valuesView, bitmap);
	}

	abstract public RemoteViews createRemoteViews(boolean needTempData);

	abstract public void setTextSize(int amount);

	abstract public void setDisplayClock(boolean displayClock);

	abstract public void setDataViewsOfSavedData();


	public WidgetDto getWidgetDto() {
		return widgetDto;
	}

	public int getAppWidgetId() {
		return appWidgetId;
	}

	public interface WidgetUpdateCallback {
		void updatePreview();
	}
}
