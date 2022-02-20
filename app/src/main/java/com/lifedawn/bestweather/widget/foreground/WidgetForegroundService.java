package com.lifedawn.bestweather.widget.foreground;

import android.app.Notification;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationResult;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.forremoteviews.RemoteViewsUtil;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.room.repository.WidgetRepository;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherRequestUtil;
import com.lifedawn.bestweather.widget.WidgetHelper;
import com.lifedawn.bestweather.widget.creator.AbstractWidgetCreator;
import com.lifedawn.bestweather.widget.creator.EighthWidgetCreator;
import com.lifedawn.bestweather.widget.creator.EleventhWidgetCreator;
import com.lifedawn.bestweather.widget.creator.FifthWidgetCreator;
import com.lifedawn.bestweather.widget.creator.FirstWidgetCreator;
import com.lifedawn.bestweather.widget.creator.FourthWidgetCreator;
import com.lifedawn.bestweather.widget.creator.NinthWidgetCreator;
import com.lifedawn.bestweather.widget.creator.SecondWidgetCreator;
import com.lifedawn.bestweather.widget.creator.SeventhWidgetCreator;
import com.lifedawn.bestweather.widget.creator.SixthWidgetCreator;
import com.lifedawn.bestweather.widget.creator.TenthWidgetCreator;
import com.lifedawn.bestweather.widget.creator.ThirdWidgetCreator;
import com.lifedawn.bestweather.widget.widgetprovider.EighthWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.EleventhWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.FifthWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.FirstWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.FourthWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.NinthWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.SecondWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.SixthWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.TenthWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.ThirdWidgetProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WidgetForegroundService extends Service {
	private final ArrayMap<Integer, WidgetDto> currentLocationWidgetDtoArrayMap = new ArrayMap<>();
	private final ArrayMap<Integer, WidgetDto> selectedLocationWidgetDtoArrayMap = new ArrayMap<>();
	private final ArrayMap<Integer, WidgetDto> allWidgetDtoArrayMap = new ArrayMap<>();
	private final ArrayMap<Integer, Class<?>> allWidgetProviderClassArrayMap = new ArrayMap<>();
	private final ArrayMap<Integer, RemoteViews> remoteViewsArrayMap = new ArrayMap<>();
	private final Map<String, MultipleRestApiDownloader> weatherResponseMap = new HashMap<>();
	private final Map<String, RequestObj> weatherRequestMap = new HashMap<>();

	private WidgetRepository widgetRepository;
	private AppWidgetManager appWidgetManager;
	private Map<Integer, AbstractWidgetCreator> widgetCreatorMap = new HashMap<>();
	private ExecutorService executorService = Executors.newFixedThreadPool(3);

	private int requestCount;
	private int responseCount;

	public WidgetForegroundService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (widgetRepository == null) {
			widgetRepository = new WidgetRepository(getApplicationContext());
		}
		if (appWidgetManager == null) {
			appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
		}
	}

	private void showNotification() {
		NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
		NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(NotificationType.WidgetForegroundService);

		NotificationCompat.Builder builder = notificationObj.getNotificationBuilder();
		builder.setSmallIcon(R.mipmap.ic_launcher_round).setContentText(getString(R.string.updatingWidgets)).setContentTitle(getString(R.string.updatingWidgets))
				.setOnlyAlertOnce(true).setWhen(0).setOngoing(true);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			builder.setPriority(NotificationCompat.PRIORITY_LOW).setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		}

		Notification notification = notificationObj.getNotificationBuilder().build();
		startForeground(notificationObj.getNotificationId(), notification);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		currentLocationWidgetDtoArrayMap.clear();
		selectedLocationWidgetDtoArrayMap.clear();

		allWidgetDtoArrayMap.clear();
		remoteViewsArrayMap.clear();
		weatherRequestMap.clear();
		weatherResponseMap.clear();

		allWidgetProviderClassArrayMap.clear();
		widgetCreatorMap.clear();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		showNotification();
		final String action = intent.getAction();

		if (action.equals(getString(R.string.com_lifedawn_bestweather_action_INIT))) {
			Bundle bundle = intent.getExtras();
			final int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);

			widgetRepository.get(appWidgetId, new DbQueryCallback<WidgetDto>() {
				@Override
				public void onResultSuccessful(WidgetDto widgetDto) {
					AbstractWidgetCreator widgetCreator = createWidgetViewCreator(appWidgetId, widgetDto.getWidgetProviderClassName());
					WidgetHelper widgetHelper = new WidgetHelper(getApplicationContext());
					if (widgetDto.getUpdateIntervalMillis() > 0) {
						widgetHelper.onSelectedAutoRefreshInterval(widgetDto.getUpdateIntervalMillis(), appWidgetId,
								allWidgetProviderClassArrayMap.get(appWidgetId));
					}

					List<String> addressList = new ArrayList<>();

					remoteViewsArrayMap.put(widgetDto.getAppWidgetId(),
							widgetCreatorMap.get(widgetDto.getAppWidgetId()).createRemoteViews());

					if (widgetDto.getLocationType() == LocationType.CurrentLocation) {
						currentLocationWidgetDtoArrayMap.put(widgetDto.getAppWidgetId(), widgetDto);
					} else {
						selectedLocationWidgetDtoArrayMap.put(widgetDto.getAppWidgetId(), widgetDto);

						if (!weatherRequestMap.containsKey(widgetDto.getAddressName())) {
							Address address = new Address(Locale.getDefault());

							address.setLatitude(widgetDto.getLatitude());
							address.setLongitude(widgetDto.getLongitude());
							address.setAddressLine(0, widgetDto.getAddressName());
							address.setCountryCode(widgetDto.getCountryCode());

							RequestObj requestObj = new RequestObj(address);

							weatherRequestMap.put(widgetDto.getAddressName(), requestObj);
							addressList.add(widgetDto.getAddressName());
						}
						weatherRequestMap.get(widgetDto.getAddressName()).weatherDataTypeSet.addAll(widgetCreator.getRequestWeatherDataTypeSet());
						weatherRequestMap.get(widgetDto.getAddressName()).weatherDataSourceTypeSet.addAll(widgetDto.getWeatherSourceTypeSet());
						weatherRequestMap.get(widgetDto.getAddressName()).appWidgetSet.add(widgetDto.getAppWidgetId());
					}

					allWidgetDtoArrayMap.putAll(currentLocationWidgetDtoArrayMap);
					allWidgetDtoArrayMap.putAll(selectedLocationWidgetDtoArrayMap);
					requestCount = 1;
					responseCount = 0;

					if (!currentLocationWidgetDtoArrayMap.isEmpty()) {
						loadCurrentLocation();
					}
					if (!selectedLocationWidgetDtoArrayMap.isEmpty()) {
						loadWeatherData(addressList);
					}
				}

				@Override
				public void onResultNoData() {

				}
			});
		} else if (action.equals(getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
			widgetRepository.getAll(new DbQueryCallback<List<WidgetDto>>() {
				@Override
				public void onResultSuccessful(List<WidgetDto> list) {
					List<String> addressList = new ArrayList<>();

					for (WidgetDto widgetDto : list) {
						AbstractWidgetCreator widgetCreator = createWidgetViewCreator(widgetDto.getAppWidgetId(),
								widgetDto.getWidgetProviderClassName());

						remoteViewsArrayMap.put(widgetDto.getAppWidgetId(),
								widgetCreatorMap.get(widgetDto.getAppWidgetId()).createRemoteViews());

						if (widgetDto.getLocationType() == LocationType.CurrentLocation) {
							currentLocationWidgetDtoArrayMap.put(widgetDto.getAppWidgetId(), widgetDto);
						} else {
							selectedLocationWidgetDtoArrayMap.put(widgetDto.getAppWidgetId(), widgetDto);

							if (!weatherRequestMap.containsKey(widgetDto.getAddressName())) {
								Address address = new Address(Locale.getDefault());

								address.setLatitude(widgetDto.getLatitude());
								address.setLongitude(widgetDto.getLongitude());
								address.setAddressLine(0, widgetDto.getAddressName());
								address.setCountryCode(widgetDto.getCountryCode());

								RequestObj requestObj = new RequestObj(address);

								weatherRequestMap.put(widgetDto.getAddressName(), requestObj);
								addressList.add(widgetDto.getAddressName());
							}
							weatherRequestMap.get(widgetDto.getAddressName()).weatherDataTypeSet.addAll(widgetCreator.getRequestWeatherDataTypeSet());
							weatherRequestMap.get(widgetDto.getAddressName()).weatherDataSourceTypeSet.addAll(widgetDto.getWeatherSourceTypeSet());
							weatherRequestMap.get(widgetDto.getAddressName()).appWidgetSet.add(widgetDto.getAppWidgetId());
						}

					}

					allWidgetDtoArrayMap.putAll(currentLocationWidgetDtoArrayMap);
					allWidgetDtoArrayMap.putAll(selectedLocationWidgetDtoArrayMap);

					final Set<Integer> appWidgetIdSet = allWidgetDtoArrayMap.keySet();
					for (Integer appWidgetId : appWidgetIdSet) {
						RemoteViews remoteViews = remoteViewsArrayMap.get(appWidgetId);
						RemoteViewsUtil.onBeginProcess(remoteViews);
						appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
					}

					requestCount = 0;
					responseCount = 0;

					if (!currentLocationWidgetDtoArrayMap.isEmpty()) {
						requestCount++;
						loadCurrentLocation();
					}
					if (!selectedLocationWidgetDtoArrayMap.isEmpty()) {
						requestCount += addressList.size();
						loadWeatherData(addressList);
					}
				}

				@Override
				public void onResultNoData() {

				}
			});

		} else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {

		}

		return START_NOT_STICKY;
	}


	private AbstractWidgetCreator createWidgetViewCreator(int appWidgetId, String widgetProviderClassName) {
		AbstractWidgetCreator widgetCreator = null;

		if (widgetProviderClassName.equals(FirstWidgetProvider.class.getName())) {
			widgetCreator = new FirstWidgetCreator(getApplicationContext(), null, appWidgetId);
		} else if (widgetProviderClassName.equals(SecondWidgetProvider.class.getName())) {
			widgetCreator = new SecondWidgetCreator(getApplicationContext(), null, appWidgetId);
		} else if (widgetProviderClassName.equals(ThirdWidgetProvider.class.getName())) {
			widgetCreator = new ThirdWidgetCreator(getApplicationContext(), null, appWidgetId);
		} else if (widgetProviderClassName.equals(FourthWidgetProvider.class.getName())) {
			widgetCreator = new FourthWidgetCreator(getApplicationContext(), null, appWidgetId);
		} else if (widgetProviderClassName.equals(FifthWidgetProvider.class.getName())) {
			widgetCreator = new FifthWidgetCreator(getApplicationContext(), null, appWidgetId);
		} else if (widgetProviderClassName.equals(SixthWidgetProvider.class.getName())) {
			widgetCreator = new SixthWidgetCreator(getApplicationContext(), null, appWidgetId);
		} else if (widgetProviderClassName.equals(SecondWidgetProvider.class.getName())) {
			widgetCreator = new SeventhWidgetCreator(getApplicationContext(), null, appWidgetId);
		} else if (widgetProviderClassName.equals(EighthWidgetProvider.class.getName())) {
			widgetCreator = new EighthWidgetCreator(getApplicationContext(), null, appWidgetId);
		} else if (widgetProviderClassName.equals(NinthWidgetProvider.class.getName())) {
			widgetCreator = new NinthWidgetCreator(getApplicationContext(), null, appWidgetId);
		} else if (widgetProviderClassName.equals(TenthWidgetProvider.class.getName())) {
			widgetCreator = new TenthWidgetCreator(getApplicationContext(), null, appWidgetId);
		} else if (widgetProviderClassName.equals(EleventhWidgetProvider.class.getName())) {
			widgetCreator = new EleventhWidgetCreator(getApplicationContext(), null, appWidgetId);
		}

		allWidgetProviderClassArrayMap.put(appWidgetId, widgetCreator.widgetProviderClass());
		widgetCreatorMap.put(appWidgetId, widgetCreator);

		return widgetCreator;
	}

	private void onResponseResult(@Nullable String addressName) {
		if (addressName != null) {
			RequestObj requestObj = weatherRequestMap.get(addressName);
			Set<Integer> appWidgetIdSet = requestObj.appWidgetSet;

			for (Integer appWidgetId : appWidgetIdSet) {
				AbstractWidgetCreator widgetCreator = widgetCreatorMap.get(appWidgetId);
				widgetCreator.setWidgetDto(allWidgetDtoArrayMap.get(appWidgetId));
				widgetCreator.setResultViews(appWidgetId, remoteViewsArrayMap.get(appWidgetId), weatherResponseMap.get(addressName));
			}
		}

		if (++responseCount == requestCount) {
			stopService();
		}
	}

	private void stopService() {
		stopForeground(true);
		stopSelf();
	}

	private void setRefreshPendingIntent(int appWidgetId) {
		widgetCreatorMap.get(appWidgetId).setRefreshPendingIntent(allWidgetProviderClassArrayMap.get(appWidgetId), remoteViewsArrayMap.get(appWidgetId)
				, appWidgetId);
	}


	public void loadCurrentLocation() {
		final FusedLocation.MyLocationCallback locationCallback = new FusedLocation.MyLocationCallback() {
			@Override
			public void onSuccessful(LocationResult locationResult) {
				final Location location = getBestLocation(locationResult);
				Geocoding.geocoding(getApplicationContext(), location.getLatitude(), location.getLongitude(), new Geocoding.GeocodingCallback() {
					@Override
					public void onGeocodingResult(List<Address> addressList) {
						if (addressList.isEmpty()) {
							onLocationResult(Fail.FAILED_FIND_LOCATION, null);
						} else {
							final Set<Integer> appWidgetIdSet = currentLocationWidgetDtoArrayMap.keySet();
							final Address address = addressList.get(0);
							final String addressName = address.getAddressLine(0);

							if (!weatherRequestMap.containsKey(addressName)) {
								weatherRequestMap.put(addressName, new RequestObj(address));
							}

							for (Integer appWidgetId : appWidgetIdSet) {
								WidgetDto widgetDto = currentLocationWidgetDtoArrayMap.get(appWidgetId);

								widgetDto.setAddressName(address.getAddressLine(0));
								widgetDto.setCountryCode(address.getCountryCode());
								widgetDto.setLatitude(address.getLatitude());
								widgetDto.setLongitude(address.getLongitude());
								widgetRepository.update(widgetDto, null);

								weatherRequestMap.get(addressName).weatherDataTypeSet.addAll(widgetCreatorMap.get(appWidgetId).getRequestWeatherDataTypeSet());
								weatherRequestMap.get(addressName).weatherDataSourceTypeSet.addAll(widgetDto.getWeatherSourceTypeSet());
								weatherRequestMap.get(addressName).appWidgetSet.add(appWidgetId);
							}

							onLocationResult(null, addressName);
						}
					}
				});
			}

			@Override
			public void onFailed(Fail fail) {
				onLocationResult(fail, null);
			}
		};

		MainThreadWorker.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				FusedLocation.getInstance(getApplicationContext()).findCurrentLocation(locationCallback, false);
			}
		});
	}

	private void onLocationResult(@Nullable FusedLocation.MyLocationCallback.Fail fail, @Nullable String addressName) {
		if (fail == null) {
			List<String> addressesList = new ArrayList<>();
			addressesList.add(addressName);
			loadWeatherData(addressesList);
		} else {
			RemoteViewsUtil.ErrorType errorType = null;

			if (fail == FusedLocation.MyLocationCallback.Fail.DENIED_LOCATION_PERMISSIONS) {
				errorType = RemoteViewsUtil.ErrorType.DENIED_GPS_PERMISSIONS;
			} else if (fail == FusedLocation.MyLocationCallback.Fail.DISABLED_GPS) {
				errorType = RemoteViewsUtil.ErrorType.GPS_OFF;
			} else if (fail == FusedLocation.MyLocationCallback.Fail.DENIED_ACCESS_BACKGROUND_LOCATION_PERMISSION) {
				errorType = RemoteViewsUtil.ErrorType.DENIED_BACKGROUND_LOCATION_PERMISSION;
			} else {
				errorType = RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA;
			}

			final Set<Integer> appWidgetIdSet = currentLocationWidgetDtoArrayMap.keySet();
			for (Integer appWidgetId : appWidgetIdSet) {
				RemoteViews remoteViews = remoteViewsArrayMap.get(appWidgetId);
				setRefreshPendingIntent(appWidgetId);
				RemoteViewsUtil.onErrorProcess(remoteViews, getApplicationContext(), errorType);
				appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
			}

			onResponseResult(null);
		}

	}


	private void loadWeatherData(List<String> addressList) {
		for (String addressName : addressList) {
			if (weatherResponseMap.containsKey(addressName)) {
				continue;
			}

			final RequestObj requestObj = weatherRequestMap.get(addressName);
			final Set<WeatherDataType> weatherDataTypeSet = requestObj.weatherDataTypeSet;
			final Set<WeatherDataSourceType> weatherDataSourceTypeSet = requestObj.weatherDataSourceTypeSet;
			final Address address = requestObj.address;

			MultipleRestApiDownloader multipleRestApiDownloader = new MultipleRestApiDownloader() {
				@Override
				public void onResult() {
					onResponseResult(addressName);
				}

				@Override
				public void onCanceled() {
					onResponseResult(addressName);
				}
			};

			weatherResponseMap.put(addressName, multipleRestApiDownloader);
			WeatherRequestUtil.loadWeatherData(getApplicationContext(), executorService, address.getLatitude(),
					address.getLongitude(), weatherDataTypeSet, multipleRestApiDownloader, weatherDataSourceTypeSet);
		}

	}

	private static class RequestObj {
		final Address address;
		Set<Integer> appWidgetSet = new HashSet<>();
		Set<WeatherDataType> weatherDataTypeSet = new HashSet<>();
		Set<WeatherDataSourceType> weatherDataSourceTypeSet = new HashSet<>();

		public RequestObj(Address address) {
			this.address = address;
		}
	}
}