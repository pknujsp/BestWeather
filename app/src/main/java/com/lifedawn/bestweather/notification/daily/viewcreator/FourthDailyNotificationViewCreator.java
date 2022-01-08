package com.lifedawn.bestweather.notification.daily.viewcreator;

import android.content.Context;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.forremoteviews.RemoteViewProcessor;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;

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
		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.view_notification);

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
		RemoteViews valuesRemoveViews = new RemoteViews(context.getPackageName(), R.layout.fourth_daily_noti_view);

		valuesRemoveViews.setTextViewText(R.id.address, addressName);
		valuesRemoveViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));

		final String noData = "-";
		valuesRemoveViews.setTextViewText(R.id.measuring_station_name,
				context.getString(R.string.measuring_station_name) + ": " + (airQualityDto.getCityName() == null ?
						noData : airQualityDto.getCityName()));
		valuesRemoveViews.setTextViewText(R.id.airQuality,
				context.getString(R.string.currentAirQuality) + "\n" + AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi()));

		valuesRemoveViews.setTextViewText(R.id.pm10, !airQualityDto.getCurrent().isHasPm10() ? noData :
				AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getPm10()));
		valuesRemoveViews.setTextViewText(R.id.pm25, !airQualityDto.getCurrent().isHasPm25() ? noData :
				AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getPm25()));
		valuesRemoveViews.setTextViewText(R.id.so2, !airQualityDto.getCurrent().isHasSo2() ? noData :
				AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getSo2()));
		valuesRemoveViews.setTextViewText(R.id.co, !airQualityDto.getCurrent().isHasCo() ? noData :
				AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getCo()));
		valuesRemoveViews.setTextViewText(R.id.o3, !airQualityDto.getCurrent().isHasO3() ? noData :
				AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getO3()));
		valuesRemoveViews.setTextViewText(R.id.no2, !airQualityDto.getCurrent().isHasNo2() ? noData :
				AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getNo2()));

		remoteViews.addView(R.id.valuesView, valuesRemoveViews);
	}


	@Override
	public void setResultViews(RemoteViews remoteViews, DailyPushNotificationDto dailyPushNotificationDto, Set<WeatherDataSourceType> weatherDataSourceTypeSet, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
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
