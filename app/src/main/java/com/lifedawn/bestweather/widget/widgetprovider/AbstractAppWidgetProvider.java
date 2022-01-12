package com.lifedawn.bestweather.widget.widgetprovider;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.ArraySet;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.forremoteviews.RemoteViewProcessor;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.room.repository.WidgetRepository;
import com.lifedawn.bestweather.widget.WidgetHelper;
import com.lifedawn.bestweather.widget.creator.AbstractWidgetCreator;

import java.util.List;
import java.util.Set;

public abstract class AbstractAppWidgetProvider extends AppWidgetProvider {
	protected static final int JOB_ID_ON_APP_WIDGET_OPTIONS_CHANGED = 100000;
	protected static final int JOB_REFRESH = 200000;
	protected static final int JOB_ACTION_BOOT_COMPLETED = 300000;
	protected static final int JOB_REDRAW = 400000;
	protected static final int JOB_INIT = 500000;

	protected abstract Class<?> getJobServiceClass();

	protected abstract AbstractWidgetCreator getWidgetCreatorInstance(Context context, int appWidgetId);

	protected void reDraw(Context context, int[] appWidgetIds) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		final Class<?> widgetProviderClass = getClass();

		for (int i = 0; i < appWidgetIds.length; i++) {
			final int appWidgetId = appWidgetIds[i];

			AbstractWidgetCreator widgetCreator = getWidgetCreatorInstance(context, appWidgetId);
			widgetCreator.loadSavedSettings(new DbQueryCallback<WidgetDto>() {
				@Override
				public void onResultSuccessful(WidgetDto widgetDto) {
					if (widgetDto != null) {
						if (widgetDto.isInitialized() && widgetDto.isLoadSuccessful()) {
							widgetCreator.setDataViewsOfSavedData();
						} else if (widgetDto.isInitialized() && !widgetDto.isLoadSuccessful()) {
							RemoteViews remoteViews = widgetCreator.createRemoteViews();

							widgetCreator.setRefreshPendingIntent(widgetProviderClass, remoteViews, appWidgetId);
							RemoteViewProcessor.onErrorProcess(remoteViews, context, RemoteViewProcessor.ErrorType.FAILED_LOAD_WEATHER_DATA);
							appWidgetManager.updateAppWidget(widgetCreator.getAppWidgetId(), remoteViews);
						} else {

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
		reDraw(context, new int[]{appWidgetId});
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		/*
		PersistableBundle bundle = new PersistableBundle();
		bundle.putIntArray("appWidgetIds", appWidgetIds);
		for (int id : appWidgetIds) {
			scheduleJob(context, context.getString(R.string.com_lifedawn_bestweather_action_REDRAW), JOB_REDRAW, id, bundle);
		}

		 */
		reDraw(context, appWidgetIds);
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
		WidgetHelper widgetHelper = new WidgetHelper(context, getClass());
		WidgetRepository widgetRepository = new WidgetRepository(context);

		for (int appWidgetId : appWidgetIds) {
			widgetHelper.cancelAutoRefresh(appWidgetId);
			widgetRepository.delete(appWidgetId, null);
		}
		super.onDeleted(context, appWidgetIds);
	}


	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		Bundle bundle = intent.getExtras();
		final String action = intent.getAction();

		int jobBeginId = 0;

		if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_INIT))) {
			jobBeginId = JOB_INIT;
		} else if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
			jobBeginId = JOB_REFRESH;
		} else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			jobBeginId = JOB_ACTION_BOOT_COMPLETED;
		} else if (action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
			int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
			reDraw(context, new int[]{appWidgetId});
		}

		if (jobBeginId != 0) {
			int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
			scheduleJob(context, action, jobBeginId, appWidgetId, null);
		}
	}


	protected final void scheduleJob(Context context, String action, int jobIdBegin, int appWidgetId, @Nullable PersistableBundle extras) {
		final PersistableBundle persistableBundle = new PersistableBundle();
		if (extras != null) {
			persistableBundle.putAll(extras);
		}
		persistableBundle.putString("action", action);
		persistableBundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

		final int newJobId = jobIdBegin + appWidgetId;
		JobInfo newJobInfo = new JobInfo.Builder(newJobId, new ComponentName(context, getJobServiceClass()))
				.setMinimumLatency(0).setOverrideDeadline(1000).setExtras(persistableBundle).build();

		JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

		List<JobInfo> pendingJobs = jobScheduler.getAllPendingJobs();
		if (jobIdBegin == JOB_REFRESH && pendingJobs.size() > 0) {
			Set<Integer> closeJobIdSet = new ArraySet<>();
			closeJobIdSet.add(JOB_ID_ON_APP_WIDGET_OPTIONS_CHANGED + appWidgetId);
			closeJobIdSet.add(JOB_ACTION_BOOT_COMPLETED + appWidgetId);
			closeJobIdSet.add(JOB_REDRAW + appWidgetId);

			for (JobInfo jobInfo : pendingJobs) {
				if (closeJobIdSet.contains(jobInfo.getId())) {
					jobScheduler.cancel(jobInfo.getId());
				}
			}
		}

		jobScheduler.schedule(newJobInfo);
	}

}
