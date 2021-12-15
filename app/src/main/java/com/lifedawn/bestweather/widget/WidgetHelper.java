package com.lifedawn.bestweather.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.notification.NotificationReceiver;
import com.lifedawn.bestweather.notification.NotificationType;

public class WidgetHelper {
	private Context context;
	private AlarmManager alarmManager;

	public WidgetHelper(Context context) {
		this.context = context;
		this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}


	public void onSelectedAutoRefreshInterval(long val, int appWidgetId, Class<?> widgetReceiverClass) {
		cancelAutoRefresh(appWidgetId, widgetReceiverClass);

		if (val == 0) {
			return;
		}

		Intent refreshIntent = new Intent(context, widgetReceiverClass);
		refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));
		Bundle bundle = new Bundle();
		bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		refreshIntent.putExtras(bundle);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId + 10000, refreshIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
				val, pendingIntent);
	}

	public void cancelAutoRefresh(int appWidgetId, Class<?> widgetReceiverClass) {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId + 10000, new Intent(context, widgetReceiverClass),
				PendingIntent.FLAG_NO_CREATE);
		if (pendingIntent != null) {
			alarmManager.cancel(pendingIntent);
			pendingIntent.cancel();
		}
	}

	public boolean repeating(int appWidgetId, Class<?> widgetReceiverClass) {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId + 10000, new Intent(context, widgetReceiverClass),
				PendingIntent.FLAG_NO_CREATE);
		return pendingIntent != null;
	}

}
