package com.lifedawn.bestweather.widget.widgetprovider;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.lifedawn.bestweather.widget.jobservice.TenthWidgetJobService;

public class TenthWidgetProvider extends AbstractAppWidgetProvider {
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
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
	}


	@Override
	protected Class<?> getJobServiceClass() {
		return TenthWidgetJobService.class;
	}
}