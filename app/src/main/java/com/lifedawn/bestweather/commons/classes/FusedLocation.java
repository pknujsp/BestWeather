package com.lifedawn.bestweather.commons.classes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.internal.ConnectionCallbacks;
import com.google.android.gms.common.api.internal.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;

import org.jetbrains.annotations.NotNull;
import org.xml.sax.helpers.AttributesImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class FusedLocation implements ConnectionCallbacks, OnConnectionFailedListener {
	private static FusedLocation instance;
	private FusedLocationProviderClient fusedLocationClient;
	private LocationManager locationManager;
	private Context context;
	private Map<MyLocationCallback, LocationRequestObj> locationRequestObjMap = new HashMap<>();

	public static FusedLocation getInstance(Context context) {
		if (instance == null) {
			instance = new FusedLocation(context);
		}
		return instance;
	}

	public static void close() {
		instance = null;
	}

	private FusedLocation(Context context) {
		this.context = context;
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
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

				LocationRequest locationRequest = LocationRequest.create();
				locationRequest.setInterval(1000);
				locationRequest.setFastestInterval(200);
				locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

				Timer timer = new Timer();

				final LocationCallback locationCallback = new LocationCallback() {
					@Override
					public void onLocationResult(@NonNull @NotNull LocationResult locationResult) {
						timer.cancel();
						locationRequestObjMap.remove(myLocationCallback);
						fusedLocationClient.removeLocationUpdates(this);

						if (locationResult != null) {
							if (locationResult.getLocations().size() > 0) {
								final Location location = myLocationCallback.getBestLocation(locationResult);

								final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
								editor.putString(context.getString(R.string.pref_key_last_current_location_latitude),
										String.valueOf(location.getLatitude())).putString(
										context.getString(R.string.pref_key_last_current_location_longitude),
										String.valueOf(location.getLongitude())).commit();

								myLocationCallback.onSuccessful(locationResult);
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

				CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

				ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
				ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);

				final LocationRequestObj locationRequestObj = new LocationRequestObj();
				locationRequestObjMap.put(myLocationCallback, locationRequestObj);

				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						locationRequestObjMap.remove(myLocationCallback);
						cancellationTokenSource.cancel();
						fusedLocationClient.removeLocationUpdates(locationCallback);
						myLocationCallback.onFailed(MyLocationCallback.Fail.TIME_OUT);
					}
				}, 6000L);

				@SuppressLint("MissingPermission")
				Task<Location> currentLocationTask = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,
						cancellationTokenSource.getToken());

				locationRequestObj.currentLocationTask = currentLocationTask;
				locationRequestObj.cancellationTokenSource = cancellationTokenSource;
				locationRequestObj.timer = timer;

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

								final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
								editor.putString(context.getString(R.string.pref_key_last_current_location_latitude),
										String.valueOf(location.getLatitude())).putString(
										context.getString(R.string.pref_key_last_current_location_longitude),
										String.valueOf(location.getLongitude())).commit();

								locationCallback.onLocationResult(LocationResult.create(locations));
							}
						}

					}
				});

				Log.e("FusedLocation", "requestLocationUpdates");
			} else {
				myLocationCallback.onFailed(MyLocationCallback.Fail.DENIED_LOCATION_PERMISSIONS);
			}
		}
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
		return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
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

	public boolean availablePlayServices() {
		return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
	}

	public void cancel(MyLocationCallback myLocationCallback) {
		if (locationRequestObjMap.containsKey(myLocationCallback)) {
			LocationRequestObj locationRequestObj = locationRequestObjMap.get(myLocationCallback);
			Objects.requireNonNull(locationRequestObj).timer.cancel();

			if (Objects.requireNonNull(locationRequestObj).locationCallback != null) {
				fusedLocationClient.removeLocationUpdates(locationRequestObj.locationCallback);
			}
			if (Objects.requireNonNull(locationRequestObj).currentLocationTask != null) {
				Objects.requireNonNull(locationRequestObj).cancellationTokenSource.cancel();
			}

			locationRequestObjMap.remove(myLocationCallback);
		}
	}

	public void startForeground(Service service) {
		NotificationHelper notificationHelper = new NotificationHelper(context);
		NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(NotificationType.Location);

		NotificationCompat.Builder builder = notificationObj.getNotificationBuilder();
		builder.setSmallIcon(R.drawable.location).setContentText(context.getString(R.string.msg_finding_current_location))
				.setContentTitle(context.getString(R.string.current_location))
				.setOngoing(true);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			builder.setPriority(NotificationCompat.PRIORITY_LOW).setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		}

		Notification notification = notificationObj.getNotificationBuilder().build();
		service.startForeground(NotificationType.Location.getNotificationId(), notification);
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
		Timer timer;
	}
}
