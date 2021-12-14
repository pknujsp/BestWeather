package com.lifedawn.bestweather.widget.widgetprovider;

import android.content.Context;
import android.os.Bundle;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.room.dto.WidgetDto;

import java.util.Set;


public class FirstSimpleWidgetProvider extends AbstractAppWidgetProvider {


	@Override
	Class<?> getThis() {
		return FirstSimpleWidgetProvider.class;
	}

	@Override
	protected void reDrawWidget(Context context, int appWidgetId) {

	}

	@Override
	protected void init(Context context, Bundle bundle) {

	}

	@Override
	Set<RequestWeatherDataType> getRequestWeatherDataTypeSet() {
		return null;
	}

	@Override
	protected void setResultViews(Context context, int appWidgetId, RemoteViews remoteViews, WidgetDto widgetDto, WeatherSourceType requestWeatherSourceType, @Nullable @org.jetbrains.annotations.Nullable MultipleJsonDownloader multipleJsonDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {

	}
}