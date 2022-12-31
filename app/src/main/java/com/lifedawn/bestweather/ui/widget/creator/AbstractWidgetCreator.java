package com.lifedawn.bestweather.ui.widget.creator;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.gson.JsonObject;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.constants.IntentRequestCodes;
import com.lifedawn.bestweather.commons.constants.LocationType;
import com.lifedawn.bestweather.commons.constants.WeatherDataType;
import com.lifedawn.bestweather.commons.constants.ValueUnits;
import com.lifedawn.bestweather.commons.constants.WeatherProviderType;
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil;
import com.lifedawn.bestweather.data.MyApplication;
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.data.remote.retrofit.callback.WeatherRestApiDownloader;
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.data.local.room.dto.WidgetDto;
import com.lifedawn.bestweather.data.local.room.repository.WidgetRepository;
import com.lifedawn.bestweather.ui.theme.AppTheme;
import com.lifedawn.bestweather.ui.widget.DialogActivity;
import com.lifedawn.bestweather.ui.widget.OnDrawBitmapCallback;
import com.lifedawn.bestweather.ui.widget.widgetprovider.EighthWidgetProvider;
import com.lifedawn.bestweather.ui.widget.widgetprovider.EleventhWidgetProvider;
import com.lifedawn.bestweather.ui.widget.widgetprovider.FifthWidgetProvider;
import com.lifedawn.bestweather.ui.widget.widgetprovider.FirstWidgetProvider;
import com.lifedawn.bestweather.ui.widget.widgetprovider.FourthWidgetProvider;
import com.lifedawn.bestweather.ui.widget.widgetprovider.NinthWidgetProvider;
import com.lifedawn.bestweather.ui.widget.widgetprovider.SecondWidgetProvider;
import com.lifedawn.bestweather.ui.widget.widgetprovider.SeventhWidgetProvider;
import com.lifedawn.bestweather.ui.widget.widgetprovider.SixthWidgetProvider;
import com.lifedawn.bestweather.ui.widget.widgetprovider.TenthWidgetProvider;
import com.lifedawn.bestweather.ui.widget.widgetprovider.ThirdWidgetProvider;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static android.view.View.MeasureSpec.EXACTLY;

public abstract class AbstractWidgetCreator {
	protected final DateTimeFormatter refreshDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a h:mm");

	protected final int appWidgetId;
	protected final ValueUnits tempUnit;
	protected final ValueUnits clockUnit;
	protected final String tempDegree;
	protected ZoneId zoneId;

	protected Context context;

	protected WidgetUpdateCallback widgetUpdateCallback;
	protected WidgetDto widgetDto;
	protected WidgetRepository widgetRepository;
	protected AppWidgetManager appWidgetManager;

	protected RemoteViewsCallback remoteViewsCallback;

	public AbstractWidgetCreator setRemoteViewsCallback(RemoteViewsCallback remoteViewsCallback) {
		this.remoteViewsCallback = remoteViewsCallback;
		return this;
	}

	public AbstractWidgetCreator setWidgetDto(WidgetDto widgetDto) {
		this.widgetDto = widgetDto;
		return this;
	}

	public static AbstractWidgetCreator getInstance(AppWidgetManager appWidgetManager, Context context, int appWidgetId) {
		AppWidgetProviderInfo appWidgetProviderInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
		final String providerClassName = appWidgetProviderInfo.provider.getClassName();

		if (providerClassName.equals(FirstWidgetProvider.class.getName())) {
			return new FirstWidgetCreator(context, null, appWidgetId);
		} else if (providerClassName.equals(SecondWidgetProvider.class.getName())) {
			return new SecondWidgetCreator(context, null, appWidgetId);
		} else if (providerClassName.equals(ThirdWidgetProvider.class.getName())) {
			return new ThirdWidgetCreator(context, null, appWidgetId);
		} else if (providerClassName.equals(FourthWidgetProvider.class.getName())) {
			return new FourthWidgetCreator(context, null, appWidgetId);
		} else if (providerClassName.equals(FifthWidgetProvider.class.getName())) {
			return new FifthWidgetCreator(context, null, appWidgetId);
		} else if (providerClassName.equals(SixthWidgetProvider.class.getName())) {
			return new SixthWidgetCreator(context, null, appWidgetId);
		} else if (providerClassName.equals(SeventhWidgetProvider.class.getName())) {
			return new SeventhWidgetCreator(context, null, appWidgetId);
		} else if (providerClassName.equals(EighthWidgetProvider.class.getName())) {
			return new EighthWidgetCreator(context, null, appWidgetId);
		} else if (providerClassName.equals(NinthWidgetProvider.class.getName())) {
			return new NinthWidgetCreator(context, null, appWidgetId);
		} else if (providerClassName.equals(TenthWidgetProvider.class.getName())) {
			return new TenthWidgetCreator(context, null, appWidgetId);
		} else if (providerClassName.equals(EleventhWidgetProvider.class.getName())) {
			return new EleventhWidgetCreator(context, null, appWidgetId);
		} else {
			return null;
		}
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

		widgetRepository = WidgetRepository.getINSTANCE();
		appWidgetManager = AppWidgetManager.getInstance(context);
	}


	public void setRefreshPendingIntent(RemoteViews remoteViews) {
		remoteViews.setOnClickPendingIntent(R.id.refreshBtn, getRefreshPendingIntent());
	}

	public PendingIntent getRefreshPendingIntent() {
		Intent refreshIntent = new Intent(context, FirstWidgetProvider.class);
		refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));

		return PendingIntent.getBroadcast(context, IntentRequestCodes.WIDGET_MANUALLY_REFRESH.requestCode, refreshIntent,
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
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

		return widgetDto;
	}

	public void loadSavedSettings(@Nullable DbQueryCallback<WidgetDto> callback) {
		widgetRepository.get(appWidgetId, new DbQueryCallback<WidgetDto>() {
			@Override
			public void onResultSuccessful(WidgetDto result) {
				widgetDto = result;

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
		remoteViews.setInt(R.id.root_layout, "setBackgroundColor", newBackgroundColor);
	}


	public PendingIntent getOnClickedPendingIntent() {
		Intent intent = new Intent(context, DialogActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		Bundle bundle = new Bundle();
		bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		intent.putExtras(bundle);

		return PendingIntent.getActivity(context, IntentRequestCodes.CLICK_WIDGET.requestCode, intent,
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
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

	public void makeResponseTextToJson(WeatherRestApiDownloader weatherRestApiDownloader, Set<WeatherDataType> weatherDataTypeSet,
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
		Map<WeatherProviderType, ArrayMap<RetrofitClient.ServiceType, WeatherRestApiDownloader.ResponseResult>> arrayMap =
				weatherRestApiDownloader.getResponseMap();

		final JsonObject rootJsonObject = new JsonObject();
		String text = null;

		//owm이면 onecall이므로 한번만 수행
		for (WeatherProviderType weatherProviderType : weatherProviderTypeSet) {
			ArrayMap<RetrofitClient.ServiceType, WeatherRestApiDownloader.ResponseResult> requestWeatherSourceArr =
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
					long tmFc = Long.parseLong(weatherRestApiDownloader.get("tmFc"));
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

		rootJsonObject.addProperty("lastUpdatedDateTime", weatherRestApiDownloader.getRequestDateTime().toString());
		widgetDto.setResponseText(rootJsonObject.toString());
	}


	protected final void drawBitmap(ViewGroup rootLayout, @Nullable OnDrawBitmapCallback onDrawBitmapCallback, RemoteViews remoteViews,
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

	}

	protected final Bitmap drawBitmap(ViewGroup rootLayout, RemoteViews remoteViews) {
		rootLayout.layout(0, 0, rootLayout.getMeasuredWidth(), rootLayout.getMeasuredHeight());

		rootLayout.setDrawingCacheEnabled(false);
		rootLayout.destroyDrawingCache();

		rootLayout.setDrawingCacheEnabled(true);
		rootLayout.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

		rootLayout.buildDrawingCache();

		Bitmap viewBmp = rootLayout.getDrawingCache();

		remoteViews.setImageViewBitmap(R.id.bitmapValuesView, viewBmp);

		return viewBmp;
	}

	protected final RemoteViews createBaseRemoteViews() {
		return new RemoteViews(context.getPackageName(), R.layout.view_widget);
	}

	public void setResultViews(int appWidgetId,
	                           @Nullable @org.jetbrains.annotations.Nullable WeatherRestApiDownloader weatherRestApiDownloader,
	                           @Nullable ZoneId zoneId) {
		if (!widgetDto.isInitialized())
			widgetDto.setInitialized(true);

		if (widgetDto.isLoadSuccessful()) {
			widgetDto.setLastErrorType(null);
		} else {
			if (widgetDto.getLastErrorType() == null)
				widgetDto.setLastErrorType(RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA);
		}

		widgetRepository.update(widgetDto, new DbQueryCallback<WidgetDto>() {
			@Override
			public void onResultSuccessful(WidgetDto result) {
				remoteViewsCallback.onResult(result.getAppWidgetId(), createRemoteViews());
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

	protected View makeHeaderViews(LayoutInflater layoutInflater, String addressName, String lastRefreshDateTime) {
		View view = layoutInflater.inflate(R.layout.header_view_in_widget, null, false);
		((TextView) view.findViewById(R.id.address)).setText(addressName);
		((TextView) view.findViewById(R.id.refresh)).setText(ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));

		return view;
	}

	public RemoteViews createRemoteViews() {
		RemoteViews remoteViews = createBaseRemoteViews();
		remoteViews.setOnClickPendingIntent(R.id.root_layout, getOnClickedPendingIntent());
		return remoteViews;
	}

	abstract public Class<?> widgetProviderClass();

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

	public static abstract class RemoteViewsCallback {
		private final int requestCount;
		private final AtomicInteger responseCount = new AtomicInteger(0);
		private final Map<Integer, RemoteViews> remoteViewsMap = new ConcurrentHashMap<>();

		public RemoteViewsCallback(int requestCount) {
			this.requestCount = requestCount;
		}

		private void onResult(int id, RemoteViews remoteViews) {
			remoteViewsMap.put(id, remoteViews);
			if (responseCount.incrementAndGet() == requestCount)
				onFinished(remoteViewsMap);
		}

		abstract protected void onFinished(Map<Integer, RemoteViews> remoteViewsMap);
	}
}
