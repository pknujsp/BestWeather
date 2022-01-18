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

	public WidgetHelper(Context context, Class<?> widgetProviderClass) {
		this.context = context;
		this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		this.widgetProviderClass = widgetProviderClass;
	}


	public void onSelectedAutoRefreshInterval(long val, int appWidgetId) {
		cancelAutoRefresh(appWidgetId);

		if (val == 0) {
			return;
		}

		Intent refreshIntent = new Intent(context, widgetProviderClass);
		refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));
		Bundle bundle = new Bundle();
		bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		refreshIntent.putExtras(bundle);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId + 10000, refreshIntent,
				Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ?
						PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
						PendingIntent.FLAG_UPDATE_CURRENT);

		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
				val, pendingIntent);
	}

	public void cancelAutoRefresh(int appWidgetId) {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId + 10000, new Intent(context, widgetProviderClass),
				Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ?
						PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
						PendingIntent.FLAG_UPDATE_CURRENT);
		if (pendingIntent != null) {
			alarmManager.cancel(pendingIntent);
			pendingIntent.cancel();
		}
	}

}
