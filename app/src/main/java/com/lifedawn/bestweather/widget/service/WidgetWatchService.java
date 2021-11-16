package com.lifedawn.bestweather.widget.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.widget.ConfigureWidgetActivity;
import com.lifedawn.bestweather.widget.RootAppWidget;
import com.lifedawn.bestweather.widget.WidgetCurrent;
import com.lifedawn.bestweather.widget.WidgetCurrentDaily;
import com.lifedawn.bestweather.widget.WidgetCurrentHourly;
import com.lifedawn.bestweather.widget.WidgetCurrentHourlyDaily;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class WidgetWatchService extends Service {
	public WidgetWatchService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("widget timetick", "timetick");
		ZonedDateTime now = ZonedDateTime.now();
		Context context = getApplicationContext();

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(getString(R.string.date_pattern));
		ValueUnits clockUnit =
				ValueUnits.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(getString(R.string.pref_key_unit_clock), ValueUnits.clock12.name()));
		DateTimeFormatter timeFormatter =
				DateTimeFormatter.ofPattern(getString(clockUnit == ValueUnits.clock12 ? R.string.clock_12_pattern :
						R.string.clock_24_pattern));

		Bundle bundle = intent.getExtras();
		ZoneId zoneId = null;
		ZonedDateTime tempNow = null;
		ZonedDateTime zonedDateTime = null;
		String[] widgetClassNames = {WidgetCurrent.class.getName(), WidgetCurrentHourly.class.getName(),
				WidgetCurrentHourlyDaily.class.getName(), WidgetCurrentDaily.class.getName()};
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

		for (String widgetClassName : widgetClassNames) {
			ComponentName componentName = new ComponentName(context.getPackageName(), widgetClassName);
			int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);

			for (int appWidgetId : appWidgetIds) {
				SharedPreferences sharedPreferences = context.getSharedPreferences(ConfigureWidgetActivity.WidgetAttributes.WIDGET_ATTRIBUTES_ID.name() + appWidgetId, Context.MODE_PRIVATE);

				if (sharedPreferences.getBoolean(ConfigureWidgetActivity.WidgetAttributes.DISPLAY_DATETIME.name(), false)) {
					boolean displayLocalDateTime =
							sharedPreferences.getBoolean(ConfigureWidgetActivity.WidgetAttributes.DISPLAY_LOCAL_DATETIME.name(), false);

					zoneId = displayLocalDateTime ? ZoneId.of(sharedPreferences.getString(RootAppWidget.WidgetDataKeys.TIMEZONE_ID.name(), ""))
							: ZoneId.systemDefault();

					tempNow = ZonedDateTime.of(now.toLocalDateTime(), now.getZone());
					zonedDateTime = tempNow.withZoneSameInstant(zoneId);

					RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
							appWidgetManager.getAppWidgetInfo(appWidgetId).initialLayout);
					remoteViews.setTextViewText(R.id.date, zonedDateTime.format(dateFormatter));
					remoteViews.setTextViewText(R.id.time, zonedDateTime.format(timeFormatter));

					appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
					Log.e("widget timetick", "zoneid : " + zoneId.getId());
					Log.e("widget timetick", "appwidget id :" + appWidgetId + " - " + zonedDateTime.toString());
				}
			}
		}


		return super.onStartCommand(intent, flags, startId);
	}

}