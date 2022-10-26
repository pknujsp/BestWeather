package com.lifedawn.bestweather.commons.classes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.internal.ConnectionCallbacks;
import com.google.android.gms.common.api.internal.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.timezone.TimeZoneUtils;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class FusedLocation implements ConnectionCallbacks, OnConnectionFailedListener {
	private FusedLocationProviderClient fusedLocationClient;
	private LocationManager locationManager;
	private Context context;
	private NetworkStatus networkStatus;
	private Map<MyLocationCallback, LocationRequestObj> locationRequestObjMap = new ConcurrentHashMap<>();
	private TimerTask timerTask;
	private Timer timer = new Timer();

	public FusedLocation(Context context) {
		this.context = context;
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		networkStatus = NetworkStatus.getInstance(context);
	}

	@Override
	public void onConnected(@Nullable @org.jetbrains.annotations.Nullable Bundle bundle) {
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(@NonNull @NotNull ConnectionResult connectionResult) {

	}

	public LocationResult getLastCurrentLocation() {
		List<Location> locations = new ArrayList<>();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		double latitude = Double.parseDouble(
				sharedPreferences.getString(context.getString(R.string.pref_key_last_current_location_latitude), "0.0"));
		double longitude = Double.parseDouble(
				sharedPreferences.getString(context.getString(R.string.pref_key_last_current_location_longitude), "0.0"));

		Location location = new Location("");
		location.setLatitude(latitude);
		location.setLongitude(longitude);

		locations.add(location);
		return LocationResult.create(locations);
	}

	public void findCurrentLocation(MyLocationCallback myLocationCallback, boolean isBackground) {
		if (!isOnGps()) {
			myLocationCallback.onFailed(MyLocationCallback.Fail.DISABLED_GPS);
		} else if (!isOnNetwork()) {
			myLocationCallback.onFailed(MyLocationCallback.Fail.FAILED_FIND_LOCATION);
		} else {
			if (checkDefaultPermissions()) {
				if (isBackground && !checkBackgroundLocationPermission()) {
					myLocationCallback.onFailed(MyLocationCallback.Fail.DENIED_ACCESS_BACKGROUND_LOCATION_PERMISSION);
					return;
				}
				notifyNotification();
				CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

				LocationRequest locationRequest =
						new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY).build();

				final LocationCallback locationCallback = new LocationCallback() {
					@Override
					public void onLocationResult(@NonNull @NotNull LocationResult locationResult) {
						if (timerTask != null)
							timerTask.cancel();

						cancelNotification();
						locationRequestObjMap.remove(myLocationCallback);
						fusedLocationClient.removeLocationUpdates(this);

						if (locationResult != null) {
							if (locationResult.getLocations().size() > 0) {
								final Location location = myLocationCallback.getBestLocation(locationResult);

								final Double latitude = location.getLatitude();
								final Double longitude = location.getLongitude();

								TimeZoneUtils.Companion.getTimeZone(latitude, longitude, zoneId -> onResultTimeZone(latitude, longitude, zoneId, myLocationCallback, locationResult));

							} else {
								myLocationCallback.onFailed(MyLocationCallback.Fail.FAILED_FIND_LOCATION);
							}
						} else {
							myLocationCallback.onFailed(MyLocationCallback.Fail.FAILED_FIND_LOCATION);
						}
					}

					@Override
					public void onLocationAvailability(@NonNull @NotNull LocationAvailability locationAvailability) {
						super.onLocationAvailability(locationAvailability);
					}
				};

				timerTask = new TimerTask() {
					@Override
					public void run() {
						cancelNotification();
						locationRequestObjMap.remove(myLocationCallback);
						cancellationTokenSource.cancel();
						fusedLocationClient.removeLocationUpdates(locationCallback);

						MainThreadWorker.runOnUiThread(() -> {
							myLocationCallback.onFailed(MyLocationCallback.Fail.TIME_OUT);
						});
					}
				};

				ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
				ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);

				final LocationRequestObj locationRequestObj = new LocationRequestObj();
				locationRequestObjMap.put(myLocationCallback, locationRequestObj);

				timer.schedule(timerTask, 6000L);

				@SuppressLint("MissingPermission")
				Task<Location> currentLocationTask = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,
						cancellationTokenSource.getToken());

				locationRequestObj.currentLocationTask = currentLocationTask;
				locationRequestObj.cancellationTokenSource = cancellationTokenSource;


				currentLocationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
					@SuppressLint("MissingPermission")
					@Override
					public void onSuccess(@NonNull @NotNull Location location) {
						if (!currentLocationTask.isCanceled()) {
							if (location == null) {
								locationRequestObj.locationCallback = locationCallback;
								ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
								ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
								fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
							} else {
								List<Location> locations = new ArrayList<>();
								locations.add(location);

								final Double latitude = location.getLatitude();
								final Double longitude = location.getLongitude();

								TimeZoneUtils.Companion.getTimeZone(latitude, longitude, new TimeZoneUtils.TimeZoneCallback() {
									@Override
									public void onResult(@NonNull ZoneId zoneId) {
										onResultTimeZone(latitude, longitude, zoneId, new MyLocationCallback() {
											@Override
											public void onSuccessful(LocationResult locationResult) {
												locationCallback.onLocationResult(locationResult);
											}

											@Override
											public void onFailed(Fail fail) {
											}
										}, LocationResult.create(locations));
									}
								});
							}
						}

					}
				});

			} else {
				myLocationCallback.onFailed(MyLocationCallback.Fail.DENIED_LOCATION_PERMISSIONS);
			}
		}
	}

	private void onResultTimeZone(Double latitude, Double longitude, ZoneId zoneId, MyLocationCallback myLocationCallback,
	                              LocationResult locationResult) {
		final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putString(context.getString(R.string.pref_key_last_current_location_latitude),
						latitude.toString())
				.putString(context.getString(R.string.pref_key_last_current_location_longitude),
						longitude.toString())
				.putString("zoneId", zoneId.getId()).commit();

		MainThreadWorker.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				myLocationCallback.onSuccessful(locationResult);
			}
		});
	}


	public boolean checkDefaultPermissions() {
		return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
	}

	public boolean checkBackgroundLocationPermission() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
			return true;
		} else {
			return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
		}
	}

	public boolean isOnGps() {
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	public boolean isOnNetwork() {
		return networkStatus.networkAvailable();
	}

	public void onDisabledGps(Activity activity, LocationLifeCycleObserver locationLifeCycleObserver,
	                          ActivityResultCallback<ActivityResult> gpsResultCallback) {
		new MaterialAlertDialogBuilder(activity).setMessage(activity.getString(R.string.request_to_make_gps_on)).setPositiveButton(
				activity.getString(R.string.check), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface paramDialogInterface, int paramInt) {
						locationLifeCycleObserver.launchGpsLauncher(IntentUtil.getLocationSettingsIntent(), gpsResultCallback);
					}
				}).setNegativeButton(activity.getString(R.string.no), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
			}
		}).setCancelable(false).create().show();
	}

	public void onRejectPermissions(Activity activity, LocationLifeCycleObserver locationLifeCycleObserver,
	                                ActivityResultCallback<ActivityResult> appSettingsResultCallback,
	                                ActivityResultCallback<Map<String, Boolean>> permissionsResultCallback) {
		// 다시 묻지 않음을 선택했는지 확인
		final boolean neverAskAgain = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
				context.getString(R.string.pref_key_never_ask_again_permission_for_access_location), false);

		if (neverAskAgain) {
			locationLifeCycleObserver.launchAppSettingsLauncher(IntentUtil.getAppSettingsIntent(activity), appSettingsResultCallback);
		} else {
			locationLifeCycleObserver.launchPermissionsLauncher(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
					Manifest.permission.ACCESS_COARSE_LOCATION}, permissionsResultCallback);
		}
	}


	public void cancel(MyLocationCallback myLocationCallback) {
		if (locationRequestObjMap.containsKey(myLocationCallback)) {
			cancelNotification();
			LocationRequestObj locationRequestObj = locationRequestObjMap.get(myLocationCallback);
			if (timerTask != null)
				timerTask.cancel();

			if (Objects.requireNonNull(locationRequestObj).locationCallback != null) {
				fusedLocationClient.removeLocationUpdates(locationRequestObj.locationCallback);
			}
			if (Objects.requireNonNull(locationRequestObj).currentLocationTask != null) {
				Objects.requireNonNull(locationRequestObj).cancellationTokenSource.cancel();
			}
			locationRequestObjMap.remove(myLocationCallback);
		}
	}

	private void notifyNotification() {
		NotificationHelper notificationHelper = new NotificationHelper(context);
		NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(NotificationType.Location);

		NotificationCompat.Builder builder = notificationObj.getNotificationBuilder();
		builder.setSmallIcon(R.drawable.location).setContentText(context.getString(R.string.msg_finding_current_location))
				.setContentTitle(context.getString(R.string.current_location))
				.setOngoing(false);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			builder.setPriority(NotificationCompat.PRIORITY_LOW).setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		}

		Notification notification = notificationObj.getNotificationBuilder().build();
		NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
		notificationManager.notify(NotificationType.Location.getNotificationId(), notification);
	}

	private void cancelNotification() {
		NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
		notificationManager.cancel(NotificationType.Location.getNotificationId());
	}

	public interface MyLocationCallback {
		enum Fail {
			DISABLED_GPS, DENIED_LOCATION_PERMISSIONS, FAILED_FIND_LOCATION, DENIED_ACCESS_BACKGROUND_LOCATION_PERMISSION, TIME_OUT
		}

		void onSuccessful(LocationResult locationResult);

		void onFailed(Fail fail);

		default Location getBestLocation(LocationResult locationResult) {
			int bestIndex = 0;
			float accuracy = Float.MIN_VALUE;
			List<Location> locations = locationResult.getLocations();

			for (int i = 0; i < locations.size(); i++) {
				if (locations.get(i).getAccuracy() > accuracy) {
					accuracy = locations.get(i).getAccuracy();
					bestIndex = i;
				}
			}
			return locations.get(bestIndex);
		}
	}

	private static class LocationRequestObj {
		LocationCallback locationCallback;
		Task<Location> currentLocationTask;
		CancellationTokenSource cancellationTokenSource;
	}
}
