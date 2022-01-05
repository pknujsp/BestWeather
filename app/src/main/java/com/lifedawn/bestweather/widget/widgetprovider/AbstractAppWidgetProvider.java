package com.lifedawn.bestweather.widget.widgetprovider;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobWorkItem;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.room.repository.WidgetRepository;
import com.lifedawn.bestweather.widget.WidgetHelper;
import com.lifedawn.bestweather.widget.jobservice.FirstWidgetJobService;

import java.util.List;

public abstract class AbstractAppWidgetProvider extends AppWidgetProvider {
	protected static final int JOB_ID_ON_APP_WIDGET_OPTIONS_CHANGED = 100000;
	protected static final int JOB_REFRESH = 200000;
	protected static final int JOB_ACTION_BOOT_COMPLETED = 300000;
	protected static final int JOB_REDRAW = 400000;
	protected static final int JOB_INIT = 500000;

	protected abstract Class<?> getJobServiceClass();

	@Override
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
		scheduleJob(context, context.getString(R.string.com_lifedawn_bestweather_action_ON_APP_WIDGET_OPTIONS_CHANGED), JOB_ID_ON_APP_WIDGET_OPTIONS_CHANGED, appWidgetId);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
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
		} else if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_REDRAW))) {
			jobBeginId = JOB_REDRAW;
		}

		if (jobBeginId != 0) {
			final int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
			scheduleJob(context, action, jobBeginId, appWidgetId);
		}
	}


	protected final void scheduleJob(Context context, String action, int jobIdBegin, int appWidgetId) {
		PersistableBundle persistableBundle = new PersistableBundle();
		persistableBundle.putString("action", action);
		persistableBundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

		final int jobId = jobIdBegin + appWidgetId;

		JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
		JobInfo jobInfo = new JobInfo.Builder(jobId, new ComponentName(context, getJobServiceClass()))
				.setMinimumLatency(0).setOverrideDeadline(500).setExtras(persistableBundle).build();

		List<JobInfo> jobInfoList = jobScheduler.getAllPendingJobs();
		for (JobInfo pendingJonInfo : jobInfoList) {
			if (pendingJonInfo.getId() == jobId) {
				jobScheduler.cancel(jobId);
			}
		}

		jobScheduler.schedule(jobInfo);
	}

}
