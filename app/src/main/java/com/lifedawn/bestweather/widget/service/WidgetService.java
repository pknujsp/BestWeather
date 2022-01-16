package com.lifedawn.bestweather.widget.service;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.ArrayMap;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationResult;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.forremoteviews.RemoteViewProcessor;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.room.repository.WidgetRepository;

import java.util.List;

public class WidgetService extends Service {
	private ArrayMap<Integer, Boolean> succeedMap = new ArrayMap<>();

	public WidgetService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		final String action = intent.getAction();
		final Bundle bundle = intent.getExtras();

		if (action.equals(getString(R.string.com_lifedawn_bestweather_action_INIT)) ||
				action.equals(getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
			NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
			NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(NotificationType.Location);
			NotificationCompat.Builder notiBuilder = notificationObj.getNotificationBuilder();

			notiBuilder.setOngoing(false)
					.setPriority(NotificationCompat.PRIORITY_DEFAULT).setVibrate(new long[]{0L}).setDefaults(NotificationCompat.DEFAULT_LIGHTS |
					NotificationCompat.DEFAULT_SOUND).setSound(null).setSilent(true).setContentTitle(getString(R.string.findingCurrentLocationTitle))
					.setContentText(getString(R.string.msg_finding_current_location));

			//알림 표시
			startForeground(notificationObj.getNotificationId(), notiBuilder.build());

			final int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
			final String appWidgetProviderClassName = bundle.getString("appWidgetProviderClassName");

			succeedMap.put(appWidgetId, false);

			loadCurrentLocation(getApplicationContext(), appWidgetId, appWidgetProviderClassName);
		}

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void loadCurrentLocation(Context context, int appWidgetId, String appWidgetProviderClassName) {
		FusedLocation.MyLocationCallback locationCallback = new FusedLocation.MyLocationCallback() {
			@Override
			public void onSuccessful(LocationResult locationResult) {
				final Location location = locationResult.getLocations().get(0);
				Geocoding.geocoding(context, location.getLatitude(), location.getLongitude(), new Geocoding.GeocodingCallback() {
					@Override
					public void onGeocodingResult(List<Address> addressList) {
						if (addressList.isEmpty()) {
							onResult(Fail.FAILED_FIND_LOCATION, appWidgetId, appWidgetProviderClassName);
						} else {
							WidgetRepository widgetRepository = new WidgetRepository(context);
							widgetRepository.get(appWidgetId, new DbQueryCallback<WidgetDto>() {
								@Override
								public void onResultSuccessful(WidgetDto result) {
									final Address address = addressList.get(0);

									result.setAddressName(address.getAddressLine(0));
									result.setCountryCode(address.getCountryCode());
									result.setLatitude(address.getLatitude());
									result.setLongitude(address.getLongitude());

									widgetRepository.update(result, new DbQueryCallback<WidgetDto>() {
										@Override
										public void onResultSuccessful(WidgetDto result) {
											onResult(null, appWidgetId, appWidgetProviderClassName);
										}

										@Override
										public void onResultNoData() {

										}
									});
								}

								@Override
								public void onResultNoData() {

								}
							});

						}
					}
				});
			}

			@Override
			public void onFailed(Fail fail) {
				onResult(fail, appWidgetId, appWidgetProviderClassName);
			}
		};

		FusedLocation.getInstance(this).startLocationUpdates(locationCallback);
	}

	private void onResult(@Nullable FusedLocation.MyLocationCallback.Fail fail, int appWidgetId, String appWidgetProviderClassName) {
		Bundle bundle = new Bundle();
		bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		bundle.putBoolean("succeed", fail == null);
		if (fail != null) {
			bundle.putString("fail", fail.name());
		}

		Class<?> widgetProviderClass = null;
		try {
			widgetProviderClass = Class.forName(appWidgetProviderClassName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Intent intent = new Intent(getApplicationContext(), widgetProviderClass);
		intent.putExtras(bundle);
		intent.setAction(getString(R.string.com_lifedawn_bestweather_action_FOUND_CURRENT_LOCATION));

		try {
			PendingIntent.getBroadcast(getApplicationContext(), appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT).send();
		} catch (PendingIntent.CanceledException e) {
			e.printStackTrace();
		}
		succeedMap.put(appWidgetId, true);

		if (!succeedMap.containsValue(false)) {
			stopSelf();
		}
	}
}