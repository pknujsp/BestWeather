package com.lifedawn.bestweather.notification.daily.viewcreator;

import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.forremoteviews.RemoteViewsUtil;
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
			if (dailyForecastDtoList.get(cell).isSingle()) {
				if (dailyForecastDtoList.get(cell).getSingleValues().isHasRainVolume()) {
					haveRain = true;
				}

				if (dailyForecastDtoList.get(cell).getSingleValues().isHasSnowVolume()) {
					haveSnow = true;
				}
			} else {
				if (dailyForecastDtoList.get(cell).getAmValues().isHasRainVolume() ||
						dailyForecastDtoList.get(cell).getPmValues().isHasRainVolume()) {
					haveRain = true;
				}

				if (dailyForecastDtoList.get(cell).getAmValues().isHasSnowVolume() ||
						dailyForecastDtoList.get(cell).getPmValues().isHasSnowVolume()) {
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

			if (dailyForecastDtoList.get(cell).isSingle()) {
				forecastRemoteViews.setImageViewResource(R.id.leftIcon, dailyForecastDtoList.get(cell).getSingleValues().getWeatherIcon());
				forecastRemoteViews.setViewVisibility(R.id.rightIcon, View.GONE);

				pop = dailyForecastDtoList.get(cell).getSingleValues().getPop();
				if (dailyForecastDtoList.get(cell).getSingleValues().isHasRainVolume()) {
					rainVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getSingleValues().getRainVolume().replace(cm, "").replace(mm,
							""));
				}
				if (dailyForecastDtoList.get(cell).getSingleValues().isHasSnowVolume()) {
					snowVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getSingleValues().getSnowVolume().replace(cm, "").replace(mm,
							""));
				}
			} else {
				forecastRemoteViews.setImageViewResource(R.id.leftIcon, dailyForecastDtoList.get(cell).getAmValues().getWeatherIcon());
				forecastRemoteViews.setImageViewResource(R.id.rightIcon, dailyForecastDtoList.get(cell).getPmValues().getWeatherIcon());

				pop = dailyForecastDtoList.get(cell).getAmValues().getPop() + "/" + dailyForecastDtoList.get(cell).getPmValues().getPop();
				if (dailyForecastDtoList.get(cell).getAmValues().isHasRainVolume()) {
					rainVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getAmValues().getRainVolume().replace(cm, "").replace(mm,
							""));
				}
				if (dailyForecastDtoList.get(cell).getPmValues().isHasRainVolume()) {
					rainVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getPmValues().getRainVolume().replace(cm, "").replace(mm,
							""));
				}

				if (dailyForecastDtoList.get(cell).getAmValues().isHasSnowVolume()) {
					snowVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getAmValues().getSnowVolume().replace(cm, "").replace(mm,
							""));
				}
				if (dailyForecastDtoList.get(cell).getPmValues().isHasSnowVolume()) {
					snowVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getPmValues().getSnowVolume().replace(cm, "").replace(mm,
							""));
				}
			}

			if (haveRain) {
				if (rainVolume > 0f) {
					forecastRemoteViews.setTextViewText(R.id.rainVolume, String.format(Locale.getDefault(), "%.2f", rainVolume));
				} else {
					forecastRemoteViews.setViewVisibility(R.id.rainVolumeLayout, View.INVISIBLE);
				}
			} else {
				forecastRemoteViews.setViewVisibility(R.id.rainVolumeLayout, View.GONE);
			}

			if (haveSnow) {
				if (snowVolume > 0f) {
					forecastRemoteViews.setTextViewText(R.id.snowVolume, String.format(Locale.getDefault(), "%.2f", snowVolume));
				} else {
					forecastRemoteViews.setViewVisibility(R.id.snowVolumeLayout, View.INVISIBLE);
				}
			} else {
				forecastRemoteViews.setViewVisibility(R.id.snowVolumeLayout, View.GONE);
			}

			forecastRemoteViews.setTextViewText(R.id.temperature, dailyForecastDtoList.get(cell).getMinTemp() + "/" +
					dailyForecastDtoList.get(cell).getMaxTemp());

			remoteViews.addView(R.id.dailyForecast, forecastRemoteViews);
		}

		remoteViews.setTextViewText(R.id.address, addressName);
		remoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));
	}

	@Override
	public void setResultViews(RemoteViews remoteViews, DailyPushNotificationDto dailyPushNotificationDto, Set<WeatherDataSourceType> weatherDataSourceTypeSet, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		final String refreshDateTime = multipleRestApiDownloader.getRequestDateTime().toString();
		WeatherDataSourceType weatherDataSourceType = WeatherResponseProcessor.getMainWeatherSourceType(weatherDataSourceTypeSet);
		List<DailyForecastDto> dailyForecastDtoList = WeatherResponseProcessor.getDailyForecastDtoList(context, multipleRestApiDownloader
				, weatherDataSourceType);
		boolean successful = !dailyForecastDtoList.isEmpty();

		if (successful) {
			setDataViews(remoteViews, dailyPushNotificationDto.getAddressName(), refreshDateTime, dailyForecastDtoList);
			makeNotification(remoteViews, dailyPushNotificationDto.getId());
		} else {
			makeFailedNotification(dailyPushNotificationDto.getId(), context.getString(R.string.msg_failed_update));

		}

	}

	@Override
	public Set<RequestWeatherDataType> getRequestWeatherDataTypeSet() {
		Set<RequestWeatherDataType> set = new HashSet<>();
		set.add(RequestWeatherDataType.dailyForecast);
		return set;
	}
}
