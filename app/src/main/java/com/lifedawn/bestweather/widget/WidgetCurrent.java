package com.lifedawn.bestweather.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;

import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;

import java.util.HashSet;
import java.util.Set;

public class WidgetCurrent extends RootAppWidget {
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
	Set<RequestWeatherDataType> getRequestWeatherDataTypeSet() {
		Set<RequestWeatherDataType> set = new HashSet<>();
		set.add(RequestWeatherDataType.currentConditions);
		return set;
	}

	@Override
	Class<?> getThis() {
		return WidgetCurrent.class;
	}
}
