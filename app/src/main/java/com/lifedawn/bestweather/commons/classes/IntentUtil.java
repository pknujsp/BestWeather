package com.lifedawn.bestweather.commons.classes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.lifedawn.bestweather.main.MainActivity;

public class IntentUtil {
	private IntentUtil() {
	}

	public static Intent getNotificationSettingsIntent(Context context) {
		Intent intent = new Intent();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
			intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
		} else {
			intent.putExtra("app_package", context.getPackageName());
			intent.putExtra("app_uid", context.getApplicationInfo().uid);
		}
		return intent;
	}

	public static Intent getAppSettingsIntent(Context context) {
		Intent intent = new Intent();
		intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		intent.setData(Uri.fromParts("package", context.getPackageName(), null));
		return intent;
	}

	public static Intent getLocationSettingsIntent() {
		Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		return intent;
	}

	public static Intent getAppIntent(Context context) {
		Intent intent = new Intent(context, MainActivity.class);
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		return intent;
	}
}
