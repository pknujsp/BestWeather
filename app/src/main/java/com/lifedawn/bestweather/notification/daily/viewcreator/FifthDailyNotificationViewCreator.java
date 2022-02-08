package com.lifedawn.bestweather.notification.daily.viewcreator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.forremoteviews.RemoteViewsUtil;
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
		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.fifth_daily_noti_view);

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
				forecastItemView.setImageViewResource(R.id.pm10, R.drawable.ic_baseline_error_24);
			}

			if (item.isHasPm25()) {
				forecastItemView.setTextViewText(R.id.pm25, item.getPm25().getAvg().toString());
				forecastItemView.setTextColor(R.id.pm25, AqicnResponseProcessor.getGradeColorId(item.getPm25().getAvg()));
			} else {
				forecastItemView.setImageViewResource(R.id.pm25, R.drawable.ic_baseline_error_24);
			}

			if (item.isHasO3()) {
				forecastItemView.setTextViewText(R.id.o3, item.getO3().getAvg().toString());
				forecastItemView.setTextColor(R.id.o3, AqicnResponseProcessor.getGradeColorId(item.getO3().getAvg()));
			} else {
				forecastItemView.setImageViewResource(R.id.o3, R.drawable.ic_baseline_error_24);
			}

			remoteViews.addView(R.id.forecast, forecastItemView);
		}
	}


	@Override
	public void setResultViews(RemoteViews remoteViews, DailyPushNotificationDto dailyPushNotificationDto, Set<WeatherDataSourceType> weatherDataSourceTypeSet, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		final String refreshDateTime = multipleRestApiDownloader.getRequestDateTime().toString();
		final AirQualityDto airQualityDto = WeatherResponseProcessor.getAirQualityDto(context, multipleRestApiDownloader, null);
		final boolean successful = airQualityDto.isSuccessful();

		if (successful) {
			setDataViews(remoteViews, dailyPushNotificationDto.getAddressName(), refreshDateTime, airQualityDto);
			makeNotification(remoteViews, dailyPushNotificationDto.getId());
		} else {
			makeFailedNotification(dailyPushNotificationDto.getId(), context.getString(R.string.msg_failed_update));
		}
	}

	@Override
	public Set<RequestWeatherDataType> getRequestWeatherDataTypeSet() {
		Set<RequestWeatherDataType> set = new HashSet<>();
		set.add(RequestWeatherDataType.airQuality);
		return set;
	}
}
