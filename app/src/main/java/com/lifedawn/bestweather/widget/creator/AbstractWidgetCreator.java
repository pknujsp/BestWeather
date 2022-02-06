package com.lifedawn.bestweather.widget.creator;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.gson.JsonObject;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.room.repository.WidgetRepository;
import com.lifedawn.bestweather.theme.AppTheme;
import com.lifedawn.bestweather.widget.DialogActivity;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;

import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;

import static android.view.View.MeasureSpec.EXACTLY;

public abstract class AbstractWidgetCreator {
	protected final int appWidgetId;
	protected final ValueUnits tempUnit;
	protected final ValueUnits clockUnit;
	protected final String tempDegree;

	protected Context context;

	protected WidgetUpdateCallback widgetUpdateCallback;
	protected WidgetDto widgetDto;
	protected WidgetRepository widgetRepository;

	public AbstractWidgetCreator setWidgetDto(WidgetDto widgetDto) {
		this.widgetDto = widgetDto;
		setTextSize(widgetDto.getTextSizeAmount());
		return this;
	}

	public abstract RemoteViews createTempViews(Integer parentWidth, Integer parentHeight);

	public Context getContext() {
		return context;
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
		tempDegree = ValueUnits.convertToStr(null, tempUnit);

		widgetRepository = new WidgetRepository(context);
	}


	public void setRefreshPendingIntent(Class<?> widgetProviderClass, RemoteViews remoteViews, int appWidgetId) {
		Intent refreshIntent = new Intent(context, widgetProviderClass);
		refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));

		Bundle bundle = new Bundle();
		bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		refreshIntent.putExtras(bundle);

		remoteViews.setOnClickPendingIntent(R.id.refreshBtn, PendingIntent.getBroadcast(context, appWidgetId, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT));
	}

	public WidgetDto loadDefaultSettings() {
		widgetDto = new WidgetDto();
		widgetDto.setAppWidgetId(appWidgetId);
		widgetDto.setMultipleWeatherDataSource(false);
		widgetDto.setBackgroundAlpha(100);
		widgetDto.setDisplayClock(true);
		widgetDto.setDisplayLocalClock(false);
		widgetDto.setLocationType(LocationType.CurrentLocation);
		widgetDto.addWeatherSourceType(WeatherDataSourceType.OWM_ONECALL);
		widgetDto.setTextSizeAmount(0);
		widgetDto.setTopPriorityKma(true);
		widgetDto.setUpdateIntervalMillis(0);

		setTextSize(widgetDto.getTextSizeAmount());
		return widgetDto;
	}

	public void loadSavedSettings(@Nullable DbQueryCallback<WidgetDto> callback) {
		widgetRepository.get(appWidgetId, new DbQueryCallback<WidgetDto>() {
			@Override
			public void onResultSuccessful(WidgetDto result) {
				widgetDto = result;

				if (widgetDto != null) {
					setTextSize(widgetDto.getTextSizeAmount());
				}
				if (callback != null) {
					callback.onResultSuccessful(widgetDto);
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


	public PendingIntent getOnClickedPendingIntent() {
		Intent intent = new Intent(context, DialogActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		Bundle bundle = new Bundle();
		bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
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

	public void makeResponseTextToJson(MultipleRestApiDownloader multipleRestApiDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet,
	                                   Set<WeatherDataSourceType> weatherDataSourceTypeSet, WidgetDto widgetDto, ZoneOffset zoneOffset) {
		//json형태로 저장
		/*
		examples :

		{
			"KMA":{
				"ULTRA_SRT_NCST": ~~json text~~,
				"ULTRA_SRT_FCST": ~~json text~~
			},
			"OPEN_WEATHER_MAP":{
				"OWM_ONE_CALL": ~~json text~~
			},
			"zoneOffset": "09:00"
		}

		 */
		Map<WeatherDataSourceType, ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult>> arrayMap =
				multipleRestApiDownloader.getResponseMap();

		final JsonObject rootJsonObject = new JsonObject();
		String text = null;

		//owm이면 onecall이므로 한번만 수행
		for (WeatherDataSourceType weatherDataSourceType : weatherDataSourceTypeSet) {
			ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult> requestWeatherSourceArr =
					arrayMap.get(weatherDataSourceType);

			if (weatherDataSourceType == WeatherDataSourceType.OWM_ONECALL) {
				JsonObject owmJsonObject = new JsonObject();

				text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.OWM_ONE_CALL).getResponseText();
				owmJsonObject.addProperty(RetrofitClient.ServiceType.OWM_ONE_CALL.name(), text);
				rootJsonObject.add(weatherDataSourceType.name(), owmJsonObject);

			} else if (weatherDataSourceType == WeatherDataSourceType.OWM_INDIVIDUAL) {
				JsonObject owmIndividualJsonObject = new JsonObject();

				if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.currentConditions)) {
					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.OWM_CURRENT_CONDITIONS).getResponseText();
					owmIndividualJsonObject.addProperty(RetrofitClient.ServiceType.OWM_CURRENT_CONDITIONS.name(), text);
				}
				if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.hourlyForecast)) {
					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.OWM_HOURLY_FORECAST).getResponseText();
					owmIndividualJsonObject.addProperty(RetrofitClient.ServiceType.OWM_HOURLY_FORECAST.name(), text);
				}
				if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.dailyForecast)) {
					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.OWM_DAILY_FORECAST).getResponseText();
					owmIndividualJsonObject.addProperty(RetrofitClient.ServiceType.OWM_DAILY_FORECAST.name(), text);
				}
				rootJsonObject.add(weatherDataSourceType.name(), owmIndividualJsonObject);

			} else if (weatherDataSourceType == WeatherDataSourceType.KMA_API) {
				JsonObject kmaJsonObject = new JsonObject();

				if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.currentConditions)) {
					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST).getResponseText();
					kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST.name(), text);

					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST).getResponseText();
					kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST.name(), text);
				}
				if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.hourlyForecast)) {
					if (!kmaJsonObject.has(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST.name())) {
						text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST).getResponseText();
						kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST.name(), text);
					}
					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.KMA_VILAGE_FCST).getResponseText();
					kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_VILAGE_FCST.name(), text);
				}
				if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.dailyForecast)) {
					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.KMA_MID_TA_FCST).getResponseText();
					kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_MID_TA_FCST.name(), text);

					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.KMA_MID_LAND_FCST).getResponseText();
					kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_MID_LAND_FCST.name(), text);

					if (!kmaJsonObject.has(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST.name())) {
						text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST).getResponseText();
						kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST.name(), text);
					}
					if (!kmaJsonObject.has(RetrofitClient.ServiceType.KMA_VILAGE_FCST.name())) {
						text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.KMA_VILAGE_FCST).getResponseText();
						kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_VILAGE_FCST.name(), text);
					}
					long tmFc = Long.parseLong(multipleRestApiDownloader.get("tmFc"));
					kmaJsonObject.addProperty("tmFc", tmFc);
				}
				rootJsonObject.add(weatherDataSourceType.name(), kmaJsonObject);

			} else if (weatherDataSourceType == WeatherDataSourceType.KMA_WEB) {
				JsonObject kmaJsonObject = new JsonObject();

				if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.currentConditions)) {
					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS).getResponseText();
					kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS.name(), text);
					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS).getResponseText();
					kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_WEB_FORECASTS.name(), text);
				}
				if (!kmaJsonObject.has(RetrofitClient.ServiceType.KMA_WEB_FORECASTS.name())) {
					if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.hourlyForecast) || requestWeatherDataTypeSet.contains(RequestWeatherDataType.dailyForecast)) {
						text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS).getResponseText();
						kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_WEB_FORECASTS.name(), text);
					}
				}
				rootJsonObject.add(weatherDataSourceType.name(), kmaJsonObject);

			} else if (weatherDataSourceType == WeatherDataSourceType.ACCU_WEATHER) {
				JsonObject accuJsonObject = new JsonObject();

				if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.currentConditions)) {
					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS).getResponseText();
					accuJsonObject.addProperty(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS.name(), text);
				}
				if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.hourlyForecast)) {
					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST).getResponseText();
					accuJsonObject.addProperty(RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST.name(), text);
				}
				if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.dailyForecast)) {
					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST).getResponseText();
					accuJsonObject.addProperty(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST.name(), text);
				}
				rootJsonObject.add(weatherDataSourceType.name(), accuJsonObject);

			} else if (weatherDataSourceType == WeatherDataSourceType.AQICN) {
				JsonObject aqiCnJsonObject = new JsonObject();

				if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.airQuality)) {
					text = arrayMap.get(WeatherDataSourceType.AQICN).get(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED).getResponseText();
					aqiCnJsonObject.addProperty(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED.name(), text);
				}

				rootJsonObject.add(weatherDataSourceType.name(), aqiCnJsonObject);
			}
		}

		if (zoneOffset != null) {
			rootJsonObject.addProperty("zoneOffset", zoneOffset.getId());
		}
		rootJsonObject.addProperty("lastUpdatedDateTime", multipleRestApiDownloader.getRequestDateTime().toString());

		widgetDto.setResponseText(rootJsonObject.toString());
	}

	public void drawBitmap(RemoteViews remoteViews, Bitmap bitmap) {
		remoteViews.setImageViewBitmap(R.id.valuesView, bitmap);
	}

	protected final Bitmap drawBitmap(ViewGroup rootLayout, @Nullable OnDrawBitmapCallback onDrawBitmapCallback, RemoteViews remoteViews,
	                                  @Nullable Integer parentWidth, @Nullable Integer parentHeight) {
		int widthSize = 0;
		int heightSize = 0;

		if (parentWidth == null || parentHeight == null) {
			int[] widgetSize = getWidgetExactSizeInPx(AppWidgetManager.getInstance(context));

			widthSize = widgetSize[0];
			heightSize = widgetSize[1];
		} else {
			widthSize = parentWidth;
			heightSize = parentHeight;
		}
		final float widgetPadding = context.getResources().getDimension(R.dimen.widget_padding);

		final int widthSpec = View.MeasureSpec.makeMeasureSpec((int) (widthSize - widgetPadding * 2), EXACTLY);
		final int heightSpec = View.MeasureSpec.makeMeasureSpec((int) (heightSize - widgetPadding * 2), EXACTLY);

		rootLayout.measure(widthSpec, heightSpec);

		Bitmap viewBmp = drawBitmap(rootLayout, remoteViews);
		if (onDrawBitmapCallback != null) {
			onDrawBitmapCallback.onCreatedBitmap(viewBmp);
		}

		return viewBmp;
	}

	protected final Bitmap drawBitmap(ViewGroup rootLayout, RemoteViews remoteViews) {
		rootLayout.layout(0, 0, rootLayout.getMeasuredWidth(), rootLayout.getMeasuredHeight());

		rootLayout.setDrawingCacheEnabled(true);
		rootLayout.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

		Bitmap viewBmp = rootLayout.getDrawingCache();
		remoteViews.setImageViewBitmap(R.id.valuesView, viewBmp);
		return viewBmp;
	}

	protected final RemoteViews createBaseRemoteViews() {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int layoutId = appWidgetManager.getAppWidgetInfo(appWidgetId).initialLayout;
		return new RemoteViews(context.getPackageName(), layoutId);
	}

	abstract public RemoteViews createRemoteViews();

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
