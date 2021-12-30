package com.lifedawn.bestweather.notification.daily.viewcreator;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.forremoteviews.RemoteViewProcessor;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FifthDailyNotificationViewCreator extends AbstractDailyNotiViewCreator {
	public FifthDailyNotificationViewCreator(Context context) {
		super(context);
	}

	public void setDataViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime,
	                         AirQualityDto airQualityDto) {
		drawViews(remoteViews, addressName, lastRefreshDateTime, airQualityDto);
	}

	@Override
	public RemoteViews createRemoteViews(boolean needTempData) {
		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.view_notification);

		if (needTempData) {
			setTempDataViews(remoteViews);
		}

		return remoteViews;
	}

	public void setTempDataViews(RemoteViews remoteViews) {
		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(), WeatherResponseProcessor.getTempAirQualityDto());
	}

	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime,
	                       AirQualityDto airQualityDto) {
		RemoteViews valuesRemoteViews = new RemoteViews(context.getPackageName(), R.layout.fifth_daily_noti_view);

		valuesRemoteViews.setTextViewText(R.id.address, addressName);
		valuesRemoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));

		final String noData = "-";
		valuesRemoteViews.setTextViewText(R.id.measuring_station_name,
				context.getString(R.string.measuring_station_name) + ": " + (airQualityDto.getCityName() == null ?
						noData : airQualityDto.getCityName()));
		valuesRemoteViews.setTextViewText(R.id.airQuality,
				context.getString(R.string.currentAirQuality) + "\n" + AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi()));

		RemoteViews forecastHeader = new RemoteViews(context.getPackageName(), R.layout.air_quality_simple_forecast_item);

		forecastHeader.setViewVisibility(R.id.date, View.INVISIBLE);
		forecastHeader.setTextViewText(R.id.pm10, context.getString(R.string.pm10_str));
		forecastHeader.setTextViewText(R.id.pm25, context.getString(R.string.pm25_str));
		forecastHeader.setTextViewText(R.id.o3, context.getString(R.string.o3_str));

		valuesRemoteViews.addView(R.id.airQualityForecast, forecastHeader);

		AirQualityDto.DailyForecast current = new AirQualityDto.DailyForecast();
		current.setDate(null).setPm10(new AirQualityDto.DailyForecast.Val().setAvg(airQualityDto.getCurrent().getPm10()))
				.setPm25(new AirQualityDto.DailyForecast.Val().setAvg(airQualityDto.getCurrent().getPm25()))
				.setO3(new AirQualityDto.DailyForecast.Val().setAvg(airQualityDto.getCurrent().getO3()));

		List<AirQualityDto.DailyForecast> dailyForecastList = new ArrayList<>();
		dailyForecastList.add(current);
		dailyForecastList.addAll(airQualityDto.getDailyForecastList());

		DateTimeFormatter forecastDateFormatter = DateTimeFormatter.ofPattern("M.d E");

		for (AirQualityDto.DailyForecast item : dailyForecastList) {
			RemoteViews forecast = new RemoteViews(context.getPackageName(), R.layout.air_quality_simple_forecast_item);

			forecast.setTextViewText(R.id.date, item.getDate() == null ? context.getString(R.string.current) :
					item.getDate().format(forecastDateFormatter));
			if (item.isHasPm10()) {
				forecast.setTextViewText(R.id.pm10, AqicnResponseProcessor.getGradeDescription(item.getPm10().getAvg()));
			} else {
				forecast.setTextViewText(R.id.pm10, noData);
			}

			if (item.isHasPm25()) {
				forecast.setTextViewText(R.id.pm25, AqicnResponseProcessor.getGradeDescription(item.getPm25().getAvg()));
			} else {
				forecast.setTextViewText(R.id.pm25, noData);
			}

			if (item.isHasO3()) {
				forecast.setTextViewText(R.id.o3, AqicnResponseProcessor.getGradeDescription(item.getO3().getAvg()));
			} else {
				forecast.setTextViewText(R.id.o3, noData);
			}

			valuesRemoteViews.addView(R.id.airQualityForecast, forecast);
		}

		remoteViews.addView(R.id.valuesView, valuesRemoteViews);
	}


	@Override
	public void setResultViews(RemoteViews remoteViews, DailyPushNotificationDto dailyPushNotificationDto, Set<WeatherSourceType> weatherSourceTypeSet, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		final String refreshDateTime = multipleRestApiDownloader.getRequestDateTime().toString();

		final AirQualityDto airQualityDto = WeatherResponseProcessor.getAirQualityDto(context, multipleRestApiDownloader, null);
		final boolean successful = airQualityDto != null;

		if (successful) {
			setDataViews(remoteViews, dailyPushNotificationDto.getAddressName(), refreshDateTime, airQualityDto);
			RemoteViewProcessor.onSuccessfulProcess(remoteViews);
		} else {
			RemoteViewProcessor.onErrorProcess(remoteViews, context, RemoteViewProcessor.ErrorType.FAILED_LOAD_WEATHER_DATA);
		}
		makeNotification(remoteViews, dailyPushNotificationDto.getId());
	}

	@Override
	public Set<RequestWeatherDataType> getRequestWeatherDataTypeSet() {
		Set<RequestWeatherDataType> set = new HashSet<>();
		set.add(RequestWeatherDataType.airQuality);
		return set;
	}
}
