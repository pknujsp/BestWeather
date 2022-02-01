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
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ThirdWidgetCreator extends AbstractWidgetCreator {
	private final DateTimeFormatter refreshDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a h:mm");

	private int addressTextSize;
	private int refreshDateTimeTextSize;
	private int currentTempTextSize;
	private int aqiTextSize;
	private int precipitationTextSize;
	private int hourlyForecastHourTextSize;
	private int hourlyForecastTempTextSize;
	private int dailyForecastDateTextSize;
	private int dailyForecastTempTextSize;

	private final int hourlyForecastCount = 12;
	private final int dailyForecastCount = 5;

	public ThirdWidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
		super(context, widgetUpdateCallback, appWidgetId);
	}


	@Override
	public RemoteViews createTempViews(Integer parentWidth, Integer parentHeight) {
		RemoteViews remoteViews = createBaseRemoteViews();
		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(), WeatherResponseProcessor.getTempAirQualityDto(),
				WeatherResponseProcessor.getTempCurrentConditionsDto(context),
				WeatherResponseProcessor.getTempHourlyForecastDtoList(context, hourlyForecastCount),
				WeatherResponseProcessor.getTempDailyForecastDtoList(context, dailyForecastCount), null, parentWidth, parentHeight);
		return remoteViews;
	}

	@Override
	public RemoteViews createRemoteViews() {
		RemoteViews remoteViews = createBaseRemoteViews();
		remoteViews.setOnClickPendingIntent(R.id.root_layout, getOnClickedPendingIntent());

		return remoteViews;
	}

	@Override
	public void setTextSize(int amount) {
		final int absSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, Math.abs(amount),
				context.getResources().getDisplayMetrics());
		final int extraSize = amount >= 0 ? absSize : absSize * -1;

		addressTextSize = context.getResources().getDimensionPixelSize(R.dimen.addressTextSizeInCommonWidgetHeader) + extraSize;
		refreshDateTimeTextSize = context.getResources().getDimensionPixelSize(R.dimen.refreshDateTimeTextSizeInCommonWidgetHeader) + extraSize;
		currentTempTextSize = context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInFullWidget) + extraSize;
		aqiTextSize = context.getResources().getDimensionPixelSize(R.dimen.airQualityTextSizeInFullWidget) + extraSize;
		precipitationTextSize = context.getResources().getDimensionPixelSize(R.dimen.precipitationTextSizeInFullWidget) + extraSize;
		hourlyForecastHourTextSize = context.getResources().getDimensionPixelSize(R.dimen.hourTextSizeInHourlyForecastItem) + extraSize;
		hourlyForecastTempTextSize = context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInHourlyForecastItem) + extraSize;
		dailyForecastDateTextSize = context.getResources().getDimensionPixelSize(R.dimen.dateTextSizeInDailyForecastItem) + extraSize;
		dailyForecastTempTextSize = context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInDailyForecastItem) + extraSize;
	}

	public void setClockTimeZone(RemoteViews remoteViews) {
		ZoneId zoneId;
		if (widgetDto.getTimeZoneId() == null) {
			zoneId = ZoneId.systemDefault();
		} else {
			zoneId = widgetDto.isDisplayLocalClock() ? ZoneId.of(widgetDto.getTimeZoneId()) : ZoneId.systemDefault();
		}

		//remoteViews.setString(R.id.dateClock, "setTimeZone", zoneId.getId());
		//remoteViews.setString(R.id.timeClock, "setTimeZone", zoneId.getId());
	}

	public View makeHeaderViews(LayoutInflater layoutInflater, String addressName, String lastRefreshDateTime) {
		View view = layoutInflater.inflate(R.layout.header_view_in_widget, null, false);
		((TextView) view.findViewById(R.id.address)).setText(addressName);
		((TextView) view.findViewById(R.id.refresh)).setText(ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));

		((TextView) view.findViewById(R.id.address)).setTextSize(TypedValue.COMPLEX_UNIT_PX, addressTextSize);
		((TextView) view.findViewById(R.id.refresh)).setTextSize(TypedValue.COMPLEX_UNIT_PX, refreshDateTimeTextSize);

		return view;
	}

	public void makeCurrentConditionsViews(View view, CurrentConditionsDto currentConditionsDto,
	                                       AirQualityDto airQualityDto) {
		((TextView) view.findViewById(R.id.temperature)).setText(currentConditionsDto.getTemp());

		String airQuality = context.getString(R.string.air_quality) + ": " + AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi());
		((TextView) view.findViewById(R.id.airQuality)).setText(airQuality);

		String precipitation = "";
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto.getPrecipitationVolume();
		} else {
			precipitation = context.getString(R.string.not_precipitation);
		}
		((TextView) view.findViewById(R.id.precipitation)).setText(precipitation);

		((TextView) view.findViewById(R.id.temperature)).setTextSize(TypedValue.COMPLEX_UNIT_PX, currentTempTextSize);
		((TextView) view.findViewById(R.id.airQuality)).setTextSize(TypedValue.COMPLEX_UNIT_PX, aqiTextSize);
		((TextView) view.findViewById(R.id.precipitation)).setTextSize(TypedValue.COMPLEX_UNIT_PX, precipitationTextSize);
	}

	public void setHourlyForecastViews(View view, LayoutInflater layoutInflater, List<HourlyForecastDto> hourlyForecastDtoList) {
		DateTimeFormatter hour0Formatter = DateTimeFormatter.ofPattern("E 0");
		String hours = "";

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
		layoutParams.weight = 1;
		layoutParams.gravity = Gravity.CENTER;

		final int padding = (int) context.getResources().getDimension(R.dimen.forecastItemViewInLinearPadding);

		for (int i = 0; i < hourlyForecastCount; i++) {
			View itemView = layoutInflater.inflate(R.layout.view_hourly_forecast_item_in_linear, null, false);
			itemView.setLayoutParams(layoutParams);
			itemView.setPadding(0, padding, 0, padding);

			if (hourlyForecastDtoList.get(i).getHours().getHour() == 0) {
				hours = hourlyForecastDtoList.get(i).getHours().format(hour0Formatter);
			} else {
				hours = String.valueOf(hourlyForecastDtoList.get(i).getHours().getHour());
			}

			((TextView) itemView.findViewById(R.id.hour)).setText(hours);
			((TextView) itemView.findViewById(R.id.temperature)).setText(hourlyForecastDtoList.get(i).getTemp());
			((ImageView) itemView.findViewById(R.id.weatherIcon)).setImageResource(hourlyForecastDtoList.get(i).getWeatherIcon());

			((TextView) itemView.findViewById(R.id.hour)).setTextSize(TypedValue.COMPLEX_UNIT_PX, hourlyForecastHourTextSize);
			((TextView) itemView.findViewById(R.id.temperature)).setTextSize(TypedValue.COMPLEX_UNIT_PX, hourlyForecastTempTextSize);

			if (i >= hourlyForecastCount / 2) {
				((ViewGroup) view.findViewById(R.id.hourly_forecast_row_2)).addView(itemView);
			} else {
				((ViewGroup) view.findViewById(R.id.hourly_forecast_row_1)).addView(itemView);
			}
		}

	}

	public void setDailyForecastViews(View view, LayoutInflater layoutInflater, List<DailyForecastDto> dailyForecastDtoList) {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("E");
		String temp = "";

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
		layoutParams.weight = 1;
		layoutParams.gravity = Gravity.CENTER;

		final int padding = (int) context.getResources().getDimension(R.dimen.forecastItemViewInLinearPadding);

		for (int day = 0; day < dailyForecastCount; day++) {
			View itemView = layoutInflater.inflate(R.layout.view_daily_forecast_item_in_linear, null, false);
			itemView.setLayoutParams(layoutParams);
			itemView.setPadding(0, padding, 0, padding);

			((TextView) itemView.findViewById(R.id.date)).setText(dailyForecastDtoList.get(day).getDate().format(dateFormatter));
			temp = dailyForecastDtoList.get(day).getMinTemp() + "/" + dailyForecastDtoList.get(day).getMaxTemp();
			((TextView) itemView.findViewById(R.id.temperature)).setText(temp);

			if (dailyForecastDtoList.get(day).isSingle()) {
				((ImageView) itemView.findViewById(R.id.leftIcon)).setImageResource(dailyForecastDtoList.get(day).getSingleValues().getWeatherIcon());
				itemView.findViewById(R.id.rightIcon).setVisibility(View.GONE);
			} else {
				((ImageView) itemView.findViewById(R.id.leftIcon)).setImageResource(dailyForecastDtoList.get(day).getAmValues().getWeatherIcon());
				((ImageView) itemView.findViewById(R.id.rightIcon)).setImageResource(dailyForecastDtoList.get(day).getPmValues().getWeatherIcon());
			}

			((TextView) itemView.findViewById(R.id.date)).setTextSize(TypedValue.COMPLEX_UNIT_PX, dailyForecastDateTextSize);
			((TextView) itemView.findViewById(R.id.temperature)).setTextSize(TypedValue.COMPLEX_UNIT_PX, dailyForecastTempTextSize);

			((ViewGroup) view.findViewById(R.id.daily_forecast_row)).addView(itemView);
		}
	}

	public void setDataViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, AirQualityDto airQualityDto, CurrentConditionsDto currentConditionsDto,
	                         List<HourlyForecastDto> hourlyForecastDtoList, List<DailyForecastDto> dailyForecastDtoList,
	                         OnDrawBitmapCallback onDrawBitmapCallback) {
		drawViews(remoteViews, addressName, lastRefreshDateTime, airQualityDto, currentConditionsDto, hourlyForecastDtoList,
				dailyForecastDtoList, onDrawBitmapCallback, null, null);
	}


	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, AirQualityDto airQualityDto, CurrentConditionsDto currentConditionsDto,
	                       List<HourlyForecastDto> hourlyForecastDtoList, List<DailyForecastDto> dailyForecastDtoList,
	                       @Nullable OnDrawBitmapCallback onDrawBitmapCallback, @Nullable Integer parentWidth,
	                       @Nullable Integer parentHeight) {
		LayoutInflater layoutInflater = LayoutInflater.from(context);

		View headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime);
		headerView.setId(R.id.header);

		final View valuesView = layoutInflater.inflate(R.layout.view_third_widget, null, false);

		makeCurrentConditionsViews(valuesView, currentConditionsDto, airQualityDto);
		setHourlyForecastViews(valuesView, layoutInflater, hourlyForecastDtoList);
		setDailyForecastViews(valuesView, layoutInflater, dailyForecastDtoList);

		RelativeLayout.LayoutParams headerViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams valuesViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		headerViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		valuesViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		valuesViewLayoutParams.addRule(RelativeLayout.BELOW, R.id.header);

		RelativeLayout rootLayout = new RelativeLayout(context);

		rootLayout.addView(headerView, headerViewLayoutParams);
		rootLayout.addView(valuesView, valuesViewLayoutParams);

		drawBitmap(rootLayout, onDrawBitmapCallback, remoteViews, parentWidth, parentHeight);

	}

	@Override
	public void setDisplayClock(boolean displayClock) {
		widgetDto.setDisplayClock(displayClock);
	}

	@Override
	public void setDataViewsOfSavedData() {
		WeatherDataSourceType weatherDataSourceType = WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherSourceTypeSet());

		if (widgetDto.isTopPriorityKma() && widgetDto.getCountryCode().equals("KR")) {
			weatherDataSourceType = WeatherDataSourceType.KMA_WEB;
		}

		RemoteViews remoteViews = createRemoteViews();
		JsonObject jsonObject = (JsonObject) JsonParser.parseString(widgetDto.getResponseText());

		AirQualityDto airQualityDto = AqicnResponseProcessor.parseTextToAirQualityDto(context, jsonObject);
		CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.parseTextToCurrentConditionsDto(context, jsonObject,
				weatherDataSourceType, widgetDto.getLatitude(), widgetDto.getLongitude());
		List<HourlyForecastDto> hourlyForecastDtoList = WeatherResponseProcessor.parseTextToHourlyForecastDtoList(context, jsonObject,
				weatherDataSourceType, widgetDto.getLatitude(), widgetDto.getLongitude());
		List<DailyForecastDto> dailyForecastDtoList = WeatherResponseProcessor.parseTextToDailyForecastDtoList(context, jsonObject,
				weatherDataSourceType);

		setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), airQualityDto, currentConditionsDto,
				hourlyForecastDtoList, dailyForecastDtoList, null);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(appWidgetId,
				remoteViews);
	}
}
