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
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ThirdDailyNotificationViewCreator extends AbstractDailyNotiViewCreator {
	private final int cellCount = 6;

	public ThirdDailyNotificationViewCreator(Context context) {
		super(context);
	}

	public void setDataViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, ArrayMap<WeatherSourceType,
			List<DailyForecastDto>> dailyForecastDtoListMap) {
		drawViews(remoteViews, addressName, lastRefreshDateTime, dailyForecastDtoListMap);
	}

	@Override
	public void setTempDataViews(RemoteViews remoteViews) {
		ArrayMap<WeatherSourceType, List<DailyForecastDto>> dailyForecastDtoListMap = new ArrayMap<>();
		dailyForecastDtoListMap.put(WeatherSourceType.ACCU_WEATHER, WeatherResponseProcessor.getTempDailyForecastDtoList(context,
				cellCount));
		dailyForecastDtoListMap.put(WeatherSourceType.OPEN_WEATHER_MAP, WeatherResponseProcessor.getTempDailyForecastDtoList(context,
				cellCount));

		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(),
				dailyForecastDtoListMap);
	}

	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime,
	                       ArrayMap<WeatherSourceType, List<DailyForecastDto>> dailyForecastDtoListMap) {
		RelativeLayout rootLayout = new RelativeLayout(context);
		LayoutInflater layoutInflater = LayoutInflater.from(context);

		View headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime);
		headerView.setId(R.id.header);


		//첫번째로 일치하는 날짜를 찾는다. 첫 날짜가 kma가 6/2, owm이 6/3 이면 6/3을 첫 시작으로 하여 화면을 표시
		Set<WeatherSourceType> weatherSourceTypeSet = dailyForecastDtoListMap.keySet();
		ZonedDateTime firstDate = null;

		for (List<DailyForecastDto> dailyForecastDtoList : dailyForecastDtoListMap.values()) {
			if (firstDate == null) {
				firstDate = ZonedDateTime.of(dailyForecastDtoList.get(0).getDate().toLocalDateTime(),
						dailyForecastDtoList.get(0).getDate().getZone());
			} else if (firstDate.isBefore(dailyForecastDtoList.get(0).getDate())) {
				firstDate = ZonedDateTime.of(dailyForecastDtoList.get(0).getDate().toLocalDateTime(),
						dailyForecastDtoList.get(0).getDate().getZone());
			}
		}

		final long firstDays = TimeUnit.SECONDS.toDays(firstDate.toEpochSecond());
		long days = 0;
		Map<WeatherSourceType, Integer> firstBeginIdxMap = new HashMap<>();
		for (WeatherSourceType weatherSourceType : dailyForecastDtoListMap.keySet()) {
			days = TimeUnit.SECONDS.toDays(dailyForecastDtoListMap.get(weatherSourceType).get(0).getDate().toEpochSecond());
			firstBeginIdxMap.put(weatherSourceType, (int) (firstDays - days));
		}

		//날짜를 먼저 표시
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M.d\nE");

		String date = null;
		LinearLayout datesRow = new LinearLayout(context);
		datesRow.setId(R.id.datesRow);
		datesRow.setOrientation(LinearLayout.HORIZONTAL);

		LinearLayout.LayoutParams dateTextViewLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
		dateTextViewLayoutParams.weight = 1;

		final int textColor = Color.BLACK;

		for (int i = 0; i < cellCount; i++) {
			date = firstDate.format(dateTimeFormatter);
			firstDate = firstDate.plusDays(1);

			TextView textView = new TextView(context);
			textView.setText(date);
			textView.setTextColor(textColor);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
			textView.setGravity(Gravity.CENTER);
			textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

			datesRow.addView(textView, dateTextViewLayoutParams);
		}

		final LinearLayout forecastTable = new LinearLayout(context);
		forecastTable.setOrientation(LinearLayout.VERTICAL);

		final LinearLayout.LayoutParams forecastRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
		forecastRowLayoutParams.weight = 1;

		final String mm = "mm";
		final String cm = "cm";

		for (WeatherSourceType weatherSourceType : weatherSourceTypeSet) {
			List<DailyForecastDto> dailyForecastDtoList = dailyForecastDtoListMap.get(weatherSourceType);

			LinearLayout row = new LinearLayout(context);
			row.setOrientation(LinearLayout.VERTICAL);
			row.setLayoutParams(forecastRowLayoutParams);

			LinearLayout dailyForecastListView = new LinearLayout(context);
			dailyForecastListView.setOrientation(LinearLayout.HORIZONTAL);

			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.gravity = Gravity.CENTER;
			layoutParams.weight = 1;

			int count = cellCount + firstBeginIdxMap.get(weatherSourceType);
			String pop = null;

			for (int cell = firstBeginIdxMap.get(weatherSourceType); cell < count; cell++) {
				View view = layoutInflater.inflate(R.layout.view_forecast_item_in_linear, null, false);
				view.findViewById(R.id.dateTime).setVisibility(View.GONE);

				if (dailyForecastDtoList.get(cell).isSingle()) {
					((ImageView) view.findViewById(R.id.leftIcon)).setImageResource(dailyForecastDtoList.get(cell).getSingleValues().getWeatherIcon());
					view.findViewById(R.id.rightIcon).setVisibility(View.GONE);

					pop = dailyForecastDtoList.get(cell).getSingleValues().getPop();
				} else {
					((ImageView) view.findViewById(R.id.leftIcon)).setImageResource(dailyForecastDtoList.get(cell).getAmValues().getWeatherIcon());
					((ImageView) view.findViewById(R.id.rightIcon)).setImageResource(dailyForecastDtoList.get(cell).getPmValues().getWeatherIcon());

					pop = dailyForecastDtoList.get(cell).getAmValues().getPop() + "/" +
							dailyForecastDtoList.get(cell).getPmValues().getPop();
				}

				((TextView) view.findViewById(R.id.pop)).setText(pop);
				((TextView) view.findViewById(R.id.temperature)).setText(new String(dailyForecastDtoList.get(cell).getMinTemp() + "/" +
						dailyForecastDtoList.get(cell).getMaxTemp()));

				view.findViewById(R.id.rainVolumeLayout).setVisibility(View.GONE);
				view.findViewById(R.id.snowVolumeLayout).setVisibility(View.GONE);

				dailyForecastListView.addView(view, layoutParams);
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
			row.addView(dailyForecastListView, listViewLayoutParams);

			forecastTable.addView(row);
		}

		RelativeLayout.LayoutParams headerViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams datesRowLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams forecastViewsLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		headerViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		datesRowLayoutParams.addRule(RelativeLayout.BELOW, R.id.header);
		forecastViewsLayoutParams.addRule(RelativeLayout.BELOW, R.id.datesRow);
		forecastViewsLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		rootLayout.addView(headerView, headerViewLayoutParams);
		rootLayout.addView(datesRow, datesRowLayoutParams);
		rootLayout.addView(forecastTable, forecastViewsLayoutParams);

		drawBitmap(rootLayout, remoteViews);
	}

	@Override
	public void setResultViews(RemoteViews remoteViews, DailyPushNotificationDto dailyPushNotificationDto, Set<WeatherSourceType> weatherSourceTypeSet, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		final String refreshDateTime = multipleRestApiDownloader.getRequestDateTime().toString();

		ArrayMap<WeatherSourceType, List<DailyForecastDto>> weatherSourceTypeListArrayMap = new ArrayMap<>();
		boolean successful = true;

		for (WeatherSourceType weatherSourceType : weatherSourceTypeSet) {
			weatherSourceTypeListArrayMap.put(weatherSourceType, WeatherResponseProcessor.getDailyForecastDtoList(context, multipleRestApiDownloader,
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
		set.add(RequestWeatherDataType.dailyForecast);
		return set;
	}
}
