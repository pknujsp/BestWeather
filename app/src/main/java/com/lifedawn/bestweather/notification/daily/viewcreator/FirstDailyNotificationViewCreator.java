package com.lifedawn.bestweather.notification.daily.viewcreator;

import android.content.Context;
import android.graphics.Color;
import android.util.ArrayMap;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.WrapperListAdapter;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.forremoteviews.RemoteViewProcessor;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class FirstDailyNotificationViewCreator extends AbstractDailyNotiViewCreator {
	private final int cellCount = 9;

	public FirstDailyNotificationViewCreator(Context context) {
		super(context);
	}

	public void setDataViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime,
	                         List<HourlyForecastDto> hourlyForecastDtoList) {
		drawViews(remoteViews, addressName, lastRefreshDateTime, hourlyForecastDtoList);
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
				WeatherResponseProcessor.getTempHourlyForecastDtoList(context, cellCount));
	}

	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime,
	                       List<HourlyForecastDto> hourlyForecastDtoList) {
		RemoteViews valuesRemoveViews = new RemoteViews(context.getPackageName(), R.layout.first_daily_noti_view);

		DateTimeFormatter hours0Formatter = DateTimeFormatter.ofPattern("E H");
		DateTimeFormatter hoursFormatter = DateTimeFormatter.ofPattern("H");

		final String mm = "mm";
		final String cm = "cm";

		boolean haveRain = false;
		boolean haveSnow = false;

		for (int cell = 0; cell < cellCount; cell++) {
			if (hourlyForecastDtoList.get(cell).isHasRain()) {
				haveRain = true;
			}

			if (hourlyForecastDtoList.get(cell).isHasSnow()) {
				haveSnow = true;
			}
		}

		for (int cell = 0; cell < cellCount; cell++) {
			RemoteViews forecastRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_forecast_item_in_linear);

			forecastRemoteViews.setTextViewText(R.id.dateTime, hourlyForecastDtoList.get(cell).getHours().getHour() == 0 ?
					hourlyForecastDtoList.get(cell).getHours().format(hours0Formatter) :
					hourlyForecastDtoList.get(cell).getHours().format(hoursFormatter));
			forecastRemoteViews.setImageViewResource(R.id.leftIcon, hourlyForecastDtoList.get(cell).getWeatherIcon());
			forecastRemoteViews.setTextViewText(R.id.pop, hourlyForecastDtoList.get(cell).getPop());

			if (hourlyForecastDtoList.get(cell).isHasRain()) {
				forecastRemoteViews.setTextViewText(R.id.rainVolume,
						hourlyForecastDtoList.get(cell).getRainVolume().replace(mm, "").replace(cm, ""));
			} else {
				forecastRemoteViews.setViewVisibility(R.id.rainVolumeLayout, View.INVISIBLE);
			}

			if (hourlyForecastDtoList.get(cell).isHasSnow()) {
				forecastRemoteViews.setTextViewText(R.id.snowVolume,
						hourlyForecastDtoList.get(cell).getSnowVolume().replace(mm, "").replace(cm, ""));
			} else {
				forecastRemoteViews.setViewVisibility(R.id.snowVolumeLayout, View.INVISIBLE);
			}

			if (!haveRain) {
				forecastRemoteViews.setViewVisibility(R.id.rainVolumeLayout, View.GONE);
			}
			if (!haveSnow) {
				forecastRemoteViews.setViewVisibility(R.id.snowVolumeLayout, View.GONE);
			}

			forecastRemoteViews.setTextViewText(R.id.temperature, hourlyForecastDtoList.get(cell).getTemp());
			forecastRemoteViews.setViewVisibility(R.id.rightIcon, View.GONE);

			valuesRemoveViews.addView(R.id.hourlyForecast, forecastRemoteViews);
		}

		valuesRemoveViews.setTextViewText(R.id.address, addressName);
		valuesRemoveViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));

		remoteViews.addView(R.id.valuesView, valuesRemoveViews);
	}

	@Override
	public void setResultViews(RemoteViews remoteViews, DailyPushNotificationDto dailyPushNotificationDto, Set<WeatherSourceType> weatherSourceTypeSet, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		final String refreshDateTime = multipleRestApiDownloader.getRequestDateTime().toString();

		WeatherSourceType weatherSourceType = WeatherResponseProcessor.getMainWeatherSourceType(weatherSourceTypeSet);
		List<HourlyForecastDto> hourlyForecastDtoList = WeatherResponseProcessor.getHourlyForecastDtoList(context,
				multipleRestApiDownloader, weatherSourceType);
		boolean successful = !hourlyForecastDtoList.isEmpty();

		if (successful) {
			setDataViews(remoteViews, dailyPushNotificationDto.getAddressName(), refreshDateTime, hourlyForecastDtoList);
			RemoteViewProcessor.onSuccessfulProcess(remoteViews);
		} else {
			RemoteViewProcessor.onErrorProcess(remoteViews, context, RemoteViewProcessor.ErrorType.FAILED_LOAD_WEATHER_DATA);
		}
		makeNotification(remoteViews, dailyPushNotificationDto.getId());
	}

	@Override
	public Set<RequestWeatherDataType> getRequestWeatherDataTypeSet() {
		Set<RequestWeatherDataType> set = new HashSet<>();
		set.add(RequestWeatherDataType.hourlyForecast);
		return set;
	}
}
