package com.lifedawn.bestweather.ui.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.constants.IntentRequestCodes;
import com.lifedawn.bestweather.commons.interfaces.BackgroundWorkCallback;
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.data.local.room.dto.WidgetDto;
import com.lifedawn.bestweather.data.local.room.repository.WidgetRepository;
import com.lifedawn.bestweather.ui.widget.widgetprovider.FirstWidgetProvider;

import java.util.List;

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

	public void reDrawWidgets(@Nullable BackgroundWorkCallback callback, int... appWidgetIds) {
		WidgetRepository widgetRepository = WidgetRepository.getINSTANCE();
		widgetRepository.getAll(new DbQueryCallback<List<WidgetDto>>() {
			@Override
			public void onResultSuccessful(List<WidgetDto> result) {
				if (result.size() > 0) {
					long widgetRefreshInterval = getRefreshInterval();
					if (widgetRefreshInterval > 0L && !isRepeating())
						onSelectedAutoRefreshInterval(widgetRefreshInterval);

					int requestCode = 200000;

					Intent refreshIntent = new Intent(context, FirstWidgetProvider.class);
					refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REDRAW));

					Bundle bundle = new Bundle();
					final int[] ids = new int[result.size()];
					int idx = 0;
					for (WidgetDto widgetDto : result) {
						ids[idx++] = widgetDto.getAppWidgetId();
					}

					bundle.putIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
					refreshIntent.putExtras(bundle);

					try {
						PendingIntent.getBroadcast(context, requestCode, refreshIntent,
								PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE).send();
					} catch (PendingIntent.CanceledException e) {
						e.printStackTrace();
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
