package com.lifedawn.bestweather.widget.foreground;

import android.app.Notification;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.LocationResult;
import com.google.common.util.concurrent.ListenableFuture;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.forremoteviews.RemoteViewsUtil;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.model.timezone.TimeZoneIdDto;
import com.lifedawn.bestweather.model.timezone.TimeZoneIdRepository;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.retrofit.responses.freetime.FreeTimeResponse;
import com.lifedawn.bestweather.retrofit.util.JsonDownloader;
import com.lifedawn.bestweather.retrofit.util.WeatherRestApiDownloader;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.room.repository.WidgetRepository;
import com.lifedawn.bestweather.timezone.FreeTimeZoneApi;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
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

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import retrofit2.Response;

public class WidgetWorker extends Worker {
	public static final Set<Integer> PROCESSING_WIDGET_ID_SET = new CopyOnWriteArraySet<>();

	private final Map<Integer, WidgetDto> currentLocationWidgetDtoArrayMap = new ConcurrentHashMap<>();
	private final Map<Integer, WidgetDto> selectedLocationWidgetDtoArrayMap = new ConcurrentHashMap<>();
	private final Map<Integer, WidgetDto> allWidgetDtoArrayMap = new ConcurrentHashMap<>();
	private final Map<Integer, Class<?>> allWidgetProviderClassArrayMap = new ConcurrentHashMap<>();
	private final Map<Integer, RemoteViews> remoteViewsArrayMap = new ConcurrentHashMap<>();
	private final Map<Integer, WeatherRestApiDownloader> multipleRestApiDownloaderMap = new ConcurrentHashMap<>();
	private Map<Integer, AbstractWidgetCreator> widgetCreatorMap = new ConcurrentHashMap<>();

	private WeatherRestApiDownloader currentLocationResponseWeatherRestApiDownloader;
	private final Map<String, WeatherRestApiDownloader> selectedLocationResponseMap = new ConcurrentHashMap<>();

	private RequestObj currentLocationRequestObj;
	private final Map<String, RequestObj> selectedLocationRequestMap = new ConcurrentHashMap<>();

	private WidgetRepository widgetRepository;
	private AppWidgetManager appWidgetManager;
	private static ExecutorService executorService = MyApplication.getExecutorService();
	private FusedLocation fusedLocation;
	private int requestCount;
	private int responseCount;
	private String action;
	private int appWidgetId;

	private TimeZoneIdRepository timeZoneIdRepository;


	public WidgetWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);

		if (widgetRepository == null) {
			widgetRepository = new WidgetRepository(getApplicationContext());
		}
		if (appWidgetManager == null) {
			appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
		}

		timeZoneIdRepository = TimeZoneIdRepository.Companion.getINSTANCE();
		action = workerParams.getInputData().getString("action");
		appWidgetId = workerParams.getInputData().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);

		PROCESSING_WIDGET_ID_SET.clear();
		currentLocationRequestObj = null;
		currentLocationWidgetDtoArrayMap.clear();

		selectedLocationRequestMap.clear();
		selectedLocationResponseMap.clear();
		selectedLocationWidgetDtoArrayMap.clear();
		allWidgetDtoArrayMap.clear();
		allWidgetProviderClassArrayMap.clear();

		remoteViewsArrayMap.clear();
		multipleRestApiDownloaderMap.clear();
		widgetCreatorMap.clear();
	}

	@NonNull
	@Override
	public Result doWork() {
		if (action.equals(getApplicationContext().getString(R.string.com_lifedawn_bestweather_action_INIT))) {
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

					PROCESSING_WIDGET_ID_SET.add(widgetDto.getAppWidgetId());
					Log.e("init widgets", widgetDto.getAppWidgetId() + "");

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

						final RequestObj requestObj = new RequestObj(address, ZoneId.of(widgetDto.getTimeZoneId()));
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

		} else if (action.equals(getApplicationContext().getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
			widgetRepository.getAll(new DbQueryCallback<List<WidgetDto>>() {
				@Override
				public void onResultSuccessful(List<WidgetDto> list) {
					List<String> addressList = new ArrayList<>();

					for (WidgetDto widgetDto : list) {
						PROCESSING_WIDGET_ID_SET.add(widgetDto.getAppWidgetId());

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

								requestObj = new RequestObj(address, ZoneId.of(widgetDto.getTimeZoneId()));

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

		return Result.success();
	}

	@NonNull
	@Override
	public ListenableFuture<ForegroundInfo> getForegroundInfoAsync() {
		NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
		NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(NotificationType.ForegroundService);

		NotificationCompat.Builder builder = notificationObj.getNotificationBuilder();
		builder.setSmallIcon(R.mipmap.ic_launcher_round).setContentText(getApplicationContext().getString(R.string.updatingWidgets)).setContentTitle(
						getApplicationContext().getString(R.string.updatingWidgets))
				.setOnlyAlertOnce(true).setWhen(0).setOngoing(true);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			builder.setPriority(NotificationCompat.PRIORITY_LOW).setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		}

		Notification notification = notificationObj.getNotificationBuilder().build();
		final int notificationId = (int) System.currentTimeMillis();

		ForegroundInfo foregroundInfo = new ForegroundInfo(notificationId, notification);

		return new ListenableFuture<ForegroundInfo>() {
			@Override
			public void addListener(Runnable listener, Executor executor) {

			}

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return false;
			}

			@Override
			public boolean isCancelled() {
				return false;
			}

			@Override
			public boolean isDone() {
				return true;
			}

			@Override
			public ForegroundInfo get() throws ExecutionException, InterruptedException {
				return foregroundInfo;
			}

			@Override
			public ForegroundInfo get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
				return foregroundInfo;
			}
		};
	}

	@Override
	public void onStopped() {
		super.onStopped();
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


	private void setRefreshPendingIntent(int appWidgetId) {
		widgetCreatorMap.get(appWidgetId).setRefreshPendingIntent(allWidgetProviderClassArrayMap.get(appWidgetId), remoteViewsArrayMap.get(appWidgetId));
	}


	public void loadCurrentLocation() {
		final Set<Integer> appWidgetIdSet = currentLocationWidgetDtoArrayMap.keySet();
		currentLocationRequestObj = new RequestObj(null, null);

		for (Integer appWidgetId : appWidgetIdSet) {
			currentLocationRequestObj.weatherDataTypeSet.addAll(widgetCreatorMap.get(appWidgetId).getRequestWeatherDataTypeSet());
			currentLocationRequestObj.weatherProviderTypeSet.addAll(currentLocationWidgetDtoArrayMap.get(appWidgetId).getWeatherProviderTypeSet());
			currentLocationRequestObj.appWidgetSet.add(appWidgetId);
		}

		fusedLocation = FusedLocation.getInstance(getApplicationContext());
		fusedLocation.startNotification(getApplicationContext());

		NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());

		final FusedLocation.MyLocationCallback locationCallback = new FusedLocation.MyLocationCallback() {
			@Override
			public void onSuccessful(LocationResult locationResult) {
				notificationHelper.cancelNotification(NotificationType.Location.getNotificationId());
				final Location location = getBestLocation(locationResult);


				Geocoding.geocoding(getApplicationContext(), location.getLatitude(), location.getLongitude(), new Geocoding.GeocodingCallback() {
					@Override
					public void onGeocodingResult(List<Address> addressList) {
						if (addressList.isEmpty()) {
							onLocationResponse(Fail.FAILED_FIND_LOCATION, null);
						} else {
							final Address newAddress = addressList.get(0);
							final String addressName = newAddress.getAddressLine(0);
							currentLocationRequestObj.address = newAddress;

							SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();

							timeZoneIdRepository.get(addressName, new DbQueryCallback<TimeZoneIdDto>() {
								@Override
								public void onResultSuccessful(TimeZoneIdDto result) {
									currentLocationRequestObj.zoneId = ZoneId.of(result.getTimeZoneId());
									editor.putString("zoneId", currentLocationRequestObj.zoneId.getId())
											.commit();

									onResultCurrentLocation(addressName, newAddress);
								}

								@Override
								public void onResultNoData() {
									FreeTimeZoneApi.Companion.getTimeZone(location.getLatitude(), location.getLongitude(), new JsonDownloader() {
										@Override
										public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
											FreeTimeResponse freeTimeDto = (FreeTimeResponse) responseObj;
											currentLocationRequestObj.zoneId = ZoneId.of(freeTimeDto.getTimezone());
											timeZoneIdRepository.insert(new TimeZoneIdDto(addressName, currentLocationRequestObj.zoneId.getId()));
											editor.putString("zoneId", currentLocationRequestObj.zoneId.getId()).commit();
											onResultCurrentLocation(addressName, newAddress);
										}

										@Override
										public void onResponseResult(Throwable t) {
											currentLocationRequestObj.zoneId = WeatherResponseProcessor.getZoneId(location.getLatitude(), location.getLongitude());

											timeZoneIdRepository.insert(new TimeZoneIdDto(addressName, currentLocationRequestObj.zoneId.getId()));
											editor.putString("zoneId", currentLocationRequestObj.zoneId.getId()).commit();

											onResultCurrentLocation(addressName, newAddress);
										}
									});

								}
							});

						}
					}
				});
			}

			@Override
			public void onFailed(Fail fail) {
				notificationHelper.cancelNotification(NotificationType.Location.getNotificationId());
				onLocationResponse(fail, null);
			}
		};

		PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);

		if (powerManager.isInteractive()) {
			fusedLocation.findCurrentLocation(locationCallback, true);
		} else {
			LocationResult lastLocation = fusedLocation.getLastCurrentLocation();
			if (lastLocation.getLocations().get(0).getLatitude() == 0.0 ||
					lastLocation.getLocations().get(0).getLongitude() == 0.0) {
				fusedLocation.findCurrentLocation(locationCallback, true);
			} else {
				locationCallback.onSuccessful(lastLocation);
			}
		}

	}

	private void onResultCurrentLocation(String addressName, Address newAddress) {
		for (Integer appWidgetId : currentLocationWidgetDtoArrayMap.keySet()) {
			WidgetDto widgetDto = currentLocationWidgetDtoArrayMap.get(appWidgetId);

			widgetDto.setAddressName(addressName);
			widgetDto.setCountryCode(newAddress.getCountryCode());
			widgetDto.setLatitude(newAddress.getLatitude());
			widgetDto.setLongitude(newAddress.getLongitude());
			widgetDto.setTimeZoneId(currentLocationRequestObj.zoneId.getId());

			widgetRepository.update(widgetDto, null);
		}

		onLocationResponse(null, addressName);
	}

	private void onLocationResponse(@Nullable FusedLocation.MyLocationCallback.Fail fail, @Nullable String addressName) {
		if (fail == null) {
			List<String> addressesList = new ArrayList<>();
			addressesList.add(addressName);
			loadWeatherData(LocationType.CurrentLocation, addressesList);
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
				allWidgetDtoArrayMap.get(appWidgetId).setLastErrorType(errorType);
				allWidgetDtoArrayMap.get(appWidgetId).setLoadSuccessful(false);
			}
			onResponseResult(LocationType.CurrentLocation, null);
		}

	}


	private void loadWeatherData(LocationType locationType, List<String> addressList) {
		for (String addressName : addressList) {
			WeatherRestApiDownloader weatherRestApiDownloader = new WeatherRestApiDownloader() {
				@Override
				public void onResult() {
					onResponseResult(locationType, addressName);
				}

				@Override
				public void onCanceled() {
					onResponseResult(locationType, addressName);
				}
			};

			RequestObj requestObj = null;

			if (locationType == LocationType.SelectedAddress) {
				requestObj = selectedLocationRequestMap.get(addressName);
				selectedLocationResponseMap.put(addressName, weatherRestApiDownloader);
			} else {
				requestObj = currentLocationRequestObj;
				currentLocationResponseWeatherRestApiDownloader = weatherRestApiDownloader;

				boolean onlyKma = true;

				for (Integer appWidgetId : currentLocationWidgetDtoArrayMap.keySet()) {
					if (currentLocationWidgetDtoArrayMap.get(appWidgetId).isTopPriorityKma() &&
							currentLocationWidgetDtoArrayMap.get(appWidgetId).getCountryCode().equals("KR")) {
						requestObj.weatherProviderTypeSet.add(WeatherProviderType.KMA_WEB);
					} else if (!currentLocationWidgetDtoArrayMap.get(appWidgetId).isTopPriorityKma()) {
						onlyKma = false;
					}
				}

				if (onlyKma) {
					requestObj.weatherProviderTypeSet.remove(WeatherProviderType.ACCU_WEATHER);
					requestObj.weatherProviderTypeSet.remove(WeatherProviderType.MET_NORWAY);
					requestObj.weatherProviderTypeSet.remove(WeatherProviderType.OWM_ONECALL);
				}
			}
			weatherRestApiDownloader.setZoneId(requestObj.zoneId);

			WeatherRequestUtil.loadWeatherData(getApplicationContext(), executorService, requestObj.address.getLatitude(),
					requestObj.address.getLongitude(), requestObj.weatherDataTypeSet, weatherRestApiDownloader,
					requestObj.weatherProviderTypeSet, weatherRestApiDownloader.getZoneId());
		}
	}

	private void onResponseResult(LocationType locationType, String addressName) {
		WeatherRestApiDownloader restApiDownloader = null;
		Set<Integer> appWidgetIdSet = null;

		if (locationType == LocationType.SelectedAddress) {
			restApiDownloader = selectedLocationResponseMap.get(addressName);
			appWidgetIdSet = selectedLocationRequestMap.get(addressName).appWidgetSet;
		} else {
			restApiDownloader = currentLocationResponseWeatherRestApiDownloader;
			appWidgetIdSet = currentLocationRequestObj.appWidgetSet;
		}

		for (int appWidgetId : appWidgetIdSet) {
			multipleRestApiDownloaderMap.put(appWidgetId, restApiDownloader);

			AbstractWidgetCreator widgetCreator = widgetCreatorMap.get(appWidgetId);
			widgetCreator.setWidgetDto(allWidgetDtoArrayMap.get(appWidgetId));
			widgetCreator.setResultViews(appWidgetId, remoteViewsArrayMap.get(appWidgetId),
					multipleRestApiDownloaderMap.get(appWidgetId), restApiDownloader.getZoneId());
		}

		//응답 처리가 끝난 요청객체를 제거
		if (addressName != null && locationType == LocationType.SelectedAddress) {
			selectedLocationRequestMap.remove(addressName);
		} else if (locationType == LocationType.CurrentLocation) {
			currentLocationRequestObj = null;
		}

	}


	private static class RequestObj {
		Address address;
		ZoneId zoneId;
		Set<Integer> appWidgetSet = new HashSet<>();
		Set<WeatherDataType> weatherDataTypeSet = new HashSet<>();
		Set<WeatherProviderType> weatherProviderTypeSet = new HashSet<>();

		public RequestObj(Address address, ZoneId zoneId) {
			this.address = address;
			this.zoneId = zoneId;
		}
	}
}