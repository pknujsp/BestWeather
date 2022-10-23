package com.lifedawn.bestweather.widget.widgetprovider;

import android.app.ActivityManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.common.util.concurrent.ListenableFuture;
import com.lifedawn.bestweather.utils.DeviceUtils;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.forremoteviews.RemoteViewsUtil;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.room.repository.WidgetRepository;
import com.lifedawn.bestweather.widget.WidgetHelper;
import com.lifedawn.bestweather.widget.creator.AbstractWidgetCreator;
import com.lifedawn.bestweather.widget.foreground.WidgetForegroundService;
import com.lifedawn.bestweather.widget.foreground.WidgetListenableWorker;

import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class BaseAppWidgetProvider extends AppWidgetProvider {
	protected AppWidgetManager appWidgetManager;
	protected final String TAG = "WIDGET_PROVIDER";

	protected AbstractWidgetCreator getWidgetCreatorInstance(Context context, int appWidgetId) {
		return null;
	}

	protected void reDraw(Context context, int[] appWidgetIds, Class<?> widgetProviderClass) {
		if (appWidgetManager == null) {
			appWidgetManager = AppWidgetManager.getInstance(context);
		}

		for (int appWidgetId : appWidgetIds) {
			if (appWidgetManager.getAppWidgetInfo(appWidgetId) == null || WidgetListenableWorker.PROCESSING_WIDGET_ID_SET.contains(appWidgetId)) {
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
								RemoteViewsUtil.onErrorProcess(remoteViews, context, widgetDto.getLastErrorType());
								appWidgetManager.updateAppWidget(widgetCreator.getAppWidgetId(), remoteViews);
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

	@Override
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
		Log.e(TAG, "onAppWidgetOptionsChanged");
		reDraw(context, new int[]{appWidgetId}, getClass());
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Log.e(TAG, "onUpdate");

		for (int appWidgetId : appWidgetIds) {
			Log.e(TAG, "onUpdate : " + appWidgetId);
		}
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
		WidgetRepository widgetRepository = WidgetRepository.getINSTANCE();
		for (int appWidgetId : appWidgetIds) {
			widgetRepository.delete(appWidgetId, null);
		}

		appWidgetManager = AppWidgetManager.getInstance(context);

		if (appWidgetManager.getInstalledProviders().isEmpty()) {
			WidgetHelper widgetHelper = new WidgetHelper(context);
			widgetHelper.cancelAutoRefresh();
		}

		super.onDeleted(context, appWidgetIds);
	}


	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		final String action = intent.getAction();

		if (action != null) {
			Bundle bundle = intent.getExtras();

			if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_INIT))) {
				//startService(context, action, argument);
				Data data = new Data.Builder().putInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
						bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID)).build();
				startWork(context, action, data);
			} else if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
				//startService(context, action, null);
				startWork(context, action, null);
			} else if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
				startWork(context, action, null);
			} else if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_REDRAW))) {
				reDraw(context, bundle.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS), getClass());
			}

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

	protected boolean isWorkRunning(Context context) {
		WorkManager workManager = WorkManager.getInstance(context);
		final String tag = "widget";
		ListenableFuture<List<WorkInfo>> statuses = workManager.getWorkInfosByTag(tag);

		try {
			boolean running = false;
			List<WorkInfo> workInfoList = statuses.get();
			for (WorkInfo workInfo : workInfoList) {
				WorkInfo.State state = workInfo.getState();
				running = (state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED);

				if (running)
					break;
			}
			return running;
		} catch (ExecutionException | InterruptedException e) {
			return false;
		}
	}

	protected void startWork(Context context, String action, @Nullable Data data) {
		if (DeviceUtils.Companion.isScreenOn(context)) {
			Data.Builder dataBuilder = new Data.Builder()
					.putString("action", action);

			if (data != null) {
				dataBuilder.putAll(data);
			}

			final String tag = "widget";

			OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(WidgetListenableWorker.class)
					.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
					.setInputData(dataBuilder.build())
					.addTag(tag)
					.build();

			WorkManager workManager = WorkManager.getInstance(context);
			workManager.enqueueUniqueWork(tag, ExistingWorkPolicy.APPEND, request);
		}
	}


}
