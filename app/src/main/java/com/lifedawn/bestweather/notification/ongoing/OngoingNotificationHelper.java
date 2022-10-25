package com.lifedawn.bestweather.notification.ongoing;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.IntentRequestCodes;

public class OngoingNotificationHelper {
	private Context context;
	private AlarmManager alarmManager;

	public OngoingNotificationHelper(Context context) {
		this.context = context;
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}

	public void onSelectedAutoRefreshInterval(long millis) {
		cancelAutoRefresh();

		if (millis > 0) {
			Intent refreshIntent = new Intent(context, OngoingNotificationReceiver.class);
			refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, IntentRequestCodes.ONGOING_NOTIFICATION_AUTO_REFRESH.requestCode, refreshIntent,
					PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + millis,
					millis, pendingIntent);
		}
	}

	public void cancelAutoRefresh() {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, IntentRequestCodes.ONGOING_NOTIFICATION_AUTO_REFRESH.requestCode, new Intent(context,
						OngoingNotificationReceiver.class)
				, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_MUTABLE);

		if (pendingIntent != null) {
			alarmManager.cancel(pendingIntent);
			pendingIntent.cancel();
		}
	}

	public boolean isRepeating() {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, IntentRequestCodes.ONGOING_NOTIFICATION_AUTO_REFRESH.requestCode, new Intent(context, OngoingNotificationReceiver.class)
				, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
		return pendingIntent != null;
	}


	public PendingIntent getRefreshPendingIntent() {
		Intent refreshIntent = new Intent(context, OngoingNotificationReceiver.class);
		refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));

		return PendingIntent.getBroadcast(context, IntentRequestCodes.ONGOING_NOTIFICATION_MANUALLY_REFRESH.requestCode, refreshIntent,
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
	}

	public PendingIntent createManualPendingIntent(String action, int flags) {
		Intent intent = new Intent(context, OngoingNotificationReceiver.class);
		intent.setAction(action);

		return PendingIntent.getBroadcast(context, IntentRequestCodes.ONGOING_NOTIFICATION_MANUALLY_REFRESH.requestCode,
				intent, flags);

	}
}
