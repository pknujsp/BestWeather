package com.lifedawn.bestweather.commons.classes;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.views.ProgressDialog;

public class Gps {
	private final LocationManager locationManager;
	private final Context context;

	private LocationListener locationListener;
	private AlertDialog dialog;
	private ActivityResultLauncher<Intent> requestOnGpsLauncher;
	private ActivityResultLauncher<String> requestLocationPermissionLauncher;
	private ActivityResultLauncher<Intent> moveToAppDetailSettingsLauncher;

	public Gps(Context context, ActivityResultLauncher<Intent> requestOnGpsLauncher, ActivityResultLauncher<String> requestLocationPermissionLauncher, ActivityResultLauncher<Intent> moveToAppDetailSettingsLauncher) {
		this.requestOnGpsLauncher = requestOnGpsLauncher;
		this.requestLocationPermissionLauncher = requestLocationPermissionLauncher;
		this.moveToAppDetailSettingsLauncher = moveToAppDetailSettingsLauncher;
		this.context = context;
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}

	public void runGps(Activity activity, LocationCallback callback) {
		//권한 확인
		dialog = ProgressDialog.show(activity, context.getString(R.string.msg_finding_current_location), null);

		final boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		final boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		final int permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);

		if (permission == PackageManager.PERMISSION_GRANTED) {
			if (isGpsEnabled) {
				HandlerThread handlerThread = new HandlerThread("LocationThread");
				handlerThread.start();
				Looper looper = handlerThread.getLooper();

				locationListener = new LocationListener() {
					boolean isCompleted = false;

					@Override
					public void onLocationChanged(Location location) {
						if (!isCompleted) {
							handlerThread.quit();
							isCompleted = true;
							clear();
							callback.onSuccessful(location);
						}
					}

					@Override
					public void onStatusChanged(String provider, int status, Bundle extras) {

					}

					@Override
					public void onProviderEnabled(String provider) {

					}

					@Override
					public void onProviderDisabled(String provider) {

					}
				};


				//locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener, looper);

				if (isNetworkEnabled) {
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener, looper);
				}
			} else {
				clear();
				showRequestGpsDialog(activity, callback, requestOnGpsLauncher);
			}
		} else {
			// 다시 묻지 않음을 선택했는지 확인
			final boolean neverAskAgain = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
					context.getString(R.string.pref_key_never_ask_again_permission_for_access_fine_location), false);
			clear();

			if (neverAskAgain) {
				Intent intent = new Intent();
				intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
				moveToAppDetailSettingsLauncher.launch(intent);
			} else {
				Toast.makeText(activity, R.string.message_needs_location_permission, Toast.LENGTH_SHORT).show();
				requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
			}

		}

	}

	public void runGps(Context context, LocationCallback callback) {
		//권한 확인
		final boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		final boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		int permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);

		HandlerThread handlerThread = new HandlerThread("LocationThread");
		handlerThread.start();
		Looper looper = handlerThread.getLooper();

		locationListener = new LocationListener() {
			boolean isCompleted = false;

			@Override
			public void onLocationChanged(Location location) {
				if (!isCompleted) {
					handlerThread.quit();
					isCompleted = true;
					clear();
					callback.onSuccessful(location);
				}
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {

			}

			@Override
			public void onProviderEnabled(String provider) {

			}

			@Override
			public void onProviderDisabled(String provider) {

			}
		};


		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener, looper);

		if (isNetworkEnabled) {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener, looper);
		}

	}


	public boolean checkPermissionAndGpsEnabled(Activity activity, LocationCallback locationCallback) {
		final int permission = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
		final boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if (permission == PackageManager.PERMISSION_GRANTED) {
			if (!isGpsEnabled) {
				showRequestGpsDialog(activity, locationCallback, requestOnGpsLauncher);
				return false;
			} else {
				return true;
			}
		} else {
			// 다시 묻지 않음을 선택했는지 확인
			final boolean neverAskAgain = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(
					activity.getString(R.string.pref_key_never_ask_again_permission_for_access_fine_location), false);

			if (neverAskAgain) {
				Intent intent = new Intent();
				intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
				moveToAppDetailSettingsLauncher.launch(intent);
			} else {
				Toast.makeText(activity, R.string.message_needs_location_permission, Toast.LENGTH_SHORT).show();
				requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
			}
			return false;
		}

	}

	public boolean checkPermissionAndGpsEnabled(Context context, LocationCallback locationCallback) {
		final int permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
		final boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if (permission == PackageManager.PERMISSION_GRANTED) {
			if (!isGpsEnabled) {
				locationCallback.onFailed(LocationCallback.Fail.DISABLED_GPS);
				return false;
			} else {
				return true;
			}
		} else {
			locationCallback.onFailed(LocationCallback.Fail.REJECT_PERMISSION);
			return false;
		}

	}

	private void showRequestGpsDialog(Activity activity, LocationCallback callback, ActivityResultLauncher<Intent> requestOnGpsLauncher) {
		new MaterialAlertDialogBuilder(activity).setMessage(activity.getString(R.string.request_to_make_gps_on)).setPositiveButton(
				activity.getString(R.string.check), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface paramDialogInterface, int paramInt) {
						requestOnGpsLauncher.launch(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				}).setNegativeButton(activity.getString(R.string.no), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				callback.onFailed(LocationCallback.Fail.DISABLED_GPS);
			}
		}).setCancelable(false).create().show();
	}

	private void closeDialog() {
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}

	public void clear() {
		if (locationListener != null) {
			locationManager.removeUpdates(locationListener);
			locationListener = null;
		}
		closeDialog();
	}

	public interface LocationCallback {
		enum Fail {
			DISABLED_GPS, REJECT_PERMISSION
		}

		void onSuccessful(Location location);

		void onFailed(Fail fail);
	}
}