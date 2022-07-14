package com.lifedawn.bestweather.notification.daily.viewcreator;

import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ThirdDailyNotificationViewCreator extends AbstractDailyNotiViewCreator {
	private final int cellCount = 6;

	public ThirdDailyNotificationViewCreator(Context context) {
		super(context);
	}

	@Override
	public RemoteViews createRemoteViews(boolean needTempData) {
		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.third_daily_noti_view);

		if (needTempData) {
			setTempDataViews(remoteViews);
		}

		return remoteViews;
	}

	public void setDataViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, List<DailyForecastDto> dailyForecastDtoList) {
		drawViews(remoteViews, addressName, lastRefreshDateTime, dailyForecastDtoList);
	}

	@Override
	public void setTempDataViews(RemoteViews remoteViews) {
		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(),
				WeatherResponseProcessor.getTempDailyForecastDtoList(context, cellCount));
	}

	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime,
	                       List<DailyForecastDto> dailyForecastDtoList) {
		final String mm = "mm";
		final String cm = "cm";

		boolean haveRain = false;
		boolean haveSnow = false;

		for (int cell = 0; cell < cellCount; cell++) {
			if (dailyForecastDtoList.get(cell).getValuesList().size() == 1) {
				if (dailyForecastDtoList.get(cell).getValuesList().get(0).isHasRainVolume()) {
					haveRain = true;
				}

				if (dailyForecastDtoList.get(cell).getValuesList().get(0).isHasSnowVolume()) {
					haveSnow = true;
				}
			} else {
				if (dailyForecastDtoList.get(cell).getValuesList().get(0).isHasRainVolume() ||
						dailyForecastDtoList.get(cell).getValuesList().get(1).isHasRainVolume()) {
					haveRain = true;
				}

				if (dailyForecastDtoList.get(cell).getValuesList().get(0).isHasSnowVolume() ||
						dailyForecastDtoList.get(cell).getValuesList().get(1).isHasSnowVolume()) {
					haveSnow = true;
				}
			}
		}

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("E");
		String pop = null;
		float rainVolume = 0f;
		float snowVolume = 0f;

		for (int cell = 0; cell < cellCount; cell++) {
			RemoteViews forecastRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_forecast_item_in_linear);

			forecastRemoteViews.setTextViewText(R.id.dateTime, dailyForecastDtoList.get(cell).getDate().format(dateTimeFormatter));
			rainVolume = 0f;
			snowVolume = 0f;

			if (dailyForecastDtoList.get(cell).getValuesList().size() == 1) {
				forecastRemoteViews.setImageViewResource(R.id.leftIcon, dailyForecastDtoList.get(cell).getValuesList().get(0).getWeatherIcon());
				forecastRemoteViews.setViewVisibility(R.id.rightIcon, View.GONE);

				pop = dailyForecastDtoList.get(cell).getValuesList().get(0).getPop();
				if (dailyForecastDtoList.get(cell).getValuesList().get(0).isHasRainVolume()) {
					rainVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getValuesList().get(0).getRainVolume().replace(cm, "").replace(mm,
							""));
				}
				if (dailyForecastDtoList.get(cell).getValuesList().get(0).isHasSnowVolume()) {
					snowVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getValuesList().get(0).getSnowVolume().replace(cm, "").replace(mm,
							""));
				}
			} else {
				forecastRemoteViews.setImageViewResource(R.id.leftIcon, dailyForecastDtoList.get(cell).getValuesList().get(0).getWeatherIcon());
				forecastRemoteViews.setImageViewResource(R.id.rightIcon, dailyForecastDtoList.get(cell).getValuesList().get(1).getWeatherIcon());

				pop = dailyForecastDtoList.get(cell).getValuesList().get(0).getPop() + "/" + dailyForecastDtoList.get(cell).getValuesList().get(1).getPop();
				if (dailyForecastDtoList.get(cell).getValuesList().get(0).isHasRainVolume()) {
					rainVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getValuesList().get(0).getRainVolume().replace(cm, "").replace(mm,
							""));
				}
				if (dailyForecastDtoList.get(cell).getValuesList().get(1).isHasRainVolume()) {
					rainVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getValuesList().get(1).getRainVolume().replace(cm, "").replace(mm,
							""));
				}

				if (dailyForecastDtoList.get(cell).getValuesList().get(0).isHasSnowVolume()) {
					snowVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getValuesList().get(0).getSnowVolume().replace(cm, "").replace(mm,
							""));
				}
				if (dailyForecastDtoList.get(cell).getValuesList().get(1).isHasSnowVolume()) {
					snowVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getValuesList().get(1).getSnowVolume().replace(cm, "").replace(mm,
							""));
				}
			}

			if (haveRain) {
				if (rainVolume > 0f) {
					forecastRemoteViews.setTextViewText(R.id.rainVolume, String.format(Locale.getDefault(), "%.1f", rainVolume));
				} else {
					forecastRemoteViews.setViewVisibility(R.id.rainVolumeLayout, View.INVISIBLE);
				}
			} else {
				forecastRemoteViews.setViewVisibility(R.id.rainVolumeLayout, View.GONE);
			}

			if (haveSnow) {
				if (snowVolume > 0f) {
					forecastRemoteViews.setTextViewText(R.id.snowVolume, String.format(Locale.getDefault(), "%.1f", snowVolume));
				} else {
					forecastRemoteViews.setViewVisibility(R.id.snowVolumeLayout, View.INVISIBLE);
				}
			} else {
				forecastRemoteViews.setViewVisibility(R.id.snowVolumeLayout, View.GONE);
			}

			forecastRemoteViews.setTextViewText(R.id.temperature, dailyForecastDtoList.get(cell).getMinTemp() + "/" +
					dailyForecastDtoList.get(cell).getMaxTemp());
			forecastRemoteViews.setTextViewText(R.id.pop, pop);

			remoteViews.addView(R.id.dailyForecast, forecastRemoteViews);
		}

		remoteViews.setTextViewText(R.id.address, addressName);
		remoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));
	}

	@Override
	public void setResultViews(RemoteViews remoteViews, DailyPushNotificationDto dailyPushNotificationDto, Set<WeatherProviderType> weatherProviderTypeSet, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader, Set<WeatherDataType> weatherDataTypeSet) {
		final String refreshDateTime = multipleRestApiDownloader.getRequestDateTime().toString();
		WeatherProviderType weatherProviderType = WeatherResponseProcessor.getMainWeatherSourceType(weatherProviderTypeSet);
		List<DailyForecastDto> dailyForecastDtoList = WeatherResponseProcessor.getDailyForecastDtoList(context, multipleRestApiDownloader
				, weatherProviderType);
		boolean successful = !dailyForecastDtoList.isEmpty();

		if (successful) {
			setDataViews(remoteViews, dailyPushNotificationDto.getAddressName(), refreshDateTime, dailyForecastDtoList);
			makeNotification(remoteViews, dailyPushNotificationDto.getId());
		} else {
			makeFailedNotification(dailyPushNotificationDto.getId(), context.getString(R.string.msg_failed_update));

		}

	}

	@Override
	public Set<WeatherDataType> getRequestWeatherDataTypeSet() {
		Set<WeatherDataType> set = new HashSet<>();
		set.add(WeatherDataType.dailyForecast);
		return set;
	}
}
