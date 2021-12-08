package com.lifedawn.bestweather.notification.always;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.notification.NotificationReceiver;
import com.lifedawn.bestweather.notification.NotificationType;

public class AlwaysNotiHelper {
	private Context context;
	private AlarmManager alarmManager;

	public AlwaysNotiHelper(Context context) {
		this.context = context;
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}

	public void onSelectedAutoRefreshInterval(long val) {
		cancelAutoRefresh();

		if (val == 0) {
			return;
		}

		Intent refreshIntent = new Intent(context, NotificationReceiver.class);
		refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));
		Bundle bundle = new Bundle();
		bundle.putString(NotificationType.class.getName(), NotificationType.Always.name());

		refreshIntent.putExtras(bundle);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 11, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
				val, pendingIntent);
	}

	public void cancelAutoRefresh() {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 11, new Intent(context, NotificationReceiver.class), 0);
		if (pendingIntent != null) {
			alarmManager.cancel(pendingIntent);
			pendingIntent.cancel();
		}
	}

}
