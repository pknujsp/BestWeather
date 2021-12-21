package com.lifedawn.bestweather.widget.creator;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import androidx.core.content.ContextCompat;

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
import com.lifedawn.bestweather.weathers.view.DetailSingleTemperatureView;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.MeasureSpec.EXACTLY;

public class ThirdSimpleWidgetCreator extends AbstractWidgetCreator {
	private final DateTimeFormatter refreshDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a h:mm");

	private int addressTextSize;
	private int refreshDateTimeTextSize;
	private int hourTextSize;
	private int tempTextSize;
	private int popTextSize;

	private final int cellCount = 9;

	public ThirdSimpleWidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
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
		hourTextSize = context.getResources().getDimensionPixelSize(R.dimen.dateTimeTextSizeInSimpleWidgetForecastItem) + extraSize;
		tempTextSize = context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInSimpleWidgetForecastItem) + extraSize;
		popTextSize = context.getResources().getDimensionPixelSize(R.dimen.popTextSizeInSimpleWidgetForecastItem) + extraSize;

	}


	public View makeHeaderViews(LayoutInflater layoutInflater, String addressName, String lastRefreshDateTime) {
		View view = layoutInflater.inflate(R.layout.header_view_in_widget, null, false);
		((TextView) view.findViewById(R.id.address)).setText(addressName);
		((TextView) view.findViewById(R.id.refresh)).setText(ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));

		((TextView) view.findViewById(R.id.address)).setTextSize(TypedValue.COMPLEX_UNIT_PX, addressTextSize);
		((TextView) view.findViewById(R.id.refresh)).setTextSize(TypedValue.COMPLEX_UNIT_PX, refreshDateTimeTextSize);

		return view;
	}

	public void setDataViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, CurrentConditionsDto currentConditionsDto,
	                         List<HourlyForecastDto> hourlyForecastDtoList, OnDrawBitmapCallback onDrawBitmapCallback) {
		final String degreeCelsius = "C";
		final String degreeFahrenheit = "F";

		HourlyForecastDto current = new HourlyForecastDto();
		current.setHours(null).setWeatherIcon(currentConditionsDto.getWeatherIcon())
				.setTemp(currentConditionsDto.getTemp().replace(degreeCelsius, "").replace(degreeFahrenheit, ""));

		hourlyForecastDtoList.add(current);
		drawViews(remoteViews, addressName, lastRefreshDateTime, hourlyForecastDtoList, onDrawBitmapCallback);
	}

	public void setTempDataViews(RemoteViews remoteViews) {
		List<HourlyForecastDto> hourlyForecastDtoList = new ArrayList<>();

		final String temp = "20°";
		final String pop = "10%";
		final int weatherIcon = R.drawable.day_clear;

		HourlyForecastDto current = new HourlyForecastDto();
		current.setHours(null).setWeatherIcon(weatherIcon)
				.setTemp(temp);

		hourlyForecastDtoList.add(current);
		ZonedDateTime now = ZonedDateTime.now();

		for (int i = 0; i < cellCount - 1; i++) {
			HourlyForecastDto hourlyForecastDto = new HourlyForecastDto();
			hourlyForecastDto.setWeatherIcon(weatherIcon).setTemp(temp).setHours(now)
					.setPop(pop);
			hourlyForecastDtoList.add(hourlyForecastDto);

			now = now.plusHours(1);
		}
		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(), hourlyForecastDtoList, null);
	}

	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime,
	                       List<HourlyForecastDto> hourlyForecastDtoList, @Nullable OnDrawBitmapCallback onDrawBitmapCallback) {
		RelativeLayout rootLayout = new RelativeLayout(context);

		LinearLayout hourAndIconLinearLayout = new LinearLayout(context);
		hourAndIconLinearLayout.setId(R.id.hourAndIconView);
		hourAndIconLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams hourAndIconCellLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		hourAndIconCellLayoutParams.gravity = Gravity.CENTER;
		hourAndIconCellLayoutParams.weight = 1;

		LayoutInflater layoutInflater = LayoutInflater.from(context);

		View headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime);
		headerView.setId(R.id.header);

		List<Integer> tempList = new ArrayList<>();
		final String degree = "°";
		DateTimeFormatter hour0Formatter = DateTimeFormatter.ofPattern("E 0");

		for (int cell = 0; cell < cellCount; cell++) {
			View view = layoutInflater.inflate(R.layout.view_forecast_item_in_linear, null, false);

			if (cell == 0) {
				((TextView) view.findViewById(R.id.dateTime)).setText(context.getString(R.string.current));
			} else {
				//hour, weatherIcon, pop
				if (hourlyForecastDtoList.get(cell).getHours().getHour() == 0) {
					((TextView) view.findViewById(R.id.dateTime)).setText(hourlyForecastDtoList.get(cell).getHours().format(hour0Formatter));
				} else {
					((TextView) view.findViewById(R.id.dateTime)).setText(String.valueOf(hourlyForecastDtoList.get(cell).getHours().getHour()));
				}
			}
			((ImageView) view.findViewById(R.id.leftIcon)).setImageResource(hourlyForecastDtoList.get(cell).getWeatherIcon());
			tempList.add(Integer.parseInt(hourlyForecastDtoList.get(cell).getTemp().replace(degree, "")));

			if (cell != 0) {
				((TextView) view.findViewById(R.id.pop)).setText(hourlyForecastDtoList.get(cell).getPop());
			} else {
				view.findViewById(R.id.popLayout).setVisibility(View.INVISIBLE);
			}

			((TextView) view.findViewById(R.id.dateTime)).setTextSize(TypedValue.COMPLEX_UNIT_PX, hourTextSize);
			((TextView) view.findViewById(R.id.pop)).setTextSize(TypedValue.COMPLEX_UNIT_PX, popTextSize);

			view.findViewById(R.id.temperature).setVisibility(View.GONE);
			view.findViewById(R.id.rightIcon).setVisibility(View.GONE);

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
		tempRowLayoutParams.addRule(RelativeLayout.BELOW, R.id.hourAndIconView);
		tempRowLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		DetailSingleTemperatureView detailSingleTemperatureView = new DetailSingleTemperatureView(context, tempList);
		detailSingleTemperatureView.setTempTextSizePx(tempTextSize);

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

		CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.parseTextToCurrentConditionsDto(context, jsonObject,
				weatherSourceType, widgetDto.getLatitude(), widgetDto.getLongitude());
		List<HourlyForecastDto> hourlyForecastDtoList = WeatherResponseProcessor.parseTextToHourlyForecastDtoList(context, jsonObject,
				weatherSourceType, widgetDto.getLatitude(), widgetDto.getLongitude());

		setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), currentConditionsDto,
				hourlyForecastDtoList, null);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(appWidgetId,
				remoteViews);
	}

}
