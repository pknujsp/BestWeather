package com.lifedawn.bestweather.commons.classes;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.internal.ConnectionCallbacks;
import com.google.android.gms.common.api.internal.OnConnectionFailedListener;
import com.google.android.gms.common.api.internal.RegistrationMethods;
import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifedawn.bestweather.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class FusedLocation implements ConnectionCallbacks, OnConnectionFailedListener {
	private static FusedLocation instance;
	private static FusedLocationProviderClient fusedLocationClient;
	private static LocationManager locationManager;

	private Context context;

	public static FusedLocation getInstance(Context context) {
		if (instance == null) {
			instance = new FusedLocation(context);
		}
		return instance;
	}

	private FusedLocation(Context context) {
		this.context = context;
		if (fusedLocationClient == null) {
			fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
		}
		if (locationManager == null) {
			locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		}
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

	public void startLocationUpdates(MyLocationCallback myLocationCallback) {
		if (!isOnGps()) {
			myLocationCallback.onFailed(MyLocationCallback.Fail.DISABLED_GPS);
		} else if (!isOnNetwork()) {
			myLocationCallback.onFailed(MyLocationCallback.Fail.FAILED_FIND_LOCATION);
		} else {
			if (checkPermissions()) {
				LocationRequest locationRequest = LocationRequest.create();
				locationRequest.setInterval(300);
				locationRequest.setFastestInterval(150);
				locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

				Timer timer = new Timer();

				LocationCallback locationCallback = new LocationCallback() {
					@Override
					public void onLocationResult(@NonNull @NotNull LocationResult locationResult) {
						timer.cancel();
						fusedLocationClient.removeLocationUpdates(this);

						if (locationResult != null) {
							if (locationResult.getLocations().size() > 0) {
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

				ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
				ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);

				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						locationCallback.onLocationResult(LocationResult.create(new ArrayList<>()));
					}
				}, 3000L);

				fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
			} else {
				myLocationCallback.onFailed(MyLocationCallback.Fail.REJECT_PERMISSION);
			}
		}
	}

	public boolean checkPermissions() {
		int accessFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
		int accessCoarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);

		return accessCoarseLocation == PackageManager.PERMISSION_GRANTED &&
				accessFineLocation == PackageManager.PERMISSION_GRANTED;
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
						locationLifeCycleObserver.launchGpsLauncher(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), gpsResultCallback);
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
			Intent intent = new Intent();
			intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
			locationLifeCycleObserver.launchAppSettingsLauncher(intent, appSettingsResultCallback);
		} else {
			locationLifeCycleObserver.launchPermissionLauncher(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
					Manifest.permission.ACCESS_COARSE_LOCATION}, permissionsResultCallback);
		}
	}

	public boolean availablePlayServices() {
		return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
	}

	public interface MyLocationCallback {
		enum Fail {
			DISABLED_GPS, REJECT_PERMISSION, FAILED_FIND_LOCATION
		}

		void onSuccessful(LocationResult locationResult);

		void onFailed(Fail fail);
	}
}
