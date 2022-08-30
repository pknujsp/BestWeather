package com.lifedawn.bestweather.notification.ongoing;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.os.PowerManager;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.LocationResult;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.commons.interfaces.Callback;
import com.lifedawn.bestweather.forremoteviews.RemoteViewsUtil;

import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.notification.NotificationUpdateCallback;
import com.lifedawn.bestweather.notification.model.NotificationDataObj;
import com.lifedawn.bestweather.notification.model.OngoingNotiDataObj;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherRequestUtil;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherUtil;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class OngoingNotiViewCreator {
	private final int hourlyForecastCount = 8;
	private Callback callback;

	private final NotificationUpdateCallback notificationUpdateCallback;
	private final ValueUnits windSpeedUnit;
	private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a h:mm");
	private final ValueUnits tempUnit;
	private final String tempDegree;
	private final NotificationType notificationType = NotificationType.Ongoing;

	private Context context;
	private NotificationHelper notificationHelper;
	private NotificationDataObj notificationDataObj;
	private Timer timer;

	public NotificationDataObj getNotificationDataObj() {
		return notificationDataObj;
	}

	public OngoingNotiViewCreator(Context context, NotificationUpdateCallback notificationUpdateCallback) {
		this.context = context;
		this.notificationUpdateCallback = notificationUpdateCallback;

		tempUnit = MyApplication.VALUE_UNIT_OBJ.getTempUnit();
		tempDegree = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();
		windSpeedUnit = MyApplication.VALUE_UNIT_OBJ.getWindUnit();
		notificationHelper = new NotificationHelper(context);
	}

	public void loadCurrentLocation(RemoteViews collapsedRemoteViews, RemoteViews expandedRemoteViews) {
		FusedLocation fusedLocation = FusedLocation.getInstance(context);

		final FusedLocation.MyLocationCallback locationCallback = new FusedLocation.MyLocationCallback() {
			@Override
			public void onSuccessful(LocationResult locationResult) {
				final Location location = getBestLocation(locationResult);
				Geocoding.geocoding(context, location.getLatitude(), location.getLongitude(), new Geocoding.GeocodingCallback() {
					@Override
					public void onGeocodingResult(List<Address> addressList) {
						final SharedPreferences sharedPreferences =
								context.getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = sharedPreferences.edit();
						Address address = addressList.get(0);
						notificationDataObj.setAddressName(address.getAddressLine(0))
								.setCountryCode(address.getCountryCode())
								.setLatitude(address.getLatitude()).setLongitude(address.getLongitude())
								.setAdmin(address.getAdminArea());

						editor.putFloat(WidgetNotiConstants.Commons.DataKeys.LATITUDE.name(), (float) notificationDataObj.getLatitude())
								.putFloat(WidgetNotiConstants.Commons.DataKeys.LONGITUDE.name(),
										(float) notificationDataObj.getLongitude())
								.putString(WidgetNotiConstants.Commons.DataKeys.COUNTRY_CODE.name(), notificationDataObj.getCountryCode())
								.putString(WidgetNotiConstants.Commons.DataKeys.ADDRESS_NAME.name(), notificationDataObj.getAddressName()).commit();

						fusedLocation.cancelNotification(context);

						loadWeatherData(collapsedRemoteViews, expandedRemoteViews);
					}
				});
			}

			@Override
			public void onFailed(Fail fail) {
				RemoteViewsUtil.ErrorType errorType = null;

				if (fail == Fail.DENIED_LOCATION_PERMISSIONS) {
					errorType = RemoteViewsUtil.ErrorType.DENIED_GPS_PERMISSIONS;
				} else if (fail == Fail.DISABLED_GPS) {
					errorType = RemoteViewsUtil.ErrorType.GPS_OFF;
				} else if (fail == Fail.DENIED_ACCESS_BACKGROUND_LOCATION_PERMISSION) {
					errorType = RemoteViewsUtil.ErrorType.DENIED_BACKGROUND_LOCATION_PERMISSION;
				} else {
					errorType = RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA;
				}

				forceFailedNotification(errorType);
			}
		};

		PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

		if (powerManager.isInteractive()) {
			fusedLocation.startNotification(context);
			fusedLocation.findCurrentLocation(locationCallback, false);
		} else {
			LocationResult lastLocation = fusedLocation.getLastCurrentLocation();
			if (lastLocation.getLocations().get(0).getLatitude() == 0.0 ||
					lastLocation.getLocations().get(0).getLongitude() == 0.0) {
				fusedLocation.findCurrentLocation(locationCallback, false);
			} else {
				locationCallback.onSuccessful(lastLocation);
			}
		}

	}


	public void loadWeatherData(RemoteViews collapsedRemoteViews, RemoteViews expandedRemoteViews) {
		RemoteViewsUtil.onBeginProcess(expandedRemoteViews);
		RemoteViewsUtil.onBeginProcess(collapsedRemoteViews);
		makeNotification(collapsedRemoteViews, expandedRemoteViews, R.mipmap.ic_launcher_round, null, false);

		final Set<WeatherDataType> weatherDataTypeSet = getRequestWeatherDataTypeSet();
		WeatherProviderType weatherProviderType = notificationDataObj.getWeatherSourceType();

		if (notificationDataObj.isTopPriorityKma() && notificationDataObj.getCountryCode().equals("KR")) {
			weatherProviderType = WeatherProviderType.KMA_WEB;
		}

		final Set<WeatherProviderType> weatherProviderTypeSet = new HashSet<>();
		weatherProviderTypeSet.add(weatherProviderType);
		if (weatherDataTypeSet.contains(WeatherDataType.airQuality)) {
			weatherProviderTypeSet.add(WeatherProviderType.AQICN);
		}

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		WeatherProviderType finalWeatherProviderType = weatherProviderType;
		WeatherRequestUtil.loadWeatherData(context, executorService,
				notificationDataObj.getLatitude(), notificationDataObj.getLongitude(), weatherDataTypeSet,
				new MultipleRestApiDownloader() {
					@Override
					public void onResult() {
						setResultViews(collapsedRemoteViews, expandedRemoteViews, finalWeatherProviderType, this, weatherDataTypeSet);
					}

					@Override
					public void onCanceled() {
						setResultViews(collapsedRemoteViews, expandedRemoteViews, finalWeatherProviderType, this, weatherDataTypeSet);
					}
				}, weatherProviderTypeSet);
	}

	protected PendingIntent getRefreshPendingIntent() {
		Intent refreshIntent = new Intent(context, OngoingNotificationReceiver.class);
		refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));

		return PendingIntent.getBroadcast(context, NotificationType.Ongoing.getNotificationId() + 1, refreshIntent,
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
	}

	public RemoteViews[] createRemoteViews(boolean temp) {
		RemoteViews collapsedRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_ongoing_notification_collapsed);
		RemoteViews expandedRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_ongoing_notification_expanded);

		if (temp) {
			setHourlyForecastViews(expandedRemoteViews, WeatherResponseProcessor.getTempHourlyForecastDtoList(context, hourlyForecastCount));
		} else {
			collapsedRemoteViews.setOnClickPendingIntent(R.id.refreshLayout, getRefreshPendingIntent());
			expandedRemoteViews.setOnClickPendingIntent(R.id.refreshLayout, getRefreshPendingIntent());
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			expandedRemoteViews.setViewPadding(R.id.root_layout, 0, 0, 0, 0);
		}

		return new RemoteViews[]{collapsedRemoteViews, expandedRemoteViews};
	}


	public void initNotification(Callback callback) {
		this.callback = callback;
		RemoteViews[] remoteViews = createRemoteViews(false);

		RemoteViews collapsedView = remoteViews[0];
		RemoteViews expandedView = remoteViews[1];

		RemoteViewsUtil.onBeginProcess(expandedView);
		RemoteViewsUtil.onBeginProcess(collapsedView);
		makeNotification(collapsedView, expandedView, R.drawable.refresh, null, false);

		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				timer = null;
				if (callback != null) {
					collapsedView.setOnClickPendingIntent(R.id.refreshBtn, getRefreshPendingIntent());
					expandedView.setOnClickPendingIntent(R.id.refreshBtn, getRefreshPendingIntent());
					RemoteViewsUtil.onErrorProcess(expandedView, context, RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA);
					RemoteViewsUtil.onErrorProcess(collapsedView, context, RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA);
					makeNotification(collapsedView, expandedView, R.mipmap.ic_launcher_round, null, true);

					callback.onResult();
				}
			}
		}, TimeUnit.SECONDS.toMillis(20L));

		if (notificationDataObj.getLocationType() == LocationType.CurrentLocation) {
			loadCurrentLocation(collapsedView, expandedView);
		} else {
			loadWeatherData(collapsedView, expandedView);
		}
	}

	protected Set<WeatherDataType> getRequestWeatherDataTypeSet() {
		Set<WeatherDataType> set = new HashSet<>();
		set.add(WeatherDataType.currentConditions);
		set.add(WeatherDataType.airQuality);
		set.add(WeatherDataType.hourlyForecast);
		return set;
	}

	protected void setResultViews(RemoteViews collapsedRemoteViews, RemoteViews expandedRemoteViews, WeatherProviderType requestWeatherProviderType, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader, Set<WeatherDataType> weatherDataTypeSet) {
		ZoneOffset zoneOffset = null;
		setHeaderViews(collapsedRemoteViews, notificationDataObj.getAdmin(), multipleRestApiDownloader.getRequestDateTime().toString());
		setHeaderViews(expandedRemoteViews, notificationDataObj.getAddressName(), multipleRestApiDownloader.getRequestDateTime().toString());

		int icon = R.mipmap.ic_launcher_round;
		String temperature = null;

		final CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.getCurrentConditionsDto(context, multipleRestApiDownloader,
				requestWeatherProviderType);
		if (currentConditionsDto != null) {
			zoneOffset = currentConditionsDto.getCurrentTime().getOffset();
			setCurrentConditionsViews(expandedRemoteViews, currentConditionsDto);
			setCollapsedCurrentConditionsViews(collapsedRemoteViews, currentConditionsDto);

			icon = currentConditionsDto.getWeatherIcon();
			temperature = currentConditionsDto.getTemp().replace(MyApplication.VALUE_UNIT_OBJ.getTempUnitText(), "°");
		}

		final List<HourlyForecastDto> hourlyForecastDtoList = WeatherResponseProcessor.getHourlyForecastDtoList(context, multipleRestApiDownloader,
				requestWeatherProviderType);
		if (!hourlyForecastDtoList.isEmpty()) {
			setHourlyForecastViews(expandedRemoteViews, hourlyForecastDtoList);
		}

		AirQualityDto airQualityDto = null;
		if (zoneOffset != null) {
			airQualityDto = WeatherResponseProcessor.getAirQualityDto(context, multipleRestApiDownloader,
					zoneOffset);
			if (airQualityDto.isSuccessful()) {
				setAirQualityViews(expandedRemoteViews, AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi()));
			} else {
				setAirQualityViews(expandedRemoteViews, context.getString(R.string.noData));
			}
		} else {
			setAirQualityViews(expandedRemoteViews, context.getString(R.string.noData));
		}
		final boolean successful = currentConditionsDto != null && !hourlyForecastDtoList.isEmpty();

		if (successful) {
			RemoteViewsUtil.onSuccessfulProcess(collapsedRemoteViews);
			RemoteViewsUtil.onSuccessfulProcess(expandedRemoteViews);
		} else {
			expandedRemoteViews.setOnClickPendingIntent(R.id.refreshBtn, getRefreshPendingIntent());
			collapsedRemoteViews.setOnClickPendingIntent(R.id.refreshBtn, getRefreshPendingIntent());
			RemoteViewsUtil.onErrorProcess(collapsedRemoteViews, context, RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA);
			RemoteViewsUtil.onErrorProcess(expandedRemoteViews, context, RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA);
		}

		makeNotification(collapsedRemoteViews, expandedRemoteViews, icon, temperature, true);
	}

	public void setHeaderViews(RemoteViews remoteViews, String addressName, String dateTime) {
		remoteViews.setTextViewText(R.id.address, addressName);
		remoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(dateTime).format(dateTimeFormatter));
	}

	public void setAirQualityViews(RemoteViews remoteViews, String value) {
		String airQuality = context.getString(R.string.air_quality) + ": " + value;
		remoteViews.setTextViewText(R.id.airQuality, airQuality);
	}

	public void setCurrentConditionsViews(RemoteViews remoteViews, CurrentConditionsDto currentConditionsDto) {
		remoteViews.setImageViewResource(R.id.weatherIcon, currentConditionsDto.getWeatherIcon());
		String precipitation = "";
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto.getPrecipitationVolume();
		} else {
			precipitation = context.getString(R.string.not_precipitation);
		}
		remoteViews.setTextViewText(R.id.precipitation, precipitation);
		remoteViews.setTextViewText(R.id.temperature, currentConditionsDto.getTemp());
		remoteViews.setTextViewText(R.id.feelsLikeTemp, new String(context.getString(R.string.feelsLike) + ": " + currentConditionsDto.getFeelsLikeTemp()));

		if (currentConditionsDto.getYesterdayTemp() != null) {
			String yesterdayCompText = WeatherUtil.makeTempCompareToYesterdayText(currentConditionsDto.getTemp(),
					currentConditionsDto.getYesterdayTemp(), tempUnit, context);
			remoteViews.setTextViewText(R.id.yesterdayTemperature, yesterdayCompText);
			remoteViews.setViewVisibility(R.id.yesterdayTemperature, View.VISIBLE);
		} else {
			remoteViews.setViewVisibility(R.id.yesterdayTemperature, View.GONE);
		}
	}

	public void setCollapsedCurrentConditionsViews(RemoteViews remoteViews, CurrentConditionsDto currentConditionsDto) {
		remoteViews.setImageViewResource(R.id.weatherIcon, currentConditionsDto.getWeatherIcon());

		remoteViews.setTextViewText(R.id.temperature, currentConditionsDto.getTemp());
		remoteViews.setTextViewText(R.id.feelsLikeTemp, new String(context.getString(R.string.feelsLike) + ": " + currentConditionsDto.getFeelsLikeTemp()));

		if (currentConditionsDto.getYesterdayTemp() != null) {
			String yesterdayCompText = WeatherUtil.makeTempCompareToYesterdayText(currentConditionsDto.getTemp(),
					currentConditionsDto.getYesterdayTemp(), tempUnit, context);
			remoteViews.setTextViewText(R.id.yesterdayTemperature, yesterdayCompText);
			remoteViews.setViewVisibility(R.id.yesterdayTemperature, View.VISIBLE);
		} else {
			remoteViews.setViewVisibility(R.id.yesterdayTemperature, View.GONE);
		}
	}

	public void setHourlyForecastViews(RemoteViews remoteViews, List<HourlyForecastDto> hourlyForecastDtoList) {
		remoteViews.removeAllViews(R.id.hourlyForecast);
		final int textColor = ContextCompat.getColor(context, R.color.textColorInNotification);
		DateTimeFormatter hour0Formatter = DateTimeFormatter.ofPattern("E 0");
		String hours = null;

		boolean haveRain = false;
		boolean haveSnow = false;

		for (int i = 0; i < hourlyForecastCount; i++) {
			if (hourlyForecastDtoList.get(i).isHasRain()) {
				haveRain = true;
			}
			if (hourlyForecastDtoList.get(i).isHasSnow()) {
				haveSnow = true;
			}
		}

		final String mm = "mm";
		final String cm = "cm";

		for (int i = 0; i < hourlyForecastCount; i++) {
			RemoteViews childRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_forecast_item_in_linear);

			if (haveRain) {
				if (hourlyForecastDtoList.get(i).isHasRain()) {
					childRemoteViews.setTextViewText(R.id.rainVolume, hourlyForecastDtoList.get(i).getRainVolume()
							.replace(mm, "").replace(cm, ""));
					childRemoteViews.setTextColor(R.id.rainVolume, textColor);
				} else {
					childRemoteViews.setViewVisibility(R.id.rainVolumeLayout, View.INVISIBLE);
				}
			} else {
				childRemoteViews.setViewVisibility(R.id.rainVolumeLayout, View.GONE);
			}

			if (haveSnow) {
				if (hourlyForecastDtoList.get(i).isHasSnow()) {
					childRemoteViews.setTextViewText(R.id.snowVolume, hourlyForecastDtoList.get(i).getSnowVolume()
							.replace(mm, "").replace(cm, ""));
					childRemoteViews.setTextColor(R.id.snowVolume, textColor);
				} else {
					childRemoteViews.setViewVisibility(R.id.snowVolumeLayout, View.INVISIBLE);
				}
			} else {
				childRemoteViews.setViewVisibility(R.id.snowVolumeLayout, View.GONE);
			}

			if (hourlyForecastDtoList.get(i).getHours().getHour() == 0) {
				hours = hourlyForecastDtoList.get(i).getHours().format(hour0Formatter);
			} else {
				hours = String.valueOf(hourlyForecastDtoList.get(i).getHours().getHour());
			}

			childRemoteViews.setTextViewText(R.id.dateTime, hours);
			childRemoteViews.setTextViewText(R.id.pop, hourlyForecastDtoList.get(i).getPop());
			childRemoteViews.setTextViewText(R.id.temperature, hourlyForecastDtoList.get(i).getTemp());
			childRemoteViews.setImageViewResource(R.id.leftIcon, hourlyForecastDtoList.get(i).getWeatherIcon());
			childRemoteViews.setViewVisibility(R.id.rightIcon, View.GONE);

			childRemoteViews.setTextColor(R.id.dateTime, textColor);
			childRemoteViews.setTextColor(R.id.temperature, textColor);
			childRemoteViews.setTextColor(R.id.pop, textColor);

			remoteViews.addView(R.id.hourlyForecast, childRemoteViews);
		}
	}

	public void makeNotification(RemoteViews collapsedRemoteViews, RemoteViews expandedRemoteViews, int icon, @Nullable String temperature, boolean isFinished) {
		boolean enabled =
				PreferenceManager.getDefaultSharedPreferences(context).getBoolean(notificationType.getPreferenceName(), false);

		if (enabled) {
			NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(notificationType);
			NotificationCompat.Builder builder = notificationObj.getNotificationBuilder();

			if (isFinished) {
				if (temperature != null) {
					if (notificationDataObj.getDataTypeOfIcon() == WidgetNotiConstants.DataTypeOfIcon.TEMPERATURE) {
						final int textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18f,
								context.getResources().getDisplayMetrics());

						TextPaint textPaint = new TextPaint();
						textPaint.setColor(Color.WHITE);
						textPaint.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
						textPaint.setTextAlign(Paint.Align.CENTER);
						textPaint.setTextScaleX(0.9f);
						textPaint.setTextSize(textSize);

						Rect textRect = new Rect();
						textPaint.getTextBounds(temperature, 0, temperature.length(), textRect);

						final int iconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f,
								context.getResources().getDisplayMetrics());
						Bitmap iconBitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
						Canvas canvas = new Canvas(iconBitmap);
						canvas.drawText(temperature, canvas.getWidth() / 2f, canvas.getHeight() / 2f + textRect.height() / 2f, textPaint);

						builder.setSmallIcon(IconCompat.createWithBitmap(iconBitmap));
					} else {
						builder.setSmallIcon(icon);
					}
				} else {
					builder.setSmallIcon(icon);
				}
			} else {
				builder.setSmallIcon(icon);
			}

			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
				builder.setDefaults(0).setVibrate(null).setSound(null).setLights(0, 0, 0)
						.setPriority(NotificationCompat.PRIORITY_LOW).setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				builder.setAutoCancel(false).setOngoing(true).setOnlyAlertOnce(true)
						.setCustomContentView(collapsedRemoteViews).setCustomBigContentView(expandedRemoteViews).setSilent(true);

			} else {
				builder.setAutoCancel(false).setOngoing(true).setOnlyAlertOnce(true)
						.setCustomContentView(expandedRemoteViews).setCustomBigContentView(expandedRemoteViews).setSilent(true);
			}

			NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
			notificationManager.notify(notificationType.getNotificationId(), builder.build());
		} else {
			notificationHelper.cancelNotification(notificationType.getNotificationId());
		}

		if (isFinished) {
			if (timer != null) {
				timer.cancel();
				timer = null;
			}
			if (callback != null) {
				callback.onResult();
			}
		}
	}


	public void loadSavedPreferences() {
		SharedPreferences notiPreferences = context.getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE);

		notificationDataObj = new OngoingNotiDataObj();
		notificationDataObj.setLocationType(LocationType.valueOf(notiPreferences.getString(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name(), LocationType.CurrentLocation.name())));
		notificationDataObj.setWeatherSourceType(WeatherProviderType.valueOf(notiPreferences.getString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(),
				WeatherProviderType.OWM_ONECALL.name())));
		notificationDataObj.setTopPriorityKma(notiPreferences.getBoolean(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name(), false));
		notificationDataObj.setUpdateIntervalMillis(notiPreferences.getLong(WidgetNotiConstants.Commons.Attributes.UPDATE_INTERVAL.name(), 0L));
		notificationDataObj.setSelectedAddressDtoId(notiPreferences.getInt(WidgetNotiConstants.Commons.Attributes.SELECTED_ADDRESS_DTO_ID.name(), 0));
		notificationDataObj.setDataTypeOfIcon(WidgetNotiConstants.DataTypeOfIcon.valueOf(notiPreferences.getString(WidgetNotiConstants.OngoingNotiAttributes.DATA_TYPE_OF_ICON.name(),
				WidgetNotiConstants.DataTypeOfIcon.TEMPERATURE.name())));

		notificationDataObj.setAddressName(notiPreferences.getString(WidgetNotiConstants.Commons.DataKeys.ADDRESS_NAME.name(), ""));
		notificationDataObj.setLatitude(notiPreferences.getFloat(WidgetNotiConstants.Commons.DataKeys.LATITUDE.name(), 0f));
		notificationDataObj.setLongitude(notiPreferences.getFloat(WidgetNotiConstants.Commons.DataKeys.LONGITUDE.name(), 0f));
		notificationDataObj.setAdmin(notiPreferences.getString(WidgetNotiConstants.Commons.DataKeys.ADMIN.name(), ""));
		notificationDataObj.setCountryCode(notiPreferences.getString(WidgetNotiConstants.Commons.DataKeys.COUNTRY_CODE.name(), ""));
	}

	public void loadDefaultPreferences() {
		notificationDataObj = new OngoingNotiDataObj();
		notificationDataObj.setLocationType(LocationType.CurrentLocation);
		notificationDataObj.setWeatherSourceType(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context
				.getString(R.string.pref_key_met), true) ? WeatherProviderType.MET_NORWAY : WeatherProviderType.OWM_ONECALL);
		notificationDataObj.setTopPriorityKma(false);
		notificationDataObj.setUpdateIntervalMillis(0);
		notificationDataObj.setSelectedAddressDtoId(0);
		notificationDataObj.setDataTypeOfIcon(WidgetNotiConstants.DataTypeOfIcon.TEMPERATURE);

		SharedPreferences notiPreferences = context.getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = notiPreferences.edit();
		editor.putString(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name(), notificationDataObj.getLocationType().name());
		editor.putString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(), notificationDataObj.getWeatherSourceType().name());
		editor.putBoolean(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name(), notificationDataObj.isTopPriorityKma());
		editor.putLong(WidgetNotiConstants.Commons.Attributes.UPDATE_INTERVAL.name(), notificationDataObj.getUpdateIntervalMillis());
		editor.putInt(WidgetNotiConstants.Commons.Attributes.SELECTED_ADDRESS_DTO_ID.name(), notificationDataObj.getSelectedAddressDtoId());
		editor.putString(WidgetNotiConstants.OngoingNotiAttributes.DATA_TYPE_OF_ICON.name(), notificationDataObj.getDataTypeOfIcon().name());
		editor.commit();
	}


	public void savePreferences() {
		SharedPreferences sharedPreferences = context.getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name(), notificationDataObj.getLocationType().name());
		editor.putString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(), notificationDataObj.getWeatherSourceType().name());
		editor.putBoolean(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name(), notificationDataObj.isTopPriorityKma());
		editor.putLong(WidgetNotiConstants.Commons.Attributes.UPDATE_INTERVAL.name(), notificationDataObj.getUpdateIntervalMillis());
		editor.putInt(WidgetNotiConstants.Commons.Attributes.SELECTED_ADDRESS_DTO_ID.name(), notificationDataObj.getSelectedAddressDtoId());
		editor.putString(WidgetNotiConstants.OngoingNotiAttributes.DATA_TYPE_OF_ICON.name(), notificationDataObj.getDataTypeOfIcon().name());

		editor.putString(WidgetNotiConstants.Commons.DataKeys.ADDRESS_NAME.name(), notificationDataObj.getAddressName());
		editor.putFloat(WidgetNotiConstants.Commons.DataKeys.LATITUDE.name(), (float) notificationDataObj.getLatitude());
		editor.putFloat(WidgetNotiConstants.Commons.DataKeys.LONGITUDE.name(), (float) notificationDataObj.getLongitude());
		editor.putString(WidgetNotiConstants.Commons.DataKeys.COUNTRY_CODE.name(), notificationDataObj.getCountryCode());
		editor.putString(WidgetNotiConstants.Commons.DataKeys.ADMIN.name(), notificationDataObj.getAdmin());
		editor.commit();
	}


	public void loadPreferences() {
		SharedPreferences notiPreferences = context.getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE);
		if (notiPreferences.getAll().isEmpty()) {
			loadDefaultPreferences();
		} else {
			loadSavedPreferences();
		}
	}


	public void forceFailedNotification(RemoteViewsUtil.ErrorType errorType) {
		RemoteViews[] remoteViews = createRemoteViews(false);
		remoteViews[0].setOnClickPendingIntent(R.id.refreshBtn, getRefreshPendingIntent());
		remoteViews[1].setOnClickPendingIntent(R.id.refreshBtn, getRefreshPendingIntent());
		RemoteViewsUtil.onErrorProcess(remoteViews[0], context, errorType);
		RemoteViewsUtil.onErrorProcess(remoteViews[1], context, errorType);
		makeNotification(remoteViews[0], remoteViews[1], R.mipmap.ic_launcher_round, null, true);
	}
}
