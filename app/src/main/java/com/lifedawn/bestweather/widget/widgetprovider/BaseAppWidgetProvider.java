package com.lifedawn.bestweather.widget.widgetprovider;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.forremoteviews.RemoteViewsUtil;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.room.repository.WidgetRepository;
import com.lifedawn.bestweather.widget.WidgetHelper;
import com.lifedawn.bestweather.widget.creator.AbstractWidgetCreator;
import com.lifedawn.bestweather.widget.work.WidgetListenableWorker;

public abstract class BaseAppWidgetProvider extends AppWidgetProvider {

	protected void drawWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		for (int appWidgetId : appWidgetIds) {
			if (appWidgetManager.getAppWidgetInfo(appWidgetId) == null)
				continue;

			AbstractWidgetCreator widgetCreator = AbstractWidgetCreator.getInstance(appWidgetManager, context, appWidgetId);
			widgetCreator.loadSavedSettings(new DbQueryCallback<WidgetDto>() {
				@Override
				public void onResultSuccessful(WidgetDto widgetDto) {
					if (widgetDto != null) {
						if (widgetDto.isInitialized()) {
							if (widgetDto.isLoadSuccessful())
								widgetCreator.setDataViewsOfSavedData();
							else {
								RemoteViews remoteViews = widgetCreator.createRemoteViews();
								widgetCreator.setRefreshPendingIntent(remoteViews);
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
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		drawWidgets(context, appWidgetManager, appWidgetIds);
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
		for (int appWidgetId : appWidgetIds)
			widgetRepository.delete(appWidgetId, null);

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

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
				Data data = new Data.Builder().putInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
						bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID)).build();
				startWork(context, action, data);
			} else if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {

				startWork(context, action, null);
			} else if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
				startWork(context, action, null);
			} else if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_REDRAW))) {
				int[] arr = bundle.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
				drawWidgets(context, AppWidgetManager.getInstance(context), arr);
			}
		}

	}


	protected void startWork(Context context, String action, @Nullable Data data) {
		Data.Builder dataBuilder = new Data.Builder()
				.putString("action", action);

		if (data != null)
			dataBuilder.putAll(data);

		final String tag = "widget" + action;

		OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(WidgetListenableWorker.class)
				.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
				.setInputData(dataBuilder.build())
				.addTag(tag)
				.build();

		WorkManager workManager = WorkManager.getInstance(context);
		workManager.enqueueUniqueWork(tag, ExistingWorkPolicy.APPEND_OR_REPLACE, request);
	}

}
