package com.lifedawn.bestweather.notification.daily.viewcreator;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.constants.WeatherDataType;
import com.lifedawn.bestweather.commons.constants.ValueUnits;
import com.lifedawn.bestweather.commons.constants.WeatherProviderType;
import com.lifedawn.bestweather.retrofit.util.WeatherRestApiDownloader;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherUtil;
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
		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.second_daily_noti_view);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			remoteViews.setViewPadding(R.id.root_layout, 0, 0, 0, 0);
		}


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
		String precipitation = "";
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto.getPrecipitationVolume();
		} else {
			precipitation = context.getString(R.string.not_precipitation);
		}

		String humidity = context.getString(R.string.humidity) + ": " + currentConditionsDto.getHumidity();
		remoteViews.setTextViewText(R.id.temperature, currentConditionsDto.getTemp());
		remoteViews.setTextViewText(R.id.humidity, humidity);
		remoteViews.setTextViewText(R.id.precipitation, precipitation);
		remoteViews.setImageViewResource(R.id.weatherIcon, currentConditionsDto.getWeatherIcon());

		if (currentConditionsDto.getYesterdayTemp() != null) {
			ValueUnits tempUnit =
					ValueUnits.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_unit_temp), ValueUnits.celsius.name()));
			String yesterdayCompText = WeatherUtil.makeTempCompareToYesterdayText(currentConditionsDto.getTemp(),
					currentConditionsDto.getYesterdayTemp(), tempUnit, context);
			remoteViews.setTextViewText(R.id.yesterdayTemperature, yesterdayCompText);
		} else {
			remoteViews.setViewVisibility(R.id.yesterdayTemperature, View.GONE);
		}

		if (currentConditionsDto.getWindDirection() != null) {
			remoteViews.setTextViewText(R.id.windDirection, currentConditionsDto.getWindDirection());
			remoteViews.setTextViewText(R.id.windSpeed, currentConditionsDto.getWindSpeed());
			remoteViews.setTextViewText(R.id.windStrength, currentConditionsDto.getWindStrength());
		} else {
			remoteViews.setViewVisibility(R.id.windDirection, View.GONE);
			remoteViews.setViewVisibility(R.id.windSpeed, View.GONE);
			remoteViews.setTextViewText(R.id.windStrength, context.getString(R.string.noWindData));
		}

		if (currentConditionsDto.getFeelsLikeTemp() != null) {
			String feelsLikeTemp = context.getString(R.string.feelsLike) + ": " + currentConditionsDto.getFeelsLikeTemp();
			remoteViews.setTextViewText(R.id.feelsLikeTemp, feelsLikeTemp);
		} else {
			remoteViews.setViewVisibility(R.id.feelsLikeTemp, View.GONE);
		}

		remoteViews.setTextViewText(R.id.airQuality, context.getString(R.string.air_quality) + ": " +
				AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi()));

		remoteViews.setTextViewText(R.id.address, addressName);
		remoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));
	}

	@Override
	public void setResultViews(RemoteViews remoteViews, DailyPushNotificationDto dailyPushNotificationDto, Set<WeatherProviderType> weatherProviderTypeSet, @Nullable @org.jetbrains.annotations.Nullable WeatherRestApiDownloader weatherRestApiDownloader, Set<WeatherDataType> weatherDataTypeSet) {
		final String refreshDateTime = weatherRestApiDownloader.getRequestDateTime().toString();

		zoneId = weatherRestApiDownloader.getZoneId();


		WeatherProviderType weatherProviderType = WeatherResponseProcessor.getMainWeatherSourceType(weatherProviderTypeSet);
		CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.getCurrentConditionsDto(context, weatherRestApiDownloader,
				weatherProviderType, zoneId);

		boolean successful = currentConditionsDto != null;
		if (successful) {
			ZoneOffset zoneOffset = currentConditionsDto.getCurrentTime().getOffset();

			AirQualityDto airQualityDto = WeatherResponseProcessor.getAirQualityDto(weatherRestApiDownloader,
					zoneOffset);

			setDataViews(remoteViews, dailyPushNotificationDto.getAddressName(), refreshDateTime, airQualityDto, currentConditionsDto);
			makeNotification(remoteViews, dailyPushNotificationDto.getId());
		} else {
			makeFailedNotification(dailyPushNotificationDto.getId(), context.getString(R.string.msg_failed_update));

		}

	}

	@Override
	public Set<WeatherDataType> getRequestWeatherDataTypeSet() {
		Set<WeatherDataType> set = new HashSet<>();
		set.add(WeatherDataType.currentConditions);
		set.add(WeatherDataType.airQuality);
		return set;
	}
}
