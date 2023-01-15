package com.lifedawn.bestweather.ui.notification.ongoing;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.constants.ValueUnits;
import com.lifedawn.bestweather.commons.constants.WeatherProviderType;
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil;

import com.lifedawn.bestweather.data.MyApplication;
import com.lifedawn.bestweather.ui.notification.model.OngoingNotificationDto;
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback;
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor;
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.WeatherUtil;
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto;
import com.lifedawn.bestweather.data.local.weather.models.CurrentConditionsDto;
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OngoingNotiViewCreator {
	private Context context;

	private final int hourlyForecastCount = 8;
	private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a h:mm");
	private final ValueUnits tempUnit;
	private final OngoingNotificationHelper ongoingNotificationHelper;
	private final OngoingNotificationDto ongoingNotificationDto;


	public OngoingNotiViewCreator(Context context, OngoingNotificationDto ongoingNotificationDto) {
		this.context = context;
		this.ongoingNotificationDto = ongoingNotificationDto;

		tempUnit = MyApplication.VALUE_UNIT_OBJ.getTempUnit();
		ongoingNotificationHelper = new OngoingNotificationHelper(context);
	}


	public RemoteViews[] createRemoteViews(boolean temp) {
		RemoteViews collapsedRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_ongoing_notification_collapsed);
		RemoteViews expandedRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_ongoing_notification_expanded);

		if (temp) {
			setHourlyForecastViews(expandedRemoteViews, WeatherResponseProcessor.getTempHourlyForecastDtoList(context, hourlyForecastCount));
		} else {
			collapsedRemoteViews.setOnClickPendingIntent(R.id.refreshLayout, ongoingNotificationHelper.getRefreshPendingIntent());
			expandedRemoteViews.setOnClickPendingIntent(R.id.refreshLayout, ongoingNotificationHelper.getRefreshPendingIntent());
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			expandedRemoteViews.setViewPadding(R.id.root_layout, 0, 0, 0, 0);
		}

		return new RemoteViews[]{collapsedRemoteViews, expandedRemoteViews};
	}


	protected void setResultViews(RemoteViews collapsedRemoteViews, RemoteViews expandedRemoteViews,
	                              WeatherProviderType requestWeatherProviderType, @Nullable @org.jetbrains.annotations.Nullable MultipleWeatherRestApiCallback multipleWeatherRestApiCallback,
	                              OnRemoteViewsCallback onRemoteViewsCallback) {
		ZoneOffset zoneOffset = null;
		setHeaderViews(collapsedRemoteViews, ongoingNotificationDto.getDisplayName(), multipleWeatherRestApiCallback.getRequestDateTime().toString());
		setHeaderViews(expandedRemoteViews, ongoingNotificationDto.getDisplayName(), multipleWeatherRestApiCallback.getRequestDateTime().toString());

		final ZoneId zoneId = ZoneId.of(ongoingNotificationDto.getZoneId());

		int icon = R.mipmap.ic_launcher_round;
		String temperature = null;

		final CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.getCurrentConditionsDto(context, multipleWeatherRestApiCallback,
				requestWeatherProviderType, zoneId);

		final List<HourlyForecastDto> hourlyForecastDtoList = WeatherResponseProcessor.getHourlyForecastDtoList(context, multipleWeatherRestApiCallback,
				requestWeatherProviderType, zoneId);


		final boolean successful = currentConditionsDto != null && !hourlyForecastDtoList.isEmpty();

		if (successful) {
			zoneOffset = currentConditionsDto.getCurrentTime().getOffset();
			setCurrentConditionsViews(expandedRemoteViews, currentConditionsDto);
			setCollapsedCurrentConditionsViews(collapsedRemoteViews, currentConditionsDto);

			icon = currentConditionsDto.getWeatherIcon();
			temperature = currentConditionsDto.getTemp().replace(MyApplication.VALUE_UNIT_OBJ.getTempUnitText(), "°");

			AirQualityDto airQualityDto = WeatherResponseProcessor.getAirQualityDto(multipleWeatherRestApiCallback, zoneOffset);
			if (airQualityDto.isSuccessful()) {
				setAirQualityViews(expandedRemoteViews, AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi()));
			} else {
				setAirQualityViews(expandedRemoteViews, context.getString(R.string.noData));
			}

			setHourlyForecastViews(expandedRemoteViews, hourlyForecastDtoList);

			RemoteViewsUtil.onSuccessfulProcess(collapsedRemoteViews);
			RemoteViewsUtil.onSuccessfulProcess(expandedRemoteViews);
		} else {
			setAirQualityViews(expandedRemoteViews, context.getString(R.string.noData));
			expandedRemoteViews.setOnClickPendingIntent(R.id.refreshBtn, ongoingNotificationHelper.getRefreshPendingIntent());
			collapsedRemoteViews.setOnClickPendingIntent(R.id.refreshBtn, ongoingNotificationHelper.getRefreshPendingIntent());
			RemoteViewsUtil.onErrorProcess(collapsedRemoteViews, context, RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA);
			RemoteViewsUtil.onErrorProcess(expandedRemoteViews, context, RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA);
		}

		onRemoteViewsCallback.onCreateFinished(collapsedRemoteViews, expandedRemoteViews, icon, temperature, true);
	}

	public void setHeaderViews(RemoteViews remoteViews, String addressName, String dateTime) {
		remoteViews.setTextViewText(R.id.address, addressName);
		remoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(dateTime).format(dateTimeFormatter));
	}

	public void setAirQualityViews(RemoteViews remoteViews, String value) {
		String airQuality = context.getString(R.string.air_quality) + ": " + value;
		remoteViews.setTextViewText(R.id.airQuality, airQuality);
	}

	public void setCurrentConditionsViews(RemoteViews remoteViews, CurrentConditionsDto currentConditionsDto) {
		remoteViews.setImageViewResource(R.id.weatherIcon, currentConditionsDto.getWeatherIcon());
		String precipitation = "";
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto.getPrecipitationVolume();
		} else {
			precipitation = context.getString(R.string.not_precipitation);
		}
		remoteViews.setTextViewText(R.id.precipitation, precipitation);
		remoteViews.setTextViewText(R.id.temperature, currentConditionsDto.getTemp());
		remoteViews.setTextViewText(R.id.feelsLikeTemp, new String(context.getString(R.string.feelsLike) + ": " + currentConditionsDto.getFeelsLikeTemp()));

		if (currentConditionsDto.getYesterdayTemp() != null) {
			String yesterdayCompText = WeatherUtil.makeTempCompareToYesterdayText(currentConditionsDto.getTemp(),
					currentConditionsDto.getYesterdayTemp(), tempUnit, context);
			remoteViews.setTextViewText(R.id.yesterdayTemperature, yesterdayCompText);
			remoteViews.setViewVisibility(R.id.yesterdayTemperature, View.VISIBLE);
		} else {
			remoteViews.setViewVisibility(R.id.yesterdayTemperature, View.GONE);
		}
	}

	public void setCollapsedCurrentConditionsViews(RemoteViews remoteViews, CurrentConditionsDto currentConditionsDto) {
		remoteViews.setImageViewResource(R.id.weatherIcon, currentConditionsDto.getWeatherIcon());

		remoteViews.setTextViewText(R.id.temperature, currentConditionsDto.getTemp());
		remoteViews.setTextViewText(R.id.feelsLikeTemp, new String(context.getString(R.string.feelsLike) + ": " + currentConditionsDto.getFeelsLikeTemp()));

		if (currentConditionsDto.getYesterdayTemp() != null) {
			String yesterdayCompText = WeatherUtil.makeTempCompareToYesterdayText(currentConditionsDto.getTemp(),
					currentConditionsDto.getYesterdayTemp(), tempUnit, context);
			remoteViews.setTextViewText(R.id.yesterdayTemperature, yesterdayCompText);
			remoteViews.setViewVisibility(R.id.yesterdayTemperature, View.VISIBLE);
		} else {
			remoteViews.setViewVisibility(R.id.yesterdayTemperature, View.GONE);
		}
	}

	public void setHourlyForecastViews(RemoteViews remoteViews, List<HourlyForecastDto> hourlyForecastDtoList) {
		remoteViews.removeAllViews(R.id.hourlyForecast);
		final int textColor = ContextCompat.getColor(context, R.color.textColorInNotification);
		DateTimeFormatter hour0Formatter = DateTimeFormatter.ofPattern("E 0");
		String hours = null;

		boolean haveRain = false;
		boolean haveSnow = false;

		for (int i = 0; i < hourlyForecastCount; i++) {
			if (hourlyForecastDtoList.get(i).isHasRain()) {
				haveRain = true;
			}
			if (hourlyForecastDtoList.get(i).isHasSnow()) {
				haveSnow = true;
			}
		}

		final String mm = "mm";
		final String cm = "cm";

		for (int i = 0; i < hourlyForecastCount; i++) {
			RemoteViews childRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_forecast_item_in_linear);

			if (haveRain) {
				if (hourlyForecastDtoList.get(i).isHasRain()) {
					childRemoteViews.setTextViewText(R.id.rainVolume, hourlyForecastDtoList.get(i).getRainVolume()
							.replace(mm, "").replace(cm, ""));
					childRemoteViews.setTextColor(R.id.rainVolume, textColor);
				} else {
					childRemoteViews.setViewVisibility(R.id.rainVolumeLayout, View.INVISIBLE);
				}
			} else {
				childRemoteViews.setViewVisibility(R.id.rainVolumeLayout, View.GONE);
			}

			if (haveSnow) {
				if (hourlyForecastDtoList.get(i).isHasSnow()) {
					childRemoteViews.setTextViewText(R.id.snowVolume, hourlyForecastDtoList.get(i).getSnowVolume()
							.replace(mm, "").replace(cm, ""));
					childRemoteViews.setTextColor(R.id.snowVolume, textColor);
				} else {
					childRemoteViews.setViewVisibility(R.id.snowVolumeLayout, View.INVISIBLE);
				}
			} else {
				childRemoteViews.setViewVisibility(R.id.snowVolumeLayout, View.GONE);
			}

			if (hourlyForecastDtoList.get(i).getHours().getHour() == 0) {
				hours = hourlyForecastDtoList.get(i).getHours().format(hour0Formatter);
			} else {
				hours = String.valueOf(hourlyForecastDtoList.get(i).getHours().getHour());
			}

			childRemoteViews.setTextViewText(R.id.dateTime, hours);
			childRemoteViews.setTextViewText(R.id.pop, hourlyForecastDtoList.get(i).getPop());
			childRemoteViews.setTextViewText(R.id.temperature, hourlyForecastDtoList.get(i).getTemp());
			childRemoteViews.setImageViewResource(R.id.leftIcon, hourlyForecastDtoList.get(i).getWeatherIcon());
			childRemoteViews.setViewVisibility(R.id.rightIcon, View.GONE);

			childRemoteViews.setTextColor(R.id.dateTime, textColor);
			childRemoteViews.setTextColor(R.id.temperature, textColor);
			childRemoteViews.setTextColor(R.id.pop, textColor);

			remoteViews.addView(R.id.hourlyForecast, childRemoteViews);
		}
	}


	public RemoteViews[] createFailedNotification(RemoteViewsUtil.ErrorType errorType) {
		RemoteViews[] remoteViews = createRemoteViews(false);

		remoteViews[0].setOnClickPendingIntent(R.id.refreshBtn, ongoingNotificationHelper.getRefreshPendingIntent());
		remoteViews[1].setOnClickPendingIntent(R.id.refreshBtn, ongoingNotificationHelper.getRefreshPendingIntent());
		RemoteViewsUtil.onErrorProcess(remoteViews[0], context, errorType);
		RemoteViewsUtil.onErrorProcess(remoteViews[1], context, errorType);

		return remoteViews;
	}


	public interface OnRemoteViewsCallback {
		void onCreateFinished(RemoteViews collapsedRemoteViews, RemoteViews expandedRemoteViews, int icon, @Nullable String temperature,
		                      boolean isFinished);
	}
}
