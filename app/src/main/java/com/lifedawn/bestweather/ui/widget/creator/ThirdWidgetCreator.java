package com.lifedawn.bestweather.ui.widget.creator;

import android.appwidget.AppWidgetManager;
import android.content.Context;
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
import com.lifedawn.bestweather.commons.constants.WeatherProviderType;
import com.lifedawn.bestweather.commons.constants.WeatherDataType;
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil;
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback;
import com.lifedawn.bestweather.ui.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.ui.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.ui.weathers.dataprocessing.util.WeatherRequestUtil;
import com.lifedawn.bestweather.ui.weathers.dataprocessing.util.WeatherUtil;
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto;
import com.lifedawn.bestweather.data.local.weather.models.CurrentConditionsDto;
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto;
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto;
import com.lifedawn.bestweather.ui.widget.OnDrawBitmapCallback;
import com.lifedawn.bestweather.ui.widget.widgetprovider.ThirdWidgetProvider;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ThirdWidgetCreator extends AbstractWidgetCreator {


	private final int hourlyForecastCount = 12;
	private final int dailyForecastCount = 5;

	public ThirdWidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
		super(context, widgetUpdateCallback, appWidgetId);
	}


	@Override
	public RemoteViews createTempViews(Integer parentWidth, Integer parentHeight) {
		RemoteViews remoteViews = createBaseRemoteViews();
		RemoteViewsUtil.onSuccessfulProcess(remoteViews);

		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(), WeatherResponseProcessor.getTempAirQualityDto(),
				WeatherResponseProcessor.getTempCurrentConditionsDto(context),
				WeatherResponseProcessor.getTempHourlyForecastDtoList(context, hourlyForecastCount),
				WeatherResponseProcessor.getTempDailyForecastDtoList(context, dailyForecastCount), null, parentWidth, parentHeight);
		return remoteViews;
	}

	@Override
	public Set<WeatherDataType> getRequestWeatherDataTypeSet() {
		Set<WeatherDataType> set = new HashSet<>();
		set.add(WeatherDataType.currentConditions);
		set.add(WeatherDataType.hourlyForecast);
		set.add(WeatherDataType.dailyForecast);
		set.add(WeatherDataType.airQuality);

		return set;
	}


	@Override
	public Class<?> widgetProviderClass() {
		return ThirdWidgetProvider.class;
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


	public void makeCurrentConditionsViews(View view, CurrentConditionsDto currentConditionsDto,
	                                       AirQualityDto airQualityDto) {
		((TextView) view.findViewById(R.id.temperature)).setText(currentConditionsDto.getTemp());
		((ImageView) view.findViewById(R.id.weatherIcon)).setImageResource(currentConditionsDto.getWeatherIcon());

		String airQuality = context.getString(R.string.air_quality) + ": ";
		if (airQualityDto.isSuccessful()) {
			airQuality += AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi());
		} else {
			airQuality += context.getString(R.string.noData);
		}
		((TextView) view.findViewById(R.id.airQuality)).setText(airQuality);

		String precipitation = "";
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto.getPrecipitationVolume();
		} else {
			precipitation = context.getString(R.string.not_precipitation);
		}
		((TextView) view.findViewById(R.id.precipitation)).setText(precipitation);

		String feelsLikeTemp = context.getString(R.string.feelsLike) + ": " + currentConditionsDto.getFeelsLikeTemp();
		((TextView) view.findViewById(R.id.feelsLikeTemp)).setText(feelsLikeTemp);


		if (currentConditionsDto.getYesterdayTemp() != null) {
			String yesterdayCompText = WeatherUtil.makeTempCompareToYesterdayText(currentConditionsDto.getTemp(),
					currentConditionsDto.getYesterdayTemp(), tempUnit, context);
			((TextView) view.findViewById(R.id.yesterdayTemperature)).setText(yesterdayCompText);
		} else {
			view.findViewById(R.id.yesterdayTemperature).setVisibility(View.GONE);
		}
	}

	public void setHourlyForecastViews(View view, LayoutInflater layoutInflater, List<HourlyForecastDto> hourlyForecastDtoList) {
		DateTimeFormatter hour0Formatter = DateTimeFormatter.ofPattern("E 0");
		String hours = "";

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
		layoutParams.weight = 1;
		layoutParams.gravity = Gravity.CENTER;

		final int padding = (int) context.getResources().getDimension(R.dimen.forecastItemViewInLinearPadding);

		for (int i = 0; i < hourlyForecastCount; i++) {
			View itemView = layoutInflater.inflate(R.layout.view_forecast_item_in_linear, null, false);
			itemView.setLayoutParams(layoutParams);
			itemView.setPadding(0, padding, 0, padding);

			if (hourlyForecastDtoList.get(i).getHours().getHour() == 0) {
				hours = hourlyForecastDtoList.get(i).getHours().format(hour0Formatter);
			} else {
				hours = String.valueOf(hourlyForecastDtoList.get(i).getHours().getHour());
			}

			((TextView) itemView.findViewById(R.id.dateTime)).setText(hours);
			((TextView) itemView.findViewById(R.id.temperature)).setText(hourlyForecastDtoList.get(i).getTemp());
			((ImageView) itemView.findViewById(R.id.leftIcon)).setImageResource(hourlyForecastDtoList.get(i).getWeatherIcon());
			itemView.findViewById(R.id.rightIcon).setVisibility(View.GONE);
			itemView.findViewById(R.id.popLayout).setVisibility(View.GONE);
			itemView.findViewById(R.id.rainVolumeLayout).setVisibility(View.GONE);
			itemView.findViewById(R.id.snowVolumeLayout).setVisibility(View.GONE);


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
			View itemView = layoutInflater.inflate(R.layout.view_forecast_item_in_linear, null, false);
			itemView.setLayoutParams(layoutParams);
			itemView.setPadding(0, padding, 0, padding);

			((TextView) itemView.findViewById(R.id.dateTime)).setText(dailyForecastDtoList.get(day).getDate().format(dateFormatter));
			temp = dailyForecastDtoList.get(day).getMinTemp() + "/" + dailyForecastDtoList.get(day).getMaxTemp();
			((TextView) itemView.findViewById(R.id.temperature)).setText(temp);

			if (dailyForecastDtoList.get(day).getValuesList().size() == 1) {
				((ImageView) itemView.findViewById(R.id.leftIcon)).setImageResource(dailyForecastDtoList.get(day).getValuesList().get(0).getWeatherIcon());
				itemView.findViewById(R.id.rightIcon).setVisibility(View.GONE);
			} else if (dailyForecastDtoList.get(day).getValuesList().size() == 2) {
				((ImageView) itemView.findViewById(R.id.leftIcon)).setImageResource(dailyForecastDtoList.get(day).getValuesList().get(0).getWeatherIcon());
				((ImageView) itemView.findViewById(R.id.rightIcon)).setImageResource(dailyForecastDtoList.get(day).getValuesList().get(1).getWeatherIcon());
			} else if (dailyForecastDtoList.get(day).getValuesList().size() == 4) {
				((ImageView) itemView.findViewById(R.id.leftIcon)).setImageResource(dailyForecastDtoList.get(day).getValuesList().get(1).getWeatherIcon());
				((ImageView) itemView.findViewById(R.id.rightIcon)).setImageResource(dailyForecastDtoList.get(day).getValuesList().get(2).getWeatherIcon());
			}


			itemView.findViewById(R.id.popLayout).setVisibility(View.GONE);
			itemView.findViewById(R.id.rainVolumeLayout).setVisibility(View.GONE);
			itemView.findViewById(R.id.snowVolumeLayout).setVisibility(View.GONE);


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

		RelativeLayout.LayoutParams headerViewLayoutParams = getHeaderViewLayoutParams();
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
		WeatherProviderType weatherProviderType = WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherProviderTypeSet());

		if (widgetDto.isTopPriorityKma() && widgetDto.getCountryCode().equals("KR")) {
			weatherProviderType = WeatherProviderType.KMA_WEB;
		}
		WeatherRequestUtil.initWeatherSourceUniqueValues(weatherProviderType, true, context);

		RemoteViews remoteViews = createRemoteViews();

		zoneId = ZoneId.of(widgetDto.getTimeZoneId());

		JsonObject jsonObject = (JsonObject) JsonParser.parseString(widgetDto.getResponseText());

		AirQualityDto airQualityDto = AqicnResponseProcessor.parseTextToAirQualityDto(jsonObject);
		CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.parseTextToCurrentConditionsDto(context, jsonObject,
				weatherProviderType, widgetDto.getLatitude(), widgetDto.getLongitude(), zoneId);
		List<HourlyForecastDto> hourlyForecastDtoList = WeatherResponseProcessor.parseTextToHourlyForecastDtoList(context, jsonObject,
				weatherProviderType, widgetDto.getLatitude(), widgetDto.getLongitude(), zoneId);
		List<DailyForecastDto> dailyForecastDtoList = WeatherResponseProcessor.parseTextToDailyForecastDtoList(context, jsonObject,
				weatherProviderType, zoneId);

		setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), airQualityDto, currentConditionsDto,
				hourlyForecastDtoList, dailyForecastDtoList, null);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		RemoteViewsUtil.onSuccessfulProcess(remoteViews);

		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	}

	@Override
	public void setResultViews(int appWidgetId, @Nullable @org.jetbrains.annotations.Nullable MultipleWeatherRestApiCallback multipleWeatherRestApiCallback, ZoneId zoneId) {
		this.zoneId = zoneId;
		ZoneOffset zoneOffset = null;

		final WeatherProviderType weatherProviderType = WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherProviderTypeSet());

		final CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.getCurrentConditionsDto(context, multipleWeatherRestApiCallback,
				weatherProviderType, zoneId);
		final List<HourlyForecastDto> hourlyForecastDtoList = WeatherResponseProcessor.getHourlyForecastDtoList(context, multipleWeatherRestApiCallback,
				weatherProviderType, zoneId);
		final List<DailyForecastDto> dailyForecastDtoList = WeatherResponseProcessor.getDailyForecastDtoList(context, multipleWeatherRestApiCallback,
				weatherProviderType, zoneId);

		final boolean successful = currentConditionsDto != null && !hourlyForecastDtoList.isEmpty()
				&& !dailyForecastDtoList.isEmpty();

		if (successful) {
			zoneOffset = currentConditionsDto.getCurrentTime().getOffset();
			widgetDto.setTimeZoneId(zoneId.getId());
			widgetDto.setLastRefreshDateTime(multipleWeatherRestApiCallback.getRequestDateTime().toString());

			makeResponseTextToJson(multipleWeatherRestApiCallback, getRequestWeatherDataTypeSet(), widgetDto.getWeatherProviderTypeSet(), widgetDto, zoneOffset);
		}

		widgetDto.setLoadSuccessful(successful);
		super.setResultViews(appWidgetId, multipleWeatherRestApiCallback, zoneId);
	}
}
