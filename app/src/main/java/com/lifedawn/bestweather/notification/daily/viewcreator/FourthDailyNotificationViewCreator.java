package com.lifedawn.bestweather.notification.daily.viewcreator;

import android.content.Context;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.retrofit.util.WeatherRestApiDownloader;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

public class FourthDailyNotificationViewCreator extends AbstractDailyNotiViewCreator {

	public FourthDailyNotificationViewCreator(Context context) {
		super(context);
	}

	public void setDataViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime,
	                         AirQualityDto airQualityDto) {
		drawViews(remoteViews, addressName, lastRefreshDateTime, airQualityDto);
	}

	@Override
	public RemoteViews createRemoteViews(boolean needTempData) {
		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.fourth_daily_noti_view);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			remoteViews.setViewPadding(R.id.root_layout, 0, 0, 0, 0);
		}


		if (needTempData) {
			setTempDataViews(remoteViews);
		}

		return remoteViews;
	}

	@Override
	public void setTempDataViews(RemoteViews remoteViews) {
		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(),
				WeatherResponseProcessor.getTempAirQualityDto());
	}


	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, AirQualityDto airQualityDto) {
		remoteViews.setTextViewText(R.id.address, addressName);
		remoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));

		final String noData = "-";
		remoteViews.setTextViewText(R.id.measuring_station_name,
				context.getString(R.string.measuring_station_name) + ": " + (airQualityDto.getCityName() == null ?
						noData : airQualityDto.getCityName()));
		remoteViews.setTextViewText(R.id.airQuality,
				context.getString(R.string.currentAirQuality) + "\n" + AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi()));

		remoteViews.setTextViewText(R.id.pm10, !airQualityDto.getCurrent().isHasPm10() ? noData :
				AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getPm10()));
		remoteViews.setTextViewText(R.id.pm25, !airQualityDto.getCurrent().isHasPm25() ? noData :
				AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getPm25()));
		remoteViews.setTextViewText(R.id.so2, !airQualityDto.getCurrent().isHasSo2() ? noData :
				AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getSo2()));
		remoteViews.setTextViewText(R.id.co, !airQualityDto.getCurrent().isHasCo() ? noData :
				AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getCo()));
		remoteViews.setTextViewText(R.id.o3, !airQualityDto.getCurrent().isHasO3() ? noData :
				AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getO3()));
		remoteViews.setTextViewText(R.id.no2, !airQualityDto.getCurrent().isHasNo2() ? noData :
				AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getNo2()));
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
