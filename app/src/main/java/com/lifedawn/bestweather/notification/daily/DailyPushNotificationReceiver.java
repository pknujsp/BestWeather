package com.lifedawn.bestweather.notification.daily;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.alarm.AlarmActivity;
import com.lifedawn.bestweather.alarm.alarmnotifications.AlarmService;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.room.dto.AlarmDto;

public class DailyPushNotificationReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Bundle arguments = intent.getExtras();

		if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
			Intent dailyNotificationService = new Intent(context, DailyNotificationService.class);
			Bundle bundle = new Bundle();

			bundle.putInt(BundleKey.dtoId.name(), arguments.getInt(BundleKey.dtoId.name()));
			bundle.putString("DailyPushNotificationType", arguments.getString("DailyPushNotificationType"));

			dailyNotificationService.putExtras(bundle);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				context.startForegroundService(dailyNotificationService);
			} else {
				context.startService(dailyNotificationService);
			}
		} else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {

		}

	}
}