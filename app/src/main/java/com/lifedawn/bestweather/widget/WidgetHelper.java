package com.lifedawn.bestweather.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.ArrayMap;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.IntentRequestCodes;
import com.lifedawn.bestweather.commons.interfaces.BackgroundWorkCallback;
import com.lifedawn.bestweather.commons.interfaces.Callback;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.room.repository.WidgetRepository;
import com.lifedawn.bestweather.widget.widgetprovider.FirstWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.FirstWidgetProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WidgetHelper {
	private Context context;
	private final AlarmManager alarmManager;

	public WidgetHelper(Context context) {
		this.context = context;
		this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}


	public void onSelectedAutoRefreshInterval(long val) {
		cancelAutoRefresh();

		if (val > 0) {
			Intent refreshIntent = new Intent(context, FirstWidgetProvider.class);
			refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));

			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, IntentRequestCodes.WIDGET_AUTO_REFRESH.requestCode, refreshIntent,
					PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + val, val, pendingIntent);
		}
	}

	public void cancelAutoRefresh() {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, IntentRequestCodes.WIDGET_AUTO_REFRESH.requestCode, new Intent(context, FirstWidgetProvider.class),
				PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

		if (pendingIntent != null) {
			alarmManager.cancel(pendingIntent);
			pendingIntent.cancel();
		}
	}

	public boolean isRepeating() {
		return PendingIntent.getBroadcast(context, IntentRequestCodes.WIDGET_AUTO_REFRESH.requestCode, new Intent(context, FirstWidgetProvider.class),
				PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE) != null;
	}

	public void reDrawWidgets(@Nullable BackgroundWorkCallback callback) {
		WidgetRepository widgetRepository = WidgetRepository.getINSTANCE();
		widgetRepository.getAll(new DbQueryCallback<List<WidgetDto>>() {
			@Override
			public void onResultSuccessful(List<WidgetDto> result) {
				if (result.size() > 0) {
					Map<Class<?>, List<Integer>> widgetArrMap = new HashMap<>();
					Map<Integer, WidgetDto> widgetDtoMap = new HashMap<>();
					AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

					for (WidgetDto widgetDto : result) {
						final AppWidgetProviderInfo appWidgetProviderInfo = appWidgetManager.getAppWidgetInfo(widgetDto.getAppWidgetId());
						ComponentName componentName = appWidgetProviderInfo.provider;
						final String providerClassName = componentName.getClassName();
						try {
							Class<?> cls = Class.forName(providerClassName);

							if (!widgetArrMap.containsKey(cls)) {
								widgetArrMap.put(cls, new ArrayList<>());
							}
							widgetArrMap.get(cls).add(widgetDto.getAppWidgetId());
							widgetDtoMap.put(widgetDto.getAppWidgetId(), widgetDto);
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
							return;
						}
					}

					int requestCode = 200000;
					Intent refreshIntent = null;

					for (Class<?> cls : widgetArrMap.keySet()) {
						try {
							refreshIntent = new Intent(context, cls);
							refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REDRAW));

							Bundle bundle = new Bundle();
							List<Integer> idList = widgetArrMap.get(cls);

							int[] ids = new int[idList.size()];
							int i = 0;
							for (Integer id : idList) {
								ids[i++] = id;
							}

							bundle.putIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
							refreshIntent.putExtras(bundle);

							PendingIntent.getBroadcast(context, requestCode++, refreshIntent,
									PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE).send();
						} catch (PendingIntent.CanceledException e) {
							e.printStackTrace();
						}
					}

					long widgetRefreshInterval = getRefreshInterval();
					if (widgetRefreshInterval > 0L) {
						if (!isRepeating()) {
							onSelectedAutoRefreshInterval(widgetRefreshInterval);
						}
					}


				}

				if (callback != null)
					callback.onFinished();
			}

			@Override
			public void onResultNoData() {
				if (callback != null)
					callback.onFinished();
			}
		});
	}

	public Long getRefreshInterval() {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getLong(context.getString(R.string.pref_key_widget_refresh_interval), 0L);
	}

}
