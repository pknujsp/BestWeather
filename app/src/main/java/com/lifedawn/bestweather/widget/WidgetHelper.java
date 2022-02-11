package com.lifedawn.bestweather.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.widget.widgetprovider.AbstractAppWidgetProvider;

public class WidgetHelper {
	private Context context;
	private AlarmManager alarmManager;
	private Class<?> widgetProviderClass;

	public WidgetHelper(Context context) {
		this.context = context;
		this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}


	public void onSelectedAutoRefreshInterval(long val, int appWidgetId, Class<?> widgetProviderClass) {
		cancelAutoRefresh(appWidgetId, widgetProviderClass);

		if (val > 0) {
			Intent refreshIntent = new Intent(context, widgetProviderClass);
			refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));
			Bundle bundle = new Bundle();
			bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			refreshIntent.putExtras(bundle);

			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId + 10000, refreshIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);

			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), val, pendingIntent);
		}
	}

	public void cancelAutoRefresh(int appWidgetId, Class<?> widgetProviderClass) {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId + 10000, new Intent(context, widgetProviderClass),
				PendingIntent.FLAG_NO_CREATE);
		if (pendingIntent != null) {
			alarmManager.cancel(pendingIntent);
			pendingIntent.cancel();
		}
	}

	public boolean isRepeating(int appWidgetId, Class<?> widgetProviderClass) {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId + 10000, new Intent(context, widgetProviderClass),
				PendingIntent.FLAG_NO_CREATE);
		if (pendingIntent != null) {
			return true;
		} else {
			return false;
		}
	}

}
