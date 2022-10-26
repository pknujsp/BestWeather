package com.lifedawn.bestweather.widget.creator;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.gridlayout.widget.GridLayout;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.forremoteviews.RemoteViewsUtil;
import com.lifedawn.bestweather.retrofit.util.WeatherRestApiDownloader;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;
import com.lifedawn.bestweather.widget.widgetprovider.SeventhWidgetProvider;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeventhWidgetCreator extends AbstractWidgetCreator {
	private final DateTimeFormatter forecastDateFormatter = DateTimeFormatter.ofPattern("E");

	public SeventhWidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
		super(context, widgetUpdateCallback, appWidgetId);
	}

	@Override
	public WidgetDto loadDefaultSettings() {
		widgetDto = super.loadDefaultSettings();
		widgetDto.getWeatherProviderTypeSet().clear();
		widgetDto.getWeatherProviderTypeSet().add(WeatherProviderType.AQICN);
		return widgetDto;
	}

	@Override
	public Set<WeatherDataType> getRequestWeatherDataTypeSet() {
		Set<WeatherDataType> set = new HashSet<>();
		set.add(WeatherDataType.airQuality);

		return set;
	}

	@Override
	public RemoteViews createTempViews(Integer parentWidth, Integer parentHeight) {
		RemoteViews remoteViews = createBaseRemoteViews();

		RemoteViewsUtil.onSuccessfulProcess(remoteViews);

		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(), WeatherResponseProcessor.getTempAirQualityDto(),
				null, parentWidth, parentHeight);
		return remoteViews;
	}



	@Override
	public Class<?> widgetProviderClass() {
		return SeventhWidgetProvider.class;
	}


	public void setDataViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime,
	                         AirQualityDto airQualityDto, OnDrawBitmapCallback onDrawBitmapCallback) {
		drawViews(remoteViews, addressName, lastRefreshDateTime, airQualityDto, onDrawBitmapCallback, null, null);
	}


	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime,
	                       AirQualityDto airQualityDto, @Nullable OnDrawBitmapCallback onDrawBitmapCallback, @Nullable Integer parentWidth,
	                       @Nullable Integer parentHeight) {
		if (!airQualityDto.isSuccessful()) {
			RemoteViewsUtil.onErrorProcess(remoteViews, context, RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA);
			setRefreshPendingIntent(remoteViews);
			return;
		}

		RelativeLayout rootLayout = new RelativeLayout(context);
		LayoutInflater layoutInflater = LayoutInflater.from(context);

		View headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime);
		headerView.setId(R.id.header);

		ViewGroup seventhView = (ViewGroup) layoutInflater.inflate(R.layout.view_seventh_widget, null, false);

		String stationName = context.getString(R.string.measuring_station_name) + ": " + airQualityDto.getCityName();
		((TextView) seventhView.findViewById(R.id.measuring_station_name)).setText(stationName);


		String airQuality = context.getString(R.string.air_quality) + ": " + AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi());

		((TextView) seventhView.findViewById(R.id.airQuality)).setText(airQuality);


		GridLayout gridLayout = seventhView.findViewById(R.id.airQualityGrid);

		//co, so2, no2 순서로
		final String[] particleNames = {context.getString(R.string.co_str), context.getString(R.string.so2_str),
				context.getString(R.string.no2_str)};

		List<String> gradeValueList = new ArrayList<>();
		List<String> gradeDescriptionList = new ArrayList<>();

		gradeValueList.add(airQualityDto.getCurrent().getCo().toString());
		gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getCo()));

		gradeValueList.add(airQualityDto.getCurrent().getSo2().toString());
		gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getSo2()));

		gradeValueList.add(airQualityDto.getCurrent().getNo2().toString());
		gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getNo2()));

		for (int i = 0; i < 3; i++) {
			addAirQualityGridItem(layoutInflater, gridLayout, particleNames[i], gradeValueList.get(i), gradeDescriptionList.get(i));
		}

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

		dateTextView.setText(null);
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
			} else {
				pm10TextView.setText(noData);
			}
			if (item.isHasPm25()) {
				pm25TextView.setText(AqicnResponseProcessor.getGradeDescription(item.getPm25().getAvg()));
			} else {
				pm25TextView.setText(noData);
			}
			if (item.isHasO3()) {
				o3TextView.setText(AqicnResponseProcessor.getGradeDescription(item.getO3().getAvg()));
			} else {
				o3TextView.setText(noData);
			}

			forecastLayout.addView(forecastItemView, forecastItemLayoutParams);
		}

		RelativeLayout.LayoutParams headerViewLayoutParams = getHeaderViewLayoutParams();
		RelativeLayout.LayoutParams seventhWidgetViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		headerViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		seventhWidgetViewLayoutParams.addRule(RelativeLayout.BELOW, R.id.header);
		seventhWidgetViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		rootLayout.addView(headerView, headerViewLayoutParams);
		rootLayout.addView(seventhView, seventhWidgetViewLayoutParams);

		drawBitmap(rootLayout, onDrawBitmapCallback, remoteViews, parentWidth, parentHeight);
	}

	private void addAirQualityGridItem(LayoutInflater layoutInflater, GridLayout gridLayout, String label, String gradeValue,
	                                   String gradeDescription) {
		View view = layoutInflater.inflate(R.layout.view_simple_air_quality_item, null, false);

		TextView labelTextView = view.findViewById(R.id.label);
		TextView gradeValueTextView = view.findViewById(R.id.gradeValue);
		TextView gradeDescriptionTextView = view.findViewById(R.id.gradeDescription);

		labelTextView.setText(label);
		gradeValueTextView.setText(gradeValue);
		gradeDescriptionTextView.setText(gradeDescription);


		int cellCount = gridLayout.getChildCount();
		int row = cellCount / gridLayout.getColumnCount();
		int column = cellCount % gridLayout.getColumnCount();

		GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();

		layoutParams.columnSpec = GridLayout.spec(column, GridLayout.FILL, 1);
		layoutParams.rowSpec = GridLayout.spec(row, GridLayout.FILL, 1);

		gridLayout.addView(view, layoutParams);
	}

	@Override
	public void setDisplayClock(boolean displayClock) {
		widgetDto.setDisplayClock(displayClock);
	}

	@Override
	public void setDataViewsOfSavedData() {
		RemoteViews remoteViews = createRemoteViews();
		RemoteViewsUtil.onSuccessfulProcess(remoteViews);

		JsonObject jsonObject = (JsonObject) JsonParser.parseString(widgetDto.getResponseText());

		AirQualityDto airQualityDto = AqicnResponseProcessor.parseTextToAirQualityDto(jsonObject);
		AqicnResponseProcessor.init(context);

		zoneId = ZoneId.of(widgetDto.getTimeZoneId());

		setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(),
				airQualityDto, null);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	}

	@Override
	public void setResultViews(int appWidgetId, @Nullable @org.jetbrains.annotations.Nullable WeatherRestApiDownloader weatherRestApiDownloader, ZoneId zoneId) {
		this.zoneId = zoneId;
		final AirQualityDto airQualityDto = WeatherResponseProcessor.getAirQualityDto(weatherRestApiDownloader, null);
		final boolean successful = airQualityDto != null && airQualityDto.isSuccessful();

		if (successful) {
			ZoneOffset zoneOffset = ZoneOffset.of(airQualityDto.getTimeInfo().getTz());
			widgetDto.setTimeZoneId(zoneId.getId());
			widgetDto.setLastRefreshDateTime(weatherRestApiDownloader.getRequestDateTime().toString());

			makeResponseTextToJson(weatherRestApiDownloader, getRequestWeatherDataTypeSet(), widgetDto.getWeatherProviderTypeSet(), widgetDto, zoneOffset);
		}

		widgetDto.setLoadSuccessful(successful);
		super.setResultViews(appWidgetId, weatherRestApiDownloader, zoneId);
	}
}
