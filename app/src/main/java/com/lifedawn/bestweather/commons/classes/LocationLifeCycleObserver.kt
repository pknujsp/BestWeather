package com.lifedawn.bestweather.commons.classes;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class LocationLifeCycleObserver implements DefaultLifecycleObserver {
	private final ActivityResultRegistry mRegistry;
	private final Context context;

	private Activity activity;
	private ActivityResultLauncher<Intent> requestOnGpsLauncher;
	private ActivityResultLauncher<Intent> moveToAppDetailSettingsLauncher;
	private ActivityResultLauncher<Intent> backgroundLocationPermissionLauncher;
	private ActivityResultLauncher<String[]> requestLocationPermissionLauncher;

	private ActivityResultCallback<ActivityResult> gpsResultCallback;
	private ActivityResultCallback<ActivityResult> appSettingsResultCallback;
	private ActivityResultCallback<ActivityResult> backgroundLocationPermissionResultCallback;
	private ActivityResultCallback<Map<String, Boolean>> permissionResultCallback;

	public LocationLifeCycleObserver(@NonNull ActivityResultRegistry mRegistry, Activity activity) {
		this.mRegistry = mRegistry;
		this.activity = activity;
		this.context = activity.getApplicationContext();
	}

	@Override
	public void onCreate(@NonNull @NotNull LifecycleOwner owner) {
		requestOnGpsLauncher = mRegistry.register("gps", owner, new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
			@Override
			public void onActivityResult(ActivityResult result) {
				if (activity == null || activity.isFinishing())
					return;

				//gps 사용확인 화면에서 나온뒤 현재 위치 다시 파악
				if (gpsResultCallback != null) {
					gpsResultCallback.onActivityResult(result);
				}

			}
		});

		moveToAppDetailSettingsLauncher = mRegistry.register("appSettings", new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
			@Override
			public void onActivityResult(ActivityResult result) {
				if (activity == null || activity.isFinishing())
					return;

				if (appSettingsResultCallback != null) {
					appSettingsResultCallback.onActivityResult(result);
				}

				if (ContextCompat.checkSelfPermission(context,
						Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
						ContextCompat.checkSelfPermission(context,
								Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
					PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(
							activity.getString(R.string.pref_key_never_ask_again_permission_for_access_location), false).apply();
				}

			}
		});

		requestLocationPermissionLauncher = mRegistry.register("locationPermissions", new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
			@Override
			public void onActivityResult(Map<String, Boolean> result) {
				if (activity == null || activity.isFinishing())
					return;

				if (permissionResultCallback != null) {
					permissionResultCallback.onActivityResult(result);
				}
				//gps사용 권한
				//허가남 : 현재 위치 다시 파악
				//거부됨 : 작업 취소
				//계속 거부 체크됨 : 작업 취소
				if (!result.containsValue(false)) {
					PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(
							activity.getString(R.string.pref_key_never_ask_again_permission_for_access_location), false).apply();
				} else {
					if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)
							|| !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
						PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(
								activity.getString(R.string.pref_key_never_ask_again_permission_for_access_location), true).apply();
					}
				}
			}
		});

		backgroundLocationPermissionLauncher = mRegistry.register("backgroundLocationPermission", new ActivityResultContracts.StartActivityForResult(),
				new ActivityResultCallback<ActivityResult>() {
					@Override
					public void onActivityResult(ActivityResult result) {
						if (activity == null || activity.isFinishing())
							return;
						if (backgroundLocationPermissionResultCallback != null) {
							backgroundLocationPermissionResultCallback.onActivityResult(result);
						}


					}
				});
	}

	@Override
	public void onStart(@NonNull LifecycleOwner owner) {
		DefaultLifecycleObserver.super.onStart(owner);
	}

	@Override
	public void onResume(@NonNull LifecycleOwner owner) {
		DefaultLifecycleObserver.super.onResume(owner);
	}

	@Override
	public void onPause(@NonNull LifecycleOwner owner) {
		DefaultLifecycleObserver.super.onPause(owner);
	}

	@Override
	public void onStop(@NonNull LifecycleOwner owner) {
		DefaultLifecycleObserver.super.onStop(owner);
	}

	@Override
	public void onDestroy(@NonNull LifecycleOwner owner) {
		DefaultLifecycleObserver.super.onDestroy(owner);
		activity = null;
	}

	public void launchGpsLauncher(Intent intent, @NonNull ActivityResultCallback<ActivityResult> callback) {
		gpsResultCallback = callback;
		requestOnGpsLauncher.launch(intent);
	}

	public void launchAppSettingsLauncher(Intent intent, @NonNull ActivityResultCallback<ActivityResult> callback) {
		appSettingsResultCallback = callback;
		moveToAppDetailSettingsLauncher.launch(intent);
	}

	public void launchPermissionsLauncher(String[] input, @NonNull ActivityResultCallback<Map<String, Boolean>> callback) {
		permissionResultCallback = callback;
		requestLocationPermissionLauncher.launch(input);
	}

	public void launchBackgroundLocationPermissionLauncher(Intent intent, @NonNull ActivityResultCallback<ActivityResult> callback) {
		backgroundLocationPermissionResultCallback = callback;
		backgroundLocationPermissionLauncher.launch(intent);
	}
}
