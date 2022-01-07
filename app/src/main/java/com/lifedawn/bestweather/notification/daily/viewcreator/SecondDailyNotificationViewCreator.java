package com.lifedawn.bestweather.notification.daily.viewcreator;

import android.content.Context;
import android.view.View;
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
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

public class SecondDailyNotificationViewCreator extends AbstractDailyNotiViewCreator {
	private final int cellCount = 6;

	@Override
	public RemoteViews createRemoteViews(boolean needTempData) {
		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.view_notification);

		if (needTempData) {
			setTempDataViews(remoteViews);
		}

		return remoteViews;
	}

	public SecondDailyNotificationViewCreator(Context context) {
		super(context);
	}


	public void setDataViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, AirQualityDto airQualityDto, CurrentConditionsDto currentConditionsDto) {
		drawViews(remoteViews, addressName, lastRefreshDateTime, airQualityDto, currentConditionsDto);
	}

	@Override
	public void setTempDataViews(RemoteViews remoteViews) {
		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(), WeatherResponseProcessor.getTempAirQualityDto(),
				WeatherResponseProcessor.getTempCurrentConditionsDto(context));
	}

	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, AirQualityDto airQualityDto,
	                       CurrentConditionsDto currentConditionsDto) {
		RemoteViews valuesRemoveViews = new RemoteViews(context.getPackageName(), R.layout.second_daily_noti_view);

		String precipitation = "";
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto.getPrecipitationVolume();
		} else {
			precipitation = context.getString(R.string.not_precipitation);
		}
		valuesRemoveViews.setTextViewText(R.id.temperature, currentConditionsDto.getTemp());
		valuesRemoveViews.setTextViewText(R.id.humidity, currentConditionsDto.getHumidity());
		valuesRemoveViews.setTextViewText(R.id.precipitation, precipitation);
		valuesRemoveViews.setImageViewResource(R.id.weatherIcon, currentConditionsDto.getWeatherIcon());

		if (currentConditionsDto.getWindDirection() != null) {
			valuesRemoveViews.setTextViewText(R.id.windDirection, currentConditionsDto.getWindDirection());
			valuesRemoveViews.setTextViewText(R.id.windSpeed, currentConditionsDto.getWindSpeed());
			valuesRemoveViews.setTextViewText(R.id.windStrength, currentConditionsDto.getWindStrength());
		} else {
			valuesRemoveViews.setViewVisibility(R.id.windDirection, View.GONE);
			valuesRemoveViews.setViewVisibility(R.id.windSpeed, View.GONE);
			valuesRemoveViews.setTextViewText(R.id.windStrength, context.getString(R.string.noWindData));
		}

		valuesRemoveViews.setTextViewText(R.id.airQuality, context.getString(R.string.air_quality) + ": " +
				AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi()));

		valuesRemoveViews.setTextViewText(R.id.address, addressName);
		valuesRemoveViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));

		remoteViews.addView(R.id.valuesView, valuesRemoveViews);
	}

	@Override
	public void setResultViews(RemoteViews remoteViews, DailyPushNotificationDto dailyPushNotificationDto, Set<WeatherDataSourceType> weatherDataSourceTypeSet, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		final String refreshDateTime = multipleRestApiDownloader.getRequestDateTime().toString();

		WeatherDataSourceType weatherDataSourceType = WeatherResponseProcessor.getMainWeatherSourceType(weatherDataSourceTypeSet);
		CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.getCurrentConditionsDto(context, multipleRestApiDownloader,
				weatherDataSourceType);

		boolean successful = currentConditionsDto != null;
		if (successful) {
			ZoneOffset zoneOffset = currentConditionsDto.getCurrentTime().getOffset();

			AirQualityDto airQualityDto = WeatherResponseProcessor.getAirQualityDto(context, multipleRestApiDownloader,
					zoneOffset);
			if (airQualityDto == null) {
				airQualityDto = new AirQualityDto();
				airQualityDto.setAqi(-1);
			}

			setDataViews(remoteViews, dailyPushNotificationDto.getAddressName(), refreshDateTime, airQualityDto, currentConditionsDto);
			RemoteViewProcessor.onSuccessfulProcess(remoteViews);
		} else {
			RemoteViewProcessor.onErrorProcess(remoteViews, context, RemoteViewProcessor.ErrorType.FAILED_LOAD_WEATHER_DATA);
		}
		makeNotification(remoteViews, dailyPushNotificationDto.getId());
	}

	@Override
	public Set<RequestWeatherDataType> getRequestWeatherDataTypeSet() {
		Set<RequestWeatherDataType> set = new HashSet<>();
		set.add(RequestWeatherDataType.currentConditions);
		set.add(RequestWeatherDataType.airQuality);
		return set;
	}
}
