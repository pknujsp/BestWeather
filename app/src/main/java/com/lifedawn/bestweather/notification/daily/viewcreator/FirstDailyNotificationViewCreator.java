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

	public void setDataViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, ArrayMap<WeatherSourceType,
			List<HourlyForecastDto>> hourlyForecastDtoListMap) {
		drawViews(remoteViews, addressName, lastRefreshDateTime, hourlyForecastDtoListMap);
	}

	@Override
	public void setTempDataViews(RemoteViews remoteViews) {
		ArrayMap<WeatherSourceType, List<HourlyForecastDto>> hourlyForecastDtoListMap = new ArrayMap<>();
		hourlyForecastDtoListMap.put(WeatherSourceType.ACCU_WEATHER, WeatherResponseProcessor.getTempHourlyForecastDtoList(context,
				cellCount));
		hourlyForecastDtoListMap.put(WeatherSourceType.OPEN_WEATHER_MAP, WeatherResponseProcessor.getTempHourlyForecastDtoList(context,
				cellCount));

		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(),
				hourlyForecastDtoListMap);
	}

	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime,
	                       ArrayMap<WeatherSourceType, List<HourlyForecastDto>> hourlyForecastDtoListMap) {
		RelativeLayout rootLayout = new RelativeLayout(context);
		LayoutInflater layoutInflater = LayoutInflater.from(context);

		View headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime);
		headerView.setId(R.id.header);

		//첫번째로 일치하는 시각을 찾는다. 첫 시각이 kma가 14, owm이 15 이면 15를 첫 시작으로 하여 화면을 표시
		Set<WeatherSourceType> weatherSourceTypeSet = hourlyForecastDtoListMap.keySet();
		ZonedDateTime firstHour = null;

		for (List<HourlyForecastDto> hourlyForecastDtoList : hourlyForecastDtoListMap.values()) {
			if (firstHour == null) {
				firstHour = ZonedDateTime.of(hourlyForecastDtoList.get(0).getHours().toLocalDateTime(),
						hourlyForecastDtoList.get(0).getHours().getZone());
			} else if (firstHour.isBefore(hourlyForecastDtoList.get(0).getHours())) {
				firstHour = ZonedDateTime.of(hourlyForecastDtoList.get(0).getHours().toLocalDateTime(),
						hourlyForecastDtoList.get(0).getHours().getZone());
			}
		}

		final long firstHours = TimeUnit.SECONDS.toHours(firstHour.toEpochSecond());
		long hours = 0;
		Map<WeatherSourceType, Integer> firstBeginIdxMap = new HashMap<>();
		for (WeatherSourceType weatherSourceType : hourlyForecastDtoListMap.keySet()) {
			hours = TimeUnit.SECONDS.toHours(hourlyForecastDtoListMap.get(weatherSourceType).get(0).getHours().toEpochSecond());
			firstBeginIdxMap.put(weatherSourceType, (int) (firstHours - hours));
		}

		//날짜를 먼저 표시
		DateTimeFormatter hours0Formatter = DateTimeFormatter.ofPattern("E H");
		DateTimeFormatter hoursFormatter = DateTimeFormatter.ofPattern("H");

		String hour = null;
		LinearLayout hoursRow = new LinearLayout(context);
		hoursRow.setId(R.id.hoursRow);
		hoursRow.setOrientation(LinearLayout.HORIZONTAL);

		LinearLayout.LayoutParams dateTextViewLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
		dateTextViewLayoutParams.weight = 1;

		final int textColor = Color.BLACK;

		for (int i = 0; i < cellCount; i++) {
			hour = firstHour.getHour() == 0 ? firstHour.format(hours0Formatter) : firstHour.format(hoursFormatter);
			firstHour = firstHour.plusHours(1);

			TextView textView = new TextView(context);
			textView.setText(hour);
			textView.setTextColor(textColor);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
			textView.setGravity(Gravity.CENTER);
			textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

			hoursRow.addView(textView, dateTextViewLayoutParams);
		}

		final LinearLayout forecastTable = new LinearLayout(context);
		forecastTable.setOrientation(LinearLayout.VERTICAL);

		final LinearLayout.LayoutParams forecastRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
		forecastRowLayoutParams.weight = 1;

		for (WeatherSourceType weatherSourceType : weatherSourceTypeSet) {
			List<HourlyForecastDto> hourlyForecastDtoList = hourlyForecastDtoListMap.get(weatherSourceType);

			LinearLayout row = new LinearLayout(context);
			row.setOrientation(LinearLayout.VERTICAL);
			row.setLayoutParams(forecastRowLayoutParams);

			LinearLayout hourlyForecastListView = new LinearLayout(context);
			hourlyForecastListView.setOrientation(LinearLayout.HORIZONTAL);

			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.gravity = Gravity.CENTER;
			layoutParams.weight = 1;

			int count = cellCount + firstBeginIdxMap.get(weatherSourceType);
			String pop = null;

			for (int cell = firstBeginIdxMap.get(weatherSourceType); cell < count; cell++) {
				View view = layoutInflater.inflate(R.layout.view_forecast_item_in_linear, null, false);
				view.findViewById(R.id.dateTime).setVisibility(View.GONE);

				((ImageView) view.findViewById(R.id.leftIcon)).setImageResource(hourlyForecastDtoList.get(cell).getWeatherIcon());
				((TextView) view.findViewById(R.id.temperature)).setText(hourlyForecastDtoList.get(cell).getTemp());
				view.findViewById(R.id.rightIcon).setVisibility(View.GONE);

				view.findViewById(R.id.popLayout).setVisibility(View.GONE);
				view.findViewById(R.id.rainVolumeLayout).setVisibility(View.GONE);
				view.findViewById(R.id.snowVolumeLayout).setVisibility(View.GONE);

				hourlyForecastListView.addView(view, layoutParams);
			}

			String weatherSource = null;
			int icon = 0;
			if (weatherSourceType == WeatherSourceType.KMA_WEB) {
				weatherSource = context.getString(R.string.kma);
				icon = R.drawable.kmaicon;
			} else if (weatherSourceType == WeatherSourceType.ACCU_WEATHER) {
				weatherSource = context.getString(R.string.accu_weather);
				icon = R.drawable.accuicon;
			} else if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
				weatherSource = context.getString(R.string.owm);
				icon = R.drawable.owmicon;
			}

			View weatherSourceView = layoutInflater.inflate(R.layout.weather_data_source_view, null);
			((TextView) weatherSourceView.findViewById(R.id.source)).setText(weatherSource);
			((ImageView) weatherSourceView.findViewById(R.id.icon)).setImageResource(icon);

			LinearLayout.LayoutParams listViewLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
			listViewLayoutParams.weight = 1;

			row.addView(weatherSourceView);
			row.addView(hourlyForecastListView, listViewLayoutParams);

			forecastTable.addView(row);
		}

		RelativeLayout.LayoutParams headerViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams hoursRowLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams forecastViewsLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		headerViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		hoursRowLayoutParams.addRule(RelativeLayout.BELOW, R.id.header);
		forecastViewsLayoutParams.addRule(RelativeLayout.BELOW, R.id.datesRow);
		forecastViewsLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		rootLayout.addView(headerView, headerViewLayoutParams);
		rootLayout.addView(hoursRow, hoursRowLayoutParams);
		rootLayout.addView(forecastTable, forecastViewsLayoutParams);

		drawBitmap(rootLayout, remoteViews);
	}

	@Override
	public void setResultViews(RemoteViews remoteViews, DailyPushNotificationDto dailyPushNotificationDto, Set<WeatherSourceType> weatherSourceTypeSet, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		final String refreshDateTime = multipleRestApiDownloader.getRequestDateTime().toString();

		ArrayMap<WeatherSourceType, List<HourlyForecastDto>> weatherSourceTypeListArrayMap = new ArrayMap<>();
		boolean successful = true;

		for (WeatherSourceType weatherSourceType : weatherSourceTypeSet) {
			weatherSourceTypeListArrayMap.put(weatherSourceType, WeatherResponseProcessor.getHourlyForecastDtoList(context, multipleRestApiDownloader,
					weatherSourceType));

			if (weatherSourceTypeListArrayMap.get(weatherSourceType).isEmpty()) {
				successful = false;
			}
		}


		if (successful) {
			setDataViews(remoteViews, dailyPushNotificationDto.getAddressName(), refreshDateTime, weatherSourceTypeListArrayMap);
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
