package com.lifedawn.bestweather.notification.daily.viewcreator;

import android.content.Context;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.constants.WeatherDataType;
import com.lifedawn.bestweather.commons.constants.WeatherProviderType;
import com.lifedawn.bestweather.data.remote.retrofit.callback.WeatherRestApiDownloader;
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
		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.fifth_daily_noti_view);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			remoteViews.setViewPadding(R.id.root_layout, 0, 0, 0, 0);
		}


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
		remoteViews.setTextViewText(R.id.address, addressName);
		remoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));

		final String noData = "-";
		remoteViews.setTextViewText(R.id.measuring_station_name,
				context.getString(R.string.measuring_station_name) + ": " + (airQualityDto.getCityName() == null ? noData : airQualityDto.getCityName()));
		remoteViews.setTextViewText(R.id.airQuality,
				context.getString(R.string.currentAirQuality) + "\n" + AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi()));

		AirQualityDto.DailyForecast current = new AirQualityDto.DailyForecast();
		current.setDate(null).setPm10(new AirQualityDto.DailyForecast.Val().setAvg(airQualityDto.getCurrent().getPm10()))
				.setPm25(new AirQualityDto.DailyForecast.Val().setAvg(airQualityDto.getCurrent().getPm25()))
				.setO3(new AirQualityDto.DailyForecast.Val().setAvg(airQualityDto.getCurrent().getO3()));

		List<AirQualityDto.DailyForecast> dailyForecastList = new ArrayList<>();
		dailyForecastList.add(current);
		dailyForecastList.addAll(airQualityDto.getDailyForecastList());

		final DateTimeFormatter forecastDateFormatter = DateTimeFormatter.ofPattern("E");
		final String packageName = context.getPackageName();

		final int maxCount = 7;
		int count = dailyForecastList.size();
		if (maxCount < count) {
			count = maxCount;
		}

		int i = 1;
		for (AirQualityDto.DailyForecast item : dailyForecastList) {
			if (i++ > count) {
				break;
			}

			RemoteViews forecastItemView = new RemoteViews(packageName, R.layout.item_view_color_airquality);

			forecastItemView.setTextViewText(R.id.date, item.getDate() == null ? context.getString(R.string.current) :
					item.getDate().format(forecastDateFormatter));

			if (item.isHasPm10()) {
				forecastItemView.setTextViewText(R.id.pm10, item.getPm10().getAvg().toString());
				forecastItemView.setTextColor(R.id.pm10, AqicnResponseProcessor.getGradeColorId(item.getPm10().getAvg()));
			} else {
				forecastItemView.setTextViewText(R.id.pm10, "?");
			}

			if (item.isHasPm25()) {
				forecastItemView.setTextViewText(R.id.pm25, item.getPm25().getAvg().toString());
				forecastItemView.setTextColor(R.id.pm25, AqicnResponseProcessor.getGradeColorId(item.getPm25().getAvg()));
			} else {
				forecastItemView.setTextViewText(R.id.pm25, "?");
			}

			if (item.isHasO3()) {
				forecastItemView.setTextViewText(R.id.o3, item.getO3().getAvg().toString());
				forecastItemView.setTextColor(R.id.o3, AqicnResponseProcessor.getGradeColorId(item.getO3().getAvg()));
			} else {
				forecastItemView.setTextViewText(R.id.o3, "?");
			}

			remoteViews.addView(R.id.forecast, forecastItemView);
		}
	}


	@Override
	public void setResultViews(RemoteViews remoteViews, DailyPushNotificationDto dailyPushNotificationDto, Set<WeatherProviderType> weatherProviderTypeSet, @Nullable @org.jetbrains.annotations.Nullable WeatherRestApiDownloader weatherRestApiDownloader, Set<WeatherDataType> weatherDataTypeSet) {
		final String refreshDateTime = weatherRestApiDownloader.getRequestDateTime().toString();
		zoneId = weatherRestApiDownloader.getZoneId();

		final AirQualityDto airQualityDto = WeatherResponseProcessor.getAirQualityDto(weatherRestApiDownloader, null);
		final boolean successful = airQualityDto.isSuccessful();

		if (successful) {
			setDataViews(remoteViews, dailyPushNotificationDto.getAddressName(), refreshDateTime, airQualityDto);
			makeNotification(remoteViews, dailyPushNotificationDto.getId());
		} else {
			makeFailedNotification(dailyPushNotificationDto.getId(), context.getString(R.string.msg_failed_update));
		}
	}

	@Override
	public Set<WeatherDataType> getRequestWeatherDataTypeSet() {
		Set<WeatherDataType> set = new HashSet<>();
		set.add(WeatherDataType.airQuality);
		return set;
	}
}
