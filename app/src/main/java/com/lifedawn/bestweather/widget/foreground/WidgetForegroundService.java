package com.lifedawn.bestweather.widget.foreground;

import android.app.Notification;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
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
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
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
import com.lifedawn.bestweather.widget.widgetprovider.SeventhWidgetProvider;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WidgetForegroundService extends Service {
	private final ArrayMap<Integer, WidgetDto> currentLocationWidgetDtoArrayMap = new ArrayMap<>();
	private final ArrayMap<Integer, WidgetDto> selectedLocationWidgetDtoArrayMap = new ArrayMap<>();
	private final ArrayMap<Integer, WidgetDto> allWidgetDtoArrayMap = new ArrayMap<>();
	private final ArrayMap<Integer, Class<?>> allWidgetProviderClassArrayMap = new ArrayMap<>();
	private final ArrayMap<Integer, RemoteViews> remoteViewsArrayMap = new ArrayMap<>();
	private Map<Integer, AbstractWidgetCreator> widgetCreatorMap = new HashMap<>();

	private final Map<String, MultipleRestApiDownloader> currentLocationResponseMap = new HashMap<>();
	private final Map<String, MultipleRestApiDownloader> selectedLocationResponseMap = new HashMap<>();

	private final Map<String, RequestObj> currentLocationRequestMap = new HashMap<>();
	private final Map<String, RequestObj> selectedLocationRequestMap = new HashMap<>();

	private WidgetRepository widgetRepository;
	private AppWidgetManager appWidgetManager;
	private ExecutorService executorService = Executors.newFixedThreadPool(3);

	private int requestCount;
	private int responseCount;
	private Timer timer;

	private final String TAG = "WidgetForeground";

	public WidgetForegroundService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		showNotification();

		if (widgetRepository == null) {
			widgetRepository = new WidgetRepository(getApplicationContext());
		}
		if (appWidgetManager == null) {
			appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
		}
	}

	private void showNotification() {
		NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
		NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(NotificationType.ForegroundService);

		NotificationCompat.Builder builder = notificationObj.getNotificationBuilder();
		builder.setSmallIcon(R.mipmap.ic_launcher_round).setContentText(getString(R.string.updatingWidgets)).setContentTitle(getString(R.string.updatingWidgets))
				.setOnlyAlertOnce(true).setWhen(0).setOngoing(true);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			builder.setPriority(NotificationCompat.PRIORITY_LOW).setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		}

		Notification notification = notificationObj.getNotificationBuilder().build();
		startForeground((int) System.currentTimeMillis(), notification);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		currentLocationWidgetDtoArrayMap.clear();
		selectedLocationWidgetDtoArrayMap.clear();
		allWidgetDtoArrayMap.clear();

		remoteViewsArrayMap.clear();

		currentLocationRequestMap.clear();
		currentLocationResponseMap.clear();

		selectedLocationRequestMap.clear();
		selectedLocationResponseMap.clear();

		allWidgetProviderClassArrayMap.clear();
		widgetCreatorMap.clear();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		final String action = intent.getAction();

		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				timer = null;

				Set<Integer> appWidgetIdSet = new HashSet<>();
				Set<String> requestMapKeySet = new HashSet<>();

				requestMapKeySet.addAll(currentLocationRequestMap.keySet());
				requestMapKeySet.addAll(selectedLocationRequestMap.keySet());

				for (String addressName : requestMapKeySet) {
					if (selectedLocationRequestMap.containsKey(addressName)) {
						appWidgetIdSet.addAll(selectedLocationRequestMap.get(addressName).appWidgetSet);
					}
					if (currentLocationRequestMap.containsKey(addressName)) {
						appWidgetIdSet.addAll(currentLocationRequestMap.get(addressName).appWidgetSet);
					}
				}

				for (Integer appWidgetId : appWidgetIdSet) {
					AbstractWidgetCreator widgetCreator = widgetCreatorMap.get(appWidgetId);
					widgetCreator.setWidgetDto(allWidgetDtoArrayMap.get(appWidgetId));
					widgetCreator.setResultViews(appWidgetId, remoteViewsArrayMap.get(appWidgetId), null);
				}

				stopService();
			}
		}, TimeUnit.SECONDS.toMillis(25L));

		if (action.equals(getString(R.string.com_lifedawn_bestweather_action_INIT))) {
			Bundle bundle = intent.getExtras();
			final int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);

			widgetRepository.get(appWidgetId, new DbQueryCallback<WidgetDto>() {
				@Override
				public void onResultSuccessful(WidgetDto widgetDto) {
					AbstractWidgetCreator widgetCreator = createWidgetViewCreator(appWidgetId, widgetDto.getWidgetProviderClassName());
					WidgetHelper widgetHelper = new WidgetHelper(getApplicationContext());
					long widgetRefreshInterval = widgetHelper.getRefreshInterval();

					if (widgetRefreshInterval > 0 && !widgetHelper.isRepeating()) {
						widgetHelper.onSelectedAutoRefreshInterval(widgetRefreshInterval);
					}

					remoteViewsArrayMap.put(widgetDto.getAppWidgetId(),
							widgetCreatorMap.get(widgetDto.getAppWidgetId()).createRemoteViews());

					requestCount = 1;
					responseCount = 0;

					if (widgetDto.getLocationType() == LocationType.CurrentLocation) {
						currentLocationWidgetDtoArrayMap.put(widgetDto.getAppWidgetId(), widgetDto);
						allWidgetDtoArrayMap.putAll(currentLocationWidgetDtoArrayMap);
						showProgressBar();
						loadCurrentLocation();
					} else {
						selectedLocationWidgetDtoArrayMap.put(widgetDto.getAppWidgetId(), widgetDto);
						Address address = new Address(Locale.getDefault());

						address.setLatitude(widgetDto.getLatitude());
						address.setLongitude(widgetDto.getLongitude());
						address.setAddressLine(0, widgetDto.getAddressName());
						address.setCountryCode(widgetDto.getCountryCode());

						final RequestObj requestObj = new RequestObj(address);
						requestObj.weatherDataTypeSet.addAll(widgetCreator.getRequestWeatherDataTypeSet());
						requestObj.weatherProviderTypeSet.addAll(widgetDto.getWeatherProviderTypeSet());
						requestObj.appWidgetSet.add(widgetDto.getAppWidgetId());

						selectedLocationRequestMap.put(widgetDto.getAddressName(), requestObj);
						List<String> addressList = new ArrayList<>();
						addressList.add(widgetDto.getAddressName());
						allWidgetDtoArrayMap.putAll(selectedLocationWidgetDtoArrayMap);
						showProgressBar();
						loadWeatherData(LocationType.SelectedAddress, addressList);
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

							RequestObj requestObj = selectedLocationRequestMap.get(widgetDto.getAddressName());
							if (requestObj == null) {
								Address address = new Address(Locale.getDefault());

								address.setLatitude(widgetDto.getLatitude());
								address.setLongitude(widgetDto.getLongitude());
								address.setAddressLine(0, widgetDto.getAddressName());
								address.setCountryCode(widgetDto.getCountryCode());

								requestObj = new RequestObj(address);

								selectedLocationRequestMap.put(widgetDto.getAddressName(), requestObj);
								addressList.add(widgetDto.getAddressName());
							}
							requestObj.weatherDataTypeSet.addAll(widgetCreator.getRequestWeatherDataTypeSet());
							requestObj.weatherProviderTypeSet.addAll(widgetDto.getWeatherProviderTypeSet());
							requestObj.appWidgetSet.add(widgetDto.getAppWidgetId());
						}

					}

					allWidgetDtoArrayMap.putAll(currentLocationWidgetDtoArrayMap);
					allWidgetDtoArrayMap.putAll(selectedLocationWidgetDtoArrayMap);

					showProgressBar();

					requestCount = 0;
					responseCount = 0;

					if (!currentLocationWidgetDtoArrayMap.isEmpty()) {
						requestCount++;
						loadCurrentLocation();
					}
					if (!selectedLocationWidgetDtoArrayMap.isEmpty()) {
						requestCount += addressList.size();
						loadWeatherData(LocationType.SelectedAddress, addressList);
					}
				}

				@Override
				public void onResultNoData() {

				}
			});
		}
		return START_NOT_STICKY;
	}

	private void showProgressBar() {
		final Set<Integer> appWidgetIdSet = allWidgetDtoArrayMap.keySet();
		for (Integer appWidgetId : appWidgetIdSet) {
			RemoteViews remoteViews = remoteViewsArrayMap.get(appWidgetId);
			RemoteViewsUtil.onBeginProcess(remoteViews);
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		}
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
		} else if (widgetProviderClassName.equals(SeventhWidgetProvider.class.getName())) {
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


	private void stopService() {
		Log.d(TAG, "stopService");
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		stopForeground(true);
		stopSelf();
	}

	private void setRefreshPendingIntent(int appWidgetId) {
		widgetCreatorMap.get(appWidgetId).setRefreshPendingIntent(allWidgetProviderClassArrayMap.get(appWidgetId), remoteViewsArrayMap.get(appWidgetId)
		);
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
							onLocationResponse(Fail.FAILED_FIND_LOCATION, null);
						} else {
							final Set<Integer> appWidgetIdSet = currentLocationWidgetDtoArrayMap.keySet();
							final Address newAddress = addressList.get(0);
							final String newAddressName = newAddress.getAddressLine(0);

							if (!currentLocationRequestMap.containsKey(newAddressName)) {
								currentLocationRequestMap.put(newAddressName, new RequestObj(newAddress));
							}

							final RequestObj requestObj = currentLocationRequestMap.get(newAddressName);

							for (Integer appWidgetId : appWidgetIdSet) {
								WidgetDto widgetDto = currentLocationWidgetDtoArrayMap.get(appWidgetId);

								widgetDto.setAddressName(newAddressName);
								widgetDto.setCountryCode(newAddress.getCountryCode());
								widgetDto.setLatitude(newAddress.getLatitude());
								widgetDto.setLongitude(newAddress.getLongitude());
								widgetRepository.update(widgetDto, null);

								requestObj.weatherDataTypeSet.addAll(widgetCreatorMap.get(appWidgetId).getRequestWeatherDataTypeSet());
								requestObj.weatherProviderTypeSet.addAll(widgetDto.getWeatherProviderTypeSet());
								requestObj.appWidgetSet.add(appWidgetId);
							}

							onLocationResponse(null, newAddressName);
						}
					}
				});
			}

			@Override
			public void onFailed(Fail fail) {
				onLocationResponse(fail, null);
			}
		};

		MainThreadWorker.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				FusedLocation fusedLocation = FusedLocation.getInstance(getApplicationContext());
				PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
				if (powerManager.isInteractive()) {
					fusedLocation.findCurrentLocation(locationCallback, true);
				} else {
					LocationResult lastLocation = fusedLocation.getLastCurrentLocation();
					if(lastLocation == null){
						fusedLocation.findCurrentLocation(locationCallback, true);
					}else{
						locationCallback.onSuccessful(lastLocation);
					}
				}
			}
		});
	}

	private void onLocationResponse(@Nullable FusedLocation.MyLocationCallback.Fail fail, @Nullable String addressName) {
		if (fail == null) {
			List<String> addressesList = new ArrayList<>();
			addressesList.add(addressName);
			loadWeatherData(LocationType.CurrentLocation, addressesList);
			Log.d(TAG, "response CurrentLocation : " + addressName);
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
			Log.d(TAG, "requestCurrentLocation Failed : " + errorType.name());
			onResponseResult(LocationType.CurrentLocation, null);
		}

	}


	private void loadWeatherData(LocationType locationType, List<String> addressList) {
		for (String addressName : addressList) {
			MultipleRestApiDownloader multipleRestApiDownloader = new MultipleRestApiDownloader() {
				@Override
				public void onResult() {
					onResponseResult(locationType, addressName);
				}

				@Override
				public void onCanceled() {
					onResponseResult(locationType, addressName);
				}
			};

			Log.d(TAG, "request weather data");
			RequestObj requestObj = null;

			if (locationType == LocationType.SelectedAddress) {
				requestObj = selectedLocationRequestMap.get(addressName);
				selectedLocationResponseMap.put(addressName, multipleRestApiDownloader);
			} else {
				requestObj = currentLocationRequestMap.get(addressName);
				currentLocationResponseMap.put(addressName, multipleRestApiDownloader);
			}

			WeatherRequestUtil.loadWeatherData(getApplicationContext(), executorService, requestObj.address.getLatitude(),
					requestObj.address.getLongitude(), requestObj.weatherDataTypeSet, multipleRestApiDownloader,
					requestObj.weatherProviderTypeSet);
		}

	}

	private void onResponseResult(LocationType locationType, @Nullable String addressName) {
		if (addressName != null) {
			Map<String, MultipleRestApiDownloader> responseMap = null;
			Map<String, WidgetForegroundService.RequestObj> requestObjMap = null;

			if (locationType == LocationType.SelectedAddress) {
				requestObjMap = selectedLocationRequestMap;
				responseMap = selectedLocationResponseMap;
			} else {
				requestObjMap = currentLocationRequestMap;
				responseMap = currentLocationResponseMap;
			}
			RequestObj requestObj = requestObjMap.get(addressName);

			Set<Integer> appWidgetIdSet = requestObj.appWidgetSet;

			for (Integer appWidgetId : appWidgetIdSet) {
				AbstractWidgetCreator widgetCreator = widgetCreatorMap.get(appWidgetId);
				widgetCreator.setWidgetDto(allWidgetDtoArrayMap.get(appWidgetId));
				widgetCreator.setResultViews(appWidgetId, remoteViewsArrayMap.get(appWidgetId), responseMap.get(addressName));
			}

			//응답 처리가 끝난 요청객체는 제거
			requestObjMap.remove(addressName);
		}

		if (++responseCount == requestCount) {
			stopService();
			Log.d(TAG, "response Count : " + responseCount + " requestCount : " + requestCount);
		}
	}

	private static class RequestObj {
		final Address address;
		Set<Integer> appWidgetSet = new HashSet<>();
		Set<WeatherDataType> weatherDataTypeSet = new HashSet<>();
		Set<WeatherProviderType> weatherProviderTypeSet = new HashSet<>();

		public RequestObj(Address address) {
			this.address = address;
		}
	}
}