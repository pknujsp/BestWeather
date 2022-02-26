package com.lifedawn.bestweather.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.ArrayMap;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.room.repository.WidgetRepository;
import com.lifedawn.bestweather.widget.widgetprovider.AbstractAppWidgetProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public void reDrawWidgets() {
		WidgetRepository widgetRepository = new WidgetRepository(context);

		widgetRepository.getAll(new DbQueryCallback<List<WidgetDto>>() {
			@Override
			public void onResultSuccessful(List<WidgetDto> result) {
				if (result.size() > 0) {
					ArrayMap<Class<?>, List<Integer>> widgetArrMap = new ArrayMap<>();
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

					int requestCode = 100000;
					for (Class<?> cls : widgetArrMap.keySet()) {
						Intent refreshIntent = null;
						try {
							refreshIntent = new Intent(context, cls);
							refreshIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

							Bundle bundle = new Bundle();
							List<Integer> idList = widgetArrMap.valueAt(widgetArrMap.indexOfKey(cls));
							int[] ids = new int[idList.size()];
							int index = 0;

							for (Integer id : idList) {
								ids[index++] = id;

								if (widgetDtoMap.get(id).getUpdateIntervalMillis() > 0) {
									if (!isRepeating(id, cls)) {
										onSelectedAutoRefreshInterval(widgetDtoMap.get(id).getUpdateIntervalMillis(), id, cls);
									}
								}
							}
							bundle.putIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
							refreshIntent.putExtras(bundle);

							PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode++, refreshIntent,
									PendingIntent.FLAG_ONE_SHOT);
							pendingIntent.send();

						} catch (PendingIntent.CanceledException e) {
							e.printStackTrace();
						}
					}


				}
			}

			@Override
			public void onResultNoData() {

			}
		});
	}
}
