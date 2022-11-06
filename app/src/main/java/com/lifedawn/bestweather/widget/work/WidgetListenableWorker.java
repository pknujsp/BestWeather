package com.lifedawn.bestweather.widget.work;

import android.app.Notification;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.LocationResult;
import com.google.common.util.concurrent.ListenableFuture;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.commons.interfaces.BackgroundWorkCallback;
import com.lifedawn.bestweather.forremoteviews.RemoteViewsUtil;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.retrofit.util.WeatherRestApiDownloader;
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

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WidgetListenableWorker extends ListenableWorker {
	private final Map<Integer, WidgetDto> currentLocationWidgetDtoArrayMap = new ConcurrentHashMap<>();
	private final Map<Integer, WidgetDto> selectedLocationWidgetDtoArrayMap = new ConcurrentHashMap<>();
	private final Map<Integer, WidgetDto> allWidgetDtoArrayMap = new ConcurrentHashMap<>();
	private final Map<Integer, WeatherRestApiDownloader> multipleRestApiDownloaderMap = new ConcurrentHashMap<>();
	private final Map<Integer, AbstractWidgetCreator> widgetCreatorMap = new ConcurrentHashMap<>();

	private WeatherRestApiDownloader currentLocationResponseWeatherRestApiDownloader;
	private final Map<String, WeatherRestApiDownloader> selectedLocationResponseMap = new ConcurrentHashMap<>();

	private RequestObj currentLocationRequestObj;
	private final Map<String, RequestObj> selectedLocationRequestMap = new ConcurrentHashMap<>();

	private WidgetRepository widgetRepository;
	private AppWidgetManager appWidgetManager;
	private final ExecutorService executorService = MyApplication.getExecutorService();
	private FusedLocation fusedLocation;
	private int requestCount;
	private final AtomicInteger responseCount = new AtomicInteger(0);
	private final String ACTION;
	private final int APP_WIDGET_ID;

	public static AtomicBoolean processing = new AtomicBoolean(false);


	public WidgetListenableWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
		processing.set(true);

		if (widgetRepository == null) {
			widgetRepository = WidgetRepository.getINSTANCE();
		}
		if (appWidgetManager == null) {
			appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
		}

		Data parameterData = workerParams.getInputData();
		ACTION = parameterData.getString("action");
		APP_WIDGET_ID = parameterData.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);

		currentLocationRequestObj = null;
		currentLocationWidgetDtoArrayMap.clear();

		selectedLocationRequestMap.clear();
		selectedLocationResponseMap.clear();
		selectedLocationWidgetDtoArrayMap.clear();
		allWidgetDtoArrayMap.clear();

		multipleRestApiDownloaderMap.clear();
		widgetCreatorMap.clear();
	}

	@NonNull
	@Override
	public ListenableFuture<Result> startWork() {
		processing.set(true);


		return CallbackToFutureAdapter.getFuture(completer -> {
			final BackgroundWorkCallback backgroundWorkCallback = () -> {
				completer.set(Result.success());
			};


			if (ACTION.equals(getApplicationContext().getString(R.string.com_lifedawn_bestweather_action_INIT))) {
				widgetRepository.get(APP_WIDGET_ID, new DbQueryCallback<WidgetDto>() {
					@Override
					public void onResultSuccessful(WidgetDto widgetDto) {
						AbstractWidgetCreator widgetCreator = createWidgetViewCreator(APP_WIDGET_ID, widgetDto.getWidgetProviderClassName());
						WidgetHelper widgetHelper = new WidgetHelper(getApplicationContext());
						long widgetRefreshInterval = widgetHelper.getRefreshInterval();

						if (widgetRefreshInterval > 0 && !widgetHelper.isRepeating()) {
							widgetHelper.onSelectedAutoRefreshInterval(widgetRefreshInterval);
						}

						requestCount = 1;

						final AbstractWidgetCreator.RemoteViewsCallback remoteViewsCallback =
								new AbstractWidgetCreator.RemoteViewsCallback(requestCount) {

									@Override
									protected void onFinished(Map<Integer, RemoteViews> remoteViewsMap) {
										processing.set(false);


										for (int id : remoteViewsMap.keySet()) {
											appWidgetManager.updateAppWidget(id, remoteViewsMap.get(id));
										}
										backgroundWorkCallback.onFinished();
									}
								};

						widgetCreator.setRemoteViewsCallback(remoteViewsCallback);
						if (widgetDto.getLocationType() == LocationType.CurrentLocation) {
							currentLocationWidgetDtoArrayMap.put(widgetDto.getAppWidgetId(), widgetDto);
							allWidgetDtoArrayMap.putAll(currentLocationWidgetDtoArrayMap);

							showProgressBar();
							loadCurrentLocation(backgroundWorkCallback);
						} else {
							selectedLocationWidgetDtoArrayMap.put(widgetDto.getAppWidgetId(), widgetDto);
							Geocoding.AddressDto address = new Geocoding.AddressDto(widgetDto.getLatitude(), widgetDto.getLongitude(),
									widgetDto.getAddressName(), null, widgetDto.getCountryCode());

							final RequestObj requestObj = new RequestObj(address, ZoneId.of(widgetDto.getTimeZoneId()));
							requestObj.weatherDataTypeSet.addAll(widgetCreator.getRequestWeatherDataTypeSet());
							requestObj.weatherProviderTypeSet.addAll(widgetDto.getWeatherProviderTypeSet());
							requestObj.appWidgetSet.add(widgetDto.getAppWidgetId());

							selectedLocationRequestMap.put(widgetDto.getAddressName(), requestObj);
							List<String> addressList = new ArrayList<>();
							addressList.add(widgetDto.getAddressName());
							allWidgetDtoArrayMap.putAll(selectedLocationWidgetDtoArrayMap);

							showProgressBar();
							loadWeatherData(LocationType.SelectedAddress, addressList, backgroundWorkCallback);
						}
					}

					@Override
					public void onResultNoData() {
						backgroundWorkCallback.onFinished();
					}
				});

			} else if (ACTION.equals(getApplicationContext().getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
				widgetRepository.getAll(new DbQueryCallback<List<WidgetDto>>() {
					@Override
					public void onResultSuccessful(List<WidgetDto> list) {
						List<String> addressList = new ArrayList<>();

						final AbstractWidgetCreator.RemoteViewsCallback remoteViewsCallback =
								new AbstractWidgetCreator.RemoteViewsCallback(list.size()) {

									@Override
									protected void onFinished(Map<Integer, RemoteViews> remoteViewsMap) {
										processing.set(false);

										for (int id : remoteViewsMap.keySet()) {
											appWidgetManager.updateAppWidget(id, remoteViewsMap.get(id));
										}
										backgroundWorkCallback.onFinished();
									}
								};


						for (WidgetDto widgetDto : list) {
							AbstractWidgetCreator widgetCreator = createWidgetViewCreator(widgetDto.getAppWidgetId(),
									widgetDto.getWidgetProviderClassName());
							widgetCreator.setRemoteViewsCallback(remoteViewsCallback);

							if (widgetDto.getLocationType() == LocationType.CurrentLocation) {
								currentLocationWidgetDtoArrayMap.put(widgetDto.getAppWidgetId(), widgetDto);
							} else {
								selectedLocationWidgetDtoArrayMap.put(widgetDto.getAppWidgetId(), widgetDto);

								RequestObj requestObj = selectedLocationRequestMap.get(widgetDto.getAddressName());
								if (requestObj == null) {
									Geocoding.AddressDto address = new Geocoding.AddressDto(widgetDto.getLatitude(), widgetDto.getLongitude(),
											widgetDto.getAddressName(), null, widgetDto.getCountryCode());

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

						if (!currentLocationWidgetDtoArrayMap.isEmpty()) {
							requestCount++;
							loadCurrentLocation(backgroundWorkCallback);
						}
						if (!selectedLocationWidgetDtoArrayMap.isEmpty()) {
							requestCount = addressList.size();
							loadWeatherData(LocationType.SelectedAddress, addressList, backgroundWorkCallback);
						}

					}

					@Override
					public void onResultNoData() {
						backgroundWorkCallback.onFinished();
					}
				});
			}
			return backgroundWorkCallback;
		});


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
		processing.set(false);

		// WidgetHelper widgetHelper = new WidgetHelper(getApplicationContext());
		// widgetHelper.reDrawWidgets(null);
	}

	private void showProgressBar() {
		AbstractWidgetCreator tempWidgetCreator = new EighthWidgetCreator(getApplicationContext(), null, 0);
		RemoteViews tempRemoteViews = tempWidgetCreator.createRemoteViews();

		for (Integer appWidgetId : allWidgetDtoArrayMap.keySet())
			appWidgetManager.updateAppWidget(appWidgetId, tempRemoteViews);
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

		widgetCreatorMap.put(appWidgetId, widgetCreator);

		return widgetCreator;
	}


	public void loadCurrentLocation(BackgroundWorkCallback backgroundWorkCallback) {
		final Set<Integer> appWidgetIdSet = currentLocationWidgetDtoArrayMap.keySet();
		currentLocationRequestObj = new RequestObj(null, null);

		for (Integer appWidgetId : appWidgetIdSet) {
			currentLocationRequestObj.weatherDataTypeSet.addAll(widgetCreatorMap.get(appWidgetId).getRequestWeatherDataTypeSet());
			currentLocationRequestObj.weatherProviderTypeSet.addAll(currentLocationWidgetDtoArrayMap.get(appWidgetId).getWeatherProviderTypeSet());
			currentLocationRequestObj.appWidgetSet.add(appWidgetId);
		}

		NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());

		final FusedLocation.MyLocationCallback locationCallback = new FusedLocation.MyLocationCallback() {
			@Override
			public void onSuccessful(LocationResult locationResult) {
				notificationHelper.cancelNotification(NotificationType.Location.getNotificationId());
				final Location location = getBestLocation(locationResult);

				Geocoding.nominatimReverseGeocoding(getApplicationContext(), location.getLatitude(), location.getLongitude(),
						new Geocoding.ReverseGeocodingCallback() {
							@Override
							public void onReverseGeocodingResult(Geocoding.AddressDto address) {
								if (address == null) {
									onLocationResponse(Fail.FAILED_FIND_LOCATION, null, backgroundWorkCallback);
								} else {
									final String addressName = address.displayName;
									currentLocationRequestObj.address = address;

									final String zoneIdText = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
											.getString("zoneId", "");
									currentLocationRequestObj.zoneId = ZoneId.of(zoneIdText);
									onResultCurrentLocation(addressName, address, backgroundWorkCallback);
								}
							}
						});
			}

			@Override
			public void onFailed(Fail fail) {
				notificationHelper.cancelNotification(NotificationType.Location.getNotificationId());
				onLocationResponse(fail, null, backgroundWorkCallback);
			}
		};

		fusedLocation = new FusedLocation(getApplicationContext());
		fusedLocation.findCurrentLocation(locationCallback, true);
	}

	private void onResultCurrentLocation(String addressName, Geocoding.AddressDto newAddress, BackgroundWorkCallback backgroundWorkCallback) {
		for (Integer appWidgetId : currentLocationWidgetDtoArrayMap.keySet()) {
			WidgetDto widgetDto = currentLocationWidgetDtoArrayMap.get(appWidgetId);

			widgetDto.setAddressName(addressName);
			widgetDto.setCountryCode(newAddress.countryCode);
			widgetDto.setLatitude(newAddress.latitude);
			widgetDto.setLongitude(newAddress.longitude);
			widgetDto.setTimeZoneId(currentLocationRequestObj.zoneId.getId());

			widgetRepository.update(widgetDto, null);
		}

		onLocationResponse(null, addressName, backgroundWorkCallback);
	}

	private void onLocationResponse(@Nullable FusedLocation.MyLocationCallback.Fail fail, @Nullable String addressName,
	                                BackgroundWorkCallback backgroundWorkCallback) {
		if (fail == null) {
			List<String> addressesList = new ArrayList<>();
			addressesList.add(addressName);
			loadWeatherData(LocationType.CurrentLocation, addressesList, backgroundWorkCallback);
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

			for (Integer appWidgetId : currentLocationWidgetDtoArrayMap.keySet()) {
				allWidgetDtoArrayMap.get(appWidgetId).setLastErrorType(errorType);
				allWidgetDtoArrayMap.get(appWidgetId).setLoadSuccessful(false);
			}
			onResponseResult(LocationType.CurrentLocation, null, backgroundWorkCallback);
		}

	}


	private void loadWeatherData(LocationType locationType, List<String> addressList, BackgroundWorkCallback backgroundWorkCallback) {
		for (String addressName : addressList) {
			WeatherRestApiDownloader weatherRestApiDownloader = new WeatherRestApiDownloader() {
				@Override
				public void onResult() {
					onResponseResult(locationType, addressName, backgroundWorkCallback);
				}

				@Override
				public void onCanceled() {
					onResponseResult(locationType, addressName, backgroundWorkCallback);
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

			WeatherRequestUtil.loadWeatherData(getApplicationContext(), executorService, requestObj.address.latitude,
					requestObj.address.longitude, requestObj.weatherDataTypeSet, weatherRestApiDownloader,
					requestObj.weatherProviderTypeSet, weatherRestApiDownloader.getZoneId());
		}
	}

	private void onResponseResult(LocationType locationType, String addressName, BackgroundWorkCallback backgroundWorkCallback) {
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
			widgetCreator.setResultViews(appWidgetId,
					multipleRestApiDownloaderMap.get(appWidgetId), restApiDownloader.getZoneId());
		}

		//응답 처리가 끝난 요청객체를 제거
		if (addressName != null && locationType == LocationType.SelectedAddress) {
			selectedLocationRequestMap.remove(addressName);
		} else if (locationType == LocationType.CurrentLocation) {
			currentLocationRequestObj = null;
		}

		/*
		if (responseCount.incrementAndGet() == requestCount) {
			backgroundWorkCallback.onFinished();
		}

		 */
	}


	private static class RequestObj {
		Geocoding.AddressDto address;
		ZoneId zoneId;
		Set<Integer> appWidgetSet = new HashSet<>();
		Set<WeatherDataType> weatherDataTypeSet = new HashSet<>();
		Set<WeatherProviderType> weatherProviderTypeSet = new HashSet<>();

		public RequestObj(Geocoding.AddressDto address, ZoneId zoneId) {
			this.address = address;
			this.zoneId = zoneId;
		}
	}
}
