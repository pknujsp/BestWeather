package com.lifedawn.bestweather.widget.creator;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.gson.JsonObject;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.forremoteviews.RemoteViewsUtil;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.notification.ongoing.OngoingNotificationReceiver;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.room.repository.WidgetRepository;
import com.lifedawn.bestweather.theme.AppTheme;
import com.lifedawn.bestweather.widget.DialogActivity;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;
import com.lifedawn.bestweather.widget.widgetprovider.BaseAppWidgetProvider;

import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;
import java.util.prefs.PreferenceChangeEvent;

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
	protected AppWidgetManager appWidgetManager;

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

		tempUnit = MyApplication.VALUE_UNIT_OBJ.getTempUnit();
		clockUnit = MyApplication.VALUE_UNIT_OBJ.getClockUnit();
		tempDegree = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();

		widgetRepository = new WidgetRepository(context);
		appWidgetManager = AppWidgetManager.getInstance(context);
	}


	public void setRefreshPendingIntent(Class<?> widgetProviderClass, RemoteViews remoteViews) {
		remoteViews.setOnClickPendingIntent(R.id.refreshBtn, getRefreshPendingIntent(widgetProviderClass));
	}

	public PendingIntent getRefreshPendingIntent(Class<?> widgetProviderClass) {
		Intent refreshIntent = new Intent(context, widgetProviderClass);
		refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));

		if (!pendingIntentCreated(widgetProviderClass)) {
			return PendingIntent.getBroadcast(context, 1500, refreshIntent,
					PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
		} else {
			return PendingIntent.getBroadcast(context, 1500, refreshIntent,
					PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_MUTABLE);
		}
	}

	public boolean pendingIntentCreated(Class<?> widgetProviderClass) {
		Intent refreshIntent = new Intent(context, widgetProviderClass);
		refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1500, refreshIntent,
				PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_MUTABLE);

		if (pendingIntent != null) {
			return true;
		} else {
			return false;
		}
	}

	public WidgetDto loadDefaultSettings() {
		widgetDto = new WidgetDto();
		widgetDto.setAppWidgetId(appWidgetId);
		widgetDto.setMultipleWeatherDataSource(false);
		widgetDto.setWidgetProviderClassName(widgetProviderClass().getName());
		widgetDto.setBackgroundAlpha(100);
		widgetDto.setDisplayClock(true);
		widgetDto.setDisplayLocalClock(false);
		widgetDto.setLocationType(LocationType.CurrentLocation);
		widgetDto.addWeatherProviderType(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(context.getString(R.string.pref_key_met), true) ?
				WeatherProviderType.MET_NORWAY : WeatherProviderType.OWM_ONECALL);
		widgetDto.setTextSizeAmount(0);
		widgetDto.setTopPriorityKma(true);

		if (getRequestWeatherDataTypeSet().contains(WeatherDataType.airQuality)) {
			widgetDto.getWeatherProviderTypeSet().add(WeatherProviderType.AQICN);
		}

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

		return PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
	}

	protected int getWidgetSizeInDp(AppWidgetManager appWidgetManager, String key) {
		return appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(key, 0);
	}

	/**
	 * @param
	 * @return {widgetWidthPx, widgetHeightPx} 반환
	 */
	public int[] getWidgetExactSizeInPx(AppWidgetManager appWidgetManager) {
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

		float density = context.getResources().getDisplayMetrics().density;
		float widgetWidthPx = widgetWidth * density;
		float widgetHeightPx = widgetHeight * density;

		return new int[]{(int) widgetWidthPx, (int) widgetHeightPx};
	}

	public void makeResponseTextToJson(MultipleRestApiDownloader multipleRestApiDownloader, Set<WeatherDataType> weatherDataTypeSet,
	                                   Set<WeatherProviderType> weatherProviderTypeSet, WidgetDto widgetDto, ZoneOffset zoneOffset) {
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
		Map<WeatherProviderType, ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult>> arrayMap =
				multipleRestApiDownloader.getResponseMap();

		final JsonObject rootJsonObject = new JsonObject();
		String text = null;

		//owm이면 onecall이므로 한번만 수행
		for (WeatherProviderType weatherProviderType : weatherProviderTypeSet) {
			ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult> requestWeatherSourceArr =
					arrayMap.get(weatherProviderType);

			if (weatherProviderType == WeatherProviderType.OWM_ONECALL) {
				JsonObject owmJsonObject = new JsonObject();

				text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.OWM_ONE_CALL).getResponseText();
				owmJsonObject.addProperty(RetrofitClient.ServiceType.OWM_ONE_CALL.name(), text);
				rootJsonObject.add(weatherProviderType.name(), owmJsonObject);

			} else if (weatherProviderType == WeatherProviderType.MET_NORWAY) {
				JsonObject metNorwayJsonObject = new JsonObject();

				text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST).getResponseText();
				metNorwayJsonObject.addProperty(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST.name(), text);
				rootJsonObject.add(weatherProviderType.name(), metNorwayJsonObject);

			} else if (weatherProviderType == WeatherProviderType.OWM_INDIVIDUAL) {
				JsonObject owmIndividualJsonObject = new JsonObject();

				if (weatherDataTypeSet.contains(WeatherDataType.currentConditions)) {
					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.OWM_CURRENT_CONDITIONS).getResponseText();
					owmIndividualJsonObject.addProperty(RetrofitClient.ServiceType.OWM_CURRENT_CONDITIONS.name(), text);
				}
				if (weatherDataTypeSet.contains(WeatherDataType.hourlyForecast)) {
					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.OWM_HOURLY_FORECAST).getResponseText();
					owmIndividualJsonObject.addProperty(RetrofitClient.ServiceType.OWM_HOURLY_FORECAST.name(), text);
				}
				if (weatherDataTypeSet.contains(WeatherDataType.dailyForecast)) {
					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.OWM_DAILY_FORECAST).getResponseText();
					owmIndividualJsonObject.addProperty(RetrofitClient.ServiceType.OWM_DAILY_FORECAST.name(), text);
				}
				rootJsonObject.add(weatherProviderType.name(), owmIndividualJsonObject);

			} else if (weatherProviderType == WeatherProviderType.KMA_API) {
				JsonObject kmaJsonObject = new JsonObject();

				if (weatherDataTypeSet.contains(WeatherDataType.currentConditions)) {
					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST).getResponseText();
					kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST.name(), text);

					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST).getResponseText();
					kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST.name(), text);
				}
				if (weatherDataTypeSet.contains(WeatherDataType.hourlyForecast)) {
					if (!kmaJsonObject.has(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST.name())) {
						text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST).getResponseText();
						kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST.name(), text);
					}
					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.KMA_VILAGE_FCST).getResponseText();
					kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_VILAGE_FCST.name(), text);
				}
				if (weatherDataTypeSet.contains(WeatherDataType.dailyForecast)) {
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
				rootJsonObject.add(weatherProviderType.name(), kmaJsonObject);

			} else if (weatherProviderType == WeatherProviderType.KMA_WEB) {
				JsonObject kmaJsonObject = new JsonObject();

				if (weatherDataTypeSet.contains(WeatherDataType.currentConditions)) {
					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS).getResponseText();
					kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS.name(), text);
					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS).getResponseText();
					kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_WEB_FORECASTS.name(), text);
				}
				if (!kmaJsonObject.has(RetrofitClient.ServiceType.KMA_WEB_FORECASTS.name())) {
					if (weatherDataTypeSet.contains(WeatherDataType.hourlyForecast) || weatherDataTypeSet.contains(WeatherDataType.dailyForecast)) {
						text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS).getResponseText();
						kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_WEB_FORECASTS.name(), text);
					}
				}
				rootJsonObject.add(weatherProviderType.name(), kmaJsonObject);

			} else if (weatherProviderType == WeatherProviderType.ACCU_WEATHER) {
				JsonObject accuJsonObject = new JsonObject();

				if (weatherDataTypeSet.contains(WeatherDataType.currentConditions)) {
					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS).getResponseText();
					accuJsonObject.addProperty(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS.name(), text);
				}
				if (weatherDataTypeSet.contains(WeatherDataType.hourlyForecast)) {
					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST).getResponseText();
					accuJsonObject.addProperty(RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST.name(), text);
				}
				if (weatherDataTypeSet.contains(WeatherDataType.dailyForecast)) {
					text = requestWeatherSourceArr.get(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST).getResponseText();
					accuJsonObject.addProperty(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST.name(), text);
				}
				rootJsonObject.add(weatherProviderType.name(), accuJsonObject);

			} else if (weatherProviderType == WeatherProviderType.AQICN) {
				JsonObject aqiCnJsonObject = new JsonObject();

				if (weatherDataTypeSet.contains(WeatherDataType.airQuality)) {
					text = arrayMap.get(WeatherProviderType.AQICN).get(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED).getResponseText();
					if (text != null && !text.equals("{}")) {
						aqiCnJsonObject.addProperty(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED.name(), text);
					}
				}
				rootJsonObject.add(weatherProviderType.name(), aqiCnJsonObject);
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

		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);
		RemoteViewsUtil.onBeginProcess(remoteViews);
		return remoteViews;
	}

	public void setResultViews(int appWidgetId, RemoteViews remoteViews, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader) {
		if (!widgetDto.isInitialized()) {
			widgetDto.setInitialized(true);
		}

		if (widgetDto.isLoadSuccessful()) {
			RemoteViewsUtil.onSuccessfulProcess(remoteViews);
			widgetDto.setLastErrorType(null);
		} else {
			if (widgetDto.getLastErrorType() == null) {
				widgetDto.setLastErrorType(RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA);
			}

			RemoteViewsUtil.onErrorProcess(remoteViews, context, RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA);
			setRefreshPendingIntent(widgetProviderClass(), remoteViews);
		}

		widgetRepository.update(widgetDto, new DbQueryCallback<WidgetDto>() {
			@Override
			public void onResultSuccessful(WidgetDto result) {
				appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
			}

			@Override
			public void onResultNoData() {

			}
		});
	}

	public RelativeLayout.LayoutParams getHeaderViewLayoutParams() {
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f,
				context.getResources().getDisplayMetrics());

		return layoutParams;
	}

	abstract public RemoteViews createRemoteViews();

	abstract public Class<?> widgetProviderClass();

	abstract public void setTextSize(int amount);

	abstract public void setDisplayClock(boolean displayClock);

	abstract public void setDataViewsOfSavedData();

	abstract public Set<WeatherDataType> getRequestWeatherDataTypeSet();

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
