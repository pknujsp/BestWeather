package com.lifedawn.bestweather.notification.daily.viewcreator;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.forremoteviews.RemoteViewProcessor;
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

	public void setTempDataViews(RemoteViews remoteViews) {
		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(), WeatherResponseProcessor.getTempAirQualityDto());
	}

	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime,
	                       AirQualityDto airQualityDto) {
		RelativeLayout rootLayout = new RelativeLayout(context);
		LayoutInflater layoutInflater = LayoutInflater.from(context);

		View headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime);
		headerView.setId(R.id.header);

		ViewGroup seventhView = (ViewGroup) layoutInflater.inflate(R.layout.view_seventh_widget, null, false);

		String stationName = context.getString(R.string.measuring_station_name) + ": " + airQualityDto.getCityName();
		((TextView) seventhView.findViewById(R.id.measuring_station_name)).setText(stationName);

		String airQuality = context.getString(R.string.air_quality) + ": " + AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi());
		((TextView) seventhView.findViewById(R.id.airQuality)).setText(airQuality);

		LinearLayout forecastLayout = seventhView.findViewById(R.id.airQualityForecast);
		final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, context.getResources().getDisplayMetrics());
		final String noData = context.getString(R.string.noData);

		LinearLayout.LayoutParams forecastItemLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		forecastItemLayoutParams.bottomMargin = margin;

		View forecastItemView = layoutInflater.inflate(R.layout.air_quality_simple_forecast_item, null);
		forecastItemView.setPadding(0, 0, 0, 0);

		TextView dateTextView = forecastItemView.findViewById(R.id.date);
		TextView pm10TextView = forecastItemView.findViewById(R.id.pm10);
		TextView pm25TextView = forecastItemView.findViewById(R.id.pm25);
		TextView o3TextView = forecastItemView.findViewById(R.id.o3);

		dateTextView.setVisibility(View.INVISIBLE);
		pm10TextView.setText(context.getString(R.string.pm10_str));
		pm25TextView.setText(context.getString(R.string.pm25_str));
		o3TextView.setText(context.getString(R.string.o3_str));

		forecastLayout.addView(forecastItemView, forecastItemLayoutParams);

		AirQualityDto.DailyForecast current = new AirQualityDto.DailyForecast();
		current.setDate(null).setPm10(new AirQualityDto.DailyForecast.Val().setAvg(airQualityDto.getCurrent().getPm10()))
				.setPm25(new AirQualityDto.DailyForecast.Val().setAvg(airQualityDto.getCurrent().getPm25()))
				.setO3(new AirQualityDto.DailyForecast.Val().setAvg(airQualityDto.getCurrent().getO3()));

		List<AirQualityDto.DailyForecast> dailyForecastList = new ArrayList<>();
		dailyForecastList.add(current);
		dailyForecastList.addAll(airQualityDto.getDailyForecastList());

		DateTimeFormatter forecastDateFormatter = DateTimeFormatter.ofPattern("M.d E");

		for (AirQualityDto.DailyForecast item : dailyForecastList) {
			forecastItemView = layoutInflater.inflate(R.layout.air_quality_simple_forecast_item, null);
			forecastItemView.setPadding(0, 0, 0, 0);

			dateTextView = forecastItemView.findViewById(R.id.date);
			pm10TextView = forecastItemView.findViewById(R.id.pm10);
			pm25TextView = forecastItemView.findViewById(R.id.pm25);
			o3TextView = forecastItemView.findViewById(R.id.o3);

			dateTextView.setText(item.getDate() == null ? context.getString(R.string.current) : item.getDate().format(forecastDateFormatter));
			if (item.isHasPm10()) {
				pm10TextView.setText(AqicnResponseProcessor.getGradeDescription(item.getPm10().getAvg()));
				pm10TextView.setTextColor(AqicnResponseProcessor.getGradeColorId(item.getPm10().getAvg()));
			} else {
				pm10TextView.setText(noData);
			}
			if (item.isHasPm25()) {
				pm25TextView.setText(AqicnResponseProcessor.getGradeDescription(item.getPm25().getAvg()));
				pm25TextView.setTextColor(AqicnResponseProcessor.getGradeColorId(item.getPm25().getAvg()));
			} else {
				pm25TextView.setText(noData);
			}
			if (item.isHasO3()) {
				o3TextView.setText(AqicnResponseProcessor.getGradeDescription(item.getO3().getAvg()));
				o3TextView.setTextColor(AqicnResponseProcessor.getGradeColorId(item.getO3().getAvg()));
			} else {
				o3TextView.setText(noData);
			}

			forecastLayout.addView(forecastItemView, forecastItemLayoutParams);
		}

		RelativeLayout.LayoutParams headerViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams seventhWidgetViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		headerViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		seventhWidgetViewLayoutParams.addRule(RelativeLayout.BELOW, R.id.header);
		seventhWidgetViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		rootLayout.addView(headerView, headerViewLayoutParams);
		rootLayout.addView(seventhView, seventhWidgetViewLayoutParams);

		drawBitmap(rootLayout, remoteViews);
	}


	@Override
	public void setResultViews(RemoteViews remoteViews, DailyPushNotificationDto dailyPushNotificationDto, Set<WeatherSourceType> weatherSourceTypeSet, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
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
