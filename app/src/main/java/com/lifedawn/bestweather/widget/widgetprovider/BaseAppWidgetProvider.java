package com.lifedawn.bestweather.widget.widgetprovider;

import android.app.ActivityManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.forremoteviews.RemoteViewsUtil;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.room.repository.WidgetRepository;
import com.lifedawn.bestweather.widget.WidgetHelper;
import com.lifedawn.bestweather.widget.creator.AbstractWidgetCreator;
import com.lifedawn.bestweather.widget.foreground.WidgetForegroundService;

import java.util.List;

public class BaseAppWidgetProvider extends AppWidgetProvider {
	protected AppWidgetManager appWidgetManager;

	protected Class<?> getJobServiceClass() {
		return null;
	}

	protected AbstractWidgetCreator getWidgetCreatorInstance(Context context, int appWidgetId) {
		return null;
	}

	protected void reDraw(Context context, int[] appWidgetIds, Class<?> widgetProviderClass) {
		if (appWidgetManager == null) {
			appWidgetManager = AppWidgetManager.getInstance(context);
		}

		for (int i = 0; i < appWidgetIds.length; i++) {
			final int appWidgetId = appWidgetIds[i];

			if (appWidgetManager.getAppWidgetInfo(appWidgetId) == null) {
				continue;
			}

			AbstractWidgetCreator widgetCreator = getWidgetCreatorInstance(context, appWidgetId);
			widgetCreator.loadSavedSettings(new DbQueryCallback<WidgetDto>() {
				@Override
				public void onResultSuccessful(WidgetDto widgetDto) {
					if (widgetDto != null) {
						if (widgetDto.isInitialized()) {
							if (widgetDto.isLoadSuccessful()) {
								widgetCreator.setDataViewsOfSavedData();
							} else {
								RemoteViews remoteViews = widgetCreator.createRemoteViews();

								widgetCreator.setRefreshPendingIntent(widgetProviderClass, remoteViews);
								RemoteViewsUtil.onErrorProcess(remoteViews, context, RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA);
								appWidgetManager.updateAppWidget(widgetCreator.getAppWidgetId(), remoteViews);
							}
						}
					} else {

					}

				}

				@Override
				public void onResultNoData() {

				}
			});
		}
	}

	@Override
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
		reDraw(context, new int[]{appWidgetId}, getClass());
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		reDraw(context, appWidgetIds, getClass());
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		WidgetHelper widgetHelper = new WidgetHelper(context);
		WidgetRepository widgetRepository = new WidgetRepository(context);

		for (int appWidgetId : appWidgetIds) {
			widgetHelper.cancelAutoRefresh();
			widgetRepository.delete(appWidgetId, null);
		}
		super.onDeleted(context, appWidgetIds);
	}


	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		MyApplication.loadValueUnits(context, false);
		Bundle bundle = intent.getExtras();
		final String action = intent.getAction();

		if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_INIT))) {
			Bundle argument = new Bundle();
			argument.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID));
			startService(context, action, argument);
		} else if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
			startService(context, action, null);
		} else if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
			WidgetRepository widgetRepository = new WidgetRepository(context);
			final String widgetProviderClassName = getClass().getName();
			final Class<?> widgetProviderClass = getClass();

			widgetRepository.getAll(widgetProviderClassName, new DbQueryCallback<List<WidgetDto>>() {
				@Override
				public void onResultSuccessful(List<WidgetDto> result) {
					appWidgetManager = AppWidgetManager.getInstance(context);
					WidgetHelper widgetHelper = new WidgetHelper(context);
					final int[] ids = new int[result.size()];

					int index = 0;
					for (WidgetDto widgetDto : result) {
						ids[index++] = widgetDto.getAppWidgetId();
					}

					reDraw(context, ids, widgetProviderClass);

					long widgetRefreshInterval = widgetHelper.getRefreshInterval();
					if (widgetRefreshInterval > 0L && !widgetHelper.isRepeating()) {
						widgetHelper.onSelectedAutoRefreshInterval(widgetRefreshInterval);
					}

				}

				@Override
				public void onResultNoData() {

				}
			});
		}
	}

	protected boolean isServiceRunning(Context context) {
		final String serviceName = WidgetForegroundService.class.getName();
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceName.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	protected void startService(Context context, String action, @Nullable Bundle bundle) {
		if (isServiceRunning(context)) {
			Toast.makeText(context, R.string.runningUpdateService, Toast.LENGTH_SHORT).show();
		} else {
			Intent intent = new Intent(context, WidgetForegroundService.class);
			intent.setAction(action);
			if (bundle != null) {
				intent.putExtras(bundle);
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				context.startForegroundService(intent);
			} else {
				context.startService(intent);
			}
		}
	}


}