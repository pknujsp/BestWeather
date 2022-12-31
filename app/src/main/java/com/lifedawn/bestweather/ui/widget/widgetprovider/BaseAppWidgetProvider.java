package com.lifedawn.bestweather.ui.widget.widgetprovider;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.interfaces.BackgroundWorkCallback;
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil;
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.data.local.room.dto.WidgetDto;
import com.lifedawn.bestweather.data.local.room.repository.WidgetRepository;
import com.lifedawn.bestweather.ui.widget.WidgetHelper;
import com.lifedawn.bestweather.ui.widget.creator.AbstractWidgetCreator;
import com.lifedawn.bestweather.ui.widget.work.WidgetListenableWorker;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseAppWidgetProvider extends AppWidgetProvider {

	protected void drawWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Executors.newSingleThreadExecutor().submit(() -> {
			final String className = getClass().getName();

			final BackgroundWorkCallback backgroundWorkCallback = new BackgroundWorkCallback() {
				final int requestCount = appWidgetIds.length;
				final AtomicInteger responseCount = new AtomicInteger(0);

				@Override
				public void onFinished() {
					int count = responseCount.incrementAndGet();
					Log.e(className, "위젯 그리기 : " + Arrays.toString(appWidgetIds) + ", 요청 : " + requestCount + ", 응답 : " + count);

					if (count == requestCount) {
						Log.e(className, "위젯 그리기 완료 : " + Arrays.toString(appWidgetIds));
					}
				}
			};

			for (int appWidgetId : appWidgetIds) {
				if (appWidgetManager.getAppWidgetInfo(appWidgetId) == null) {
					backgroundWorkCallback.onFinished();
					continue;
				}

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
						backgroundWorkCallback.onFinished();
					}

					@Override
					public void onResultNoData() {
						backgroundWorkCallback.onFinished();
					}

				});

			}

		});

	}

	@Override
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.e(getClass().getName(), Arrays.toString(appWidgetIds));
		if (!WidgetListenableWorker.processing.get())
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
		super.onDeleted(context, appWidgetIds);

		final BackgroundWorkCallback backgroundWorkCallback = new BackgroundWorkCallback() {
			final int allDeletedCount = appWidgetIds.length;
			final AtomicInteger deletedCount = new AtomicInteger(0);

			@Override
			public void onFinished() {
				if (deletedCount.incrementAndGet() == allDeletedCount) {
					AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

					if (appWidgetManager.getInstalledProviders().isEmpty()) {
						WidgetHelper widgetHelper = new WidgetHelper(context);
						widgetHelper.cancelAutoRefresh();
					}
				}
			}
		};

		WidgetRepository widgetRepository = WidgetRepository.getINSTANCE();
		for (int appWidgetId : appWidgetIds)
			widgetRepository.delete(appWidgetId, new DbQueryCallback<Boolean>() {
				@Override
				public void onResultSuccessful(Boolean result) {
					backgroundWorkCallback.onFinished();
				}

				@Override
				public void onResultNoData() {

				}
			});

	}


	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		final String action = intent.getAction();
		Log.e(getClass().getName(), action);

		if (action != null) {
			Bundle bundle = intent.getExtras();
			if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_INIT))) {
				Data data = new Data.Builder().putInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
						bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID)).build();
				startWork(context, action, data);
			} else if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
				startWork(context, action, null);
			} else if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
				WidgetHelper widgetHelper = new WidgetHelper(context);
				widgetHelper.reDrawWidgets(null);
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

		final String tag = "widget_" + action;

		OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(WidgetListenableWorker.class)
				.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
				.setInputData(dataBuilder.build())
				.addTag(tag)
				.build();

		WorkManager workManager = WorkManager.getInstance(context);
		workManager.enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, request);
	}

}
