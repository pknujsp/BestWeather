package com.lifedawn.bestweather.widget.creator;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;
import com.lifedawn.bestweather.weathers.view.DetailDoubleTemperatureViewForRemoteViews;
import com.lifedawn.bestweather.weathers.view.DetailSingleTemperatureView;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TenthWidgetCreator extends AbstractWidgetCreator {
	private final DateTimeFormatter refreshDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a h:mm");

	private int addressTextSize;
	private int refreshDateTimeTextSize;
	private int dateTextSize;
	private int tempTextSize;
	private int popTextSize;
	private int rainVolumeTextSize;
	private int snowVolumeTextSize;

	public TenthWidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
		super(context, widgetUpdateCallback, appWidgetId);
	}

	@Override
	public RemoteViews createRemoteViews(boolean needTempData) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int layoutId = appWidgetManager.getAppWidgetInfo(appWidgetId).initialLayout;
		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);

		if (needTempData) {
			setTempDataViews(remoteViews);
		} else {
			remoteViews.setOnClickPendingIntent(R.id.root_layout, getOnClickedPendingIntent(remoteViews));
		}

		//setBackgroundAlpha(remoteViews, widgetDto.getBackgroundAlpha());

		return remoteViews;
	}

	@Override
	public void setTextSize(int amount) {
		final int absSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, Math.abs(amount),
				context.getResources().getDisplayMetrics());
		final int extraSize = amount >= 0 ? absSize : absSize * -1;

		addressTextSize = context.getResources().getDimensionPixelSize(R.dimen.addressTextSizeInCommonWidgetHeader) + extraSize;
		refreshDateTimeTextSize = context.getResources().getDimensionPixelSize(R.dimen.refreshDateTimeTextSizeInCommonWidgetHeader) + extraSize;
		tempTextSize = context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInSimpleWidgetForecastItem) + extraSize;
		popTextSize = context.getResources().getDimensionPixelSize(R.dimen.popTextSizeInSimpleWidgetForecastItem) + extraSize;
		dateTextSize = context.getResources().getDimensionPixelSize(R.dimen.dateTimeTextSizeInSimpleWidgetForecastItem) + extraSize;
		rainVolumeTextSize = context.getResources().getDimensionPixelSize(R.dimen.rainVolumeTextSizeInSimpleWidgetForecastItem) + extraSize;
		snowVolumeTextSize = context.getResources().getDimensionPixelSize(R.dimen.snowVolumeTextSizeInSimpleWidgetForecastItem) + extraSize;
	}


	public View makeHeaderViews(LayoutInflater layoutInflater, String addressName, String lastRefreshDateTime) {
		View view = layoutInflater.inflate(R.layout.header_view_in_widget, null, false);
		((TextView) view.findViewById(R.id.address)).setText(addressName);
		((TextView) view.findViewById(R.id.refresh)).setText(ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));

		((TextView) view.findViewById(R.id.address)).setTextSize(TypedValue.COMPLEX_UNIT_PX, addressTextSize);
		((TextView) view.findViewById(R.id.refresh)).setTextSize(TypedValue.COMPLEX_UNIT_PX, refreshDateTimeTextSize);

		return view;
	}

	public void setDataViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime,
	                         List<DailyForecastDto> dailyForecastDtoList, OnDrawBitmapCallback onDrawBitmapCallback) {

		drawViews(remoteViews, addressName, lastRefreshDateTime, dailyForecastDtoList, onDrawBitmapCallback);
	}

	public void setTempDataViews(RemoteViews remoteViews) {
		List<DailyForecastDto> dailyForecastDtoList = new ArrayList<>();
		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(),
				dailyForecastDtoList, null);
	}

	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime,
	                       List<DailyForecastDto> dailyForecastDtoList, @Nullable OnDrawBitmapCallback onDrawBitmapCallback) {
		LayoutInflater layoutInflater = LayoutInflater.from(context);

		View headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime);
		headerView.setId(R.id.header);

		LinearLayout hourAndIconLinearLayout = new LinearLayout(context);
		hourAndIconLinearLayout.setId(R.id.hourAndIconView);
		hourAndIconLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams hourAndIconCellLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		hourAndIconCellLayoutParams.gravity = Gravity.CENTER;
		hourAndIconCellLayoutParams.weight = 1;

		List<Integer> minTempList = new ArrayList<>();
		List<Integer> maxTempList = new ArrayList<>();
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M.d\nE");
		String pop = null;

		for (int cell = 0; cell < dailyForecastDtoList.size(); cell++) {
			View view = layoutInflater.inflate(R.layout.view_forecast_item_in_linear, null, false);
			//hour, weatherIcon
			((TextView) view.findViewById(R.id.dateTime)).setText(dailyForecastDtoList.get(cell).getDate().format(dateFormatter));

			((TextView) view.findViewById(R.id.dateTime)).setTextSize(TypedValue.COMPLEX_UNIT_PX, dateTextSize);
			((TextView) view.findViewById(R.id.pop)).setTextSize(TypedValue.COMPLEX_UNIT_PX, popTextSize);

			if (dailyForecastDtoList.get(cell).isSingle()) {
				((ImageView) view.findViewById(R.id.leftIcon)).setImageResource(dailyForecastDtoList.get(cell).getSingleValues().getWeatherIcon());
				pop = dailyForecastDtoList.get(cell).getSingleValues().getPop();

				view.findViewById(R.id.rightIcon).setVisibility(View.GONE);
			} else {
				((ImageView) view.findViewById(R.id.leftIcon)).setImageResource(dailyForecastDtoList.get(cell).getAmValues().getWeatherIcon());
				((ImageView) view.findViewById(R.id.rightIcon)).setImageResource(dailyForecastDtoList.get(cell).getPmValues().getWeatherIcon());
				pop = dailyForecastDtoList.get(cell).getAmValues().getPop() + "/" +
						dailyForecastDtoList.get(cell).getPmValues().getPop();
			}
			((TextView) view.findViewById(R.id.pop)).setText(pop);

			view.findViewById(R.id.temperature).setVisibility(View.GONE);

			minTempList.add(Integer.parseInt(dailyForecastDtoList.get(cell).getMinTemp().replace(tempDegree, "")));
			maxTempList.add(Integer.parseInt(dailyForecastDtoList.get(cell).getMaxTemp().replace(tempDegree, "")));
			hourAndIconLinearLayout.addView(view, hourAndIconCellLayoutParams);
		}

		RelativeLayout.LayoutParams headerViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams hourAndIconRowLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams tempRowLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		headerViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		hourAndIconRowLayoutParams.addRule(RelativeLayout.BELOW, R.id.header);
		hourAndIconRowLayoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.currentConditions);
		tempRowLayoutParams.addRule(RelativeLayout.BELOW, R.id.hourAndIconView);
		tempRowLayoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.currentConditions);
		tempRowLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		DetailDoubleTemperatureViewForRemoteViews detailSingleTemperatureView = new DetailDoubleTemperatureViewForRemoteViews(context,
				minTempList, maxTempList);
		detailSingleTemperatureView.setTempTextSizePx(tempTextSize);

		RelativeLayout rootLayout = new RelativeLayout(context);

		rootLayout.addView(headerView, headerViewLayoutParams);
		rootLayout.addView(hourAndIconLinearLayout, hourAndIconRowLayoutParams);
		rootLayout.addView(detailSingleTemperatureView, tempRowLayoutParams);

		drawBitmap(rootLayout, onDrawBitmapCallback, remoteViews);
	}


	@Override
	public void setDisplayClock(boolean displayClock) {
		widgetDto.setDisplayClock(displayClock);
	}

	@Override
	public void setDataViewsOfSavedData() {
		WeatherSourceType weatherSourceType = WeatherSourceType.valueOf(widgetDto.getWeatherSourceType());

		if (widgetDto.isTopPriorityKma() && widgetDto.getCountryCode().equals("KR")) {
			weatherSourceType = WeatherSourceType.KMA;
		}

		RemoteViews remoteViews = createRemoteViews(false);
		JsonObject jsonObject = (JsonObject) JsonParser.parseString(widgetDto.getResponseText());

		List<DailyForecastDto> dailyForecastDtoList = WeatherResponseProcessor.parseTextToDailyForecastDtoList(context, jsonObject,
				weatherSourceType);

		setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(),
				dailyForecastDtoList, null);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(appWidgetId,
				remoteViews);
	}

}
