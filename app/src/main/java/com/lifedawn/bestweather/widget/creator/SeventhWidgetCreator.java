package com.lifedawn.bestweather.widget.creator;

import android.appwidget.AppWidgetManager;
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
import androidx.gridlayout.widget.GridLayout;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SeventhWidgetCreator extends AbstractWidgetCreator {
	private final DateTimeFormatter refreshDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a h:mm");
	private final DateTimeFormatter forecastDateFormatter = DateTimeFormatter.ofPattern("E");

	private int addressTextSize;
	private int refreshDateTimeTextSize;
	private int stationNameTextSize;
	private int simpleAirQualityTextSize;
	private int currentParticleNameTextSize;
	private int currentGradeValueTextSize;
	private int currentGradeDescriptionTextSize;
	private int forecastParticleNameTextSize;
	private int forecastGradeDescriptionTextSize;
	private int forecastDateTextSize;

	public SeventhWidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
		super(context, widgetUpdateCallback, appWidgetId);
	}

	@Override
	public RemoteViews createRemoteViews(boolean needTempData) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int layoutId = appWidgetManager.getAppWidgetInfo(appWidgetId).initialLayout;
		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);

		if (needTempData) {
			setTempDataViews(remoteViews);
		} else {
			remoteViews.setOnClickPendingIntent(R.id.root_layout, getOnClickedPendingIntent());
		}

		//setBackgroundAlpha(remoteViews, widgetDto.getBackgroundAlpha());

		return remoteViews;
	}

	@Override
	public void setTextSize(int amount) {
		final int absSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, Math.abs(amount),
				context.getResources().getDisplayMetrics());
		final int extraSize = amount >= 0 ? absSize : absSize * -1;

		addressTextSize = context.getResources().getDimensionPixelSize(R.dimen.addressTextSizeInCommonWidgetHeader) + extraSize;
		refreshDateTimeTextSize = context.getResources().getDimensionPixelSize(R.dimen.refreshDateTimeTextSizeInCommonWidgetHeader) + extraSize;

		stationNameTextSize = context.getResources().getDimensionPixelSize(R.dimen.stationNameTextSizeInSeventhWidget) + extraSize;
		simpleAirQualityTextSize = context.getResources().getDimensionPixelSize(R.dimen.airQualityTextSizeInSeventhWidget) + extraSize;

		currentParticleNameTextSize = context.getResources().getDimensionPixelSize(R.dimen.labelTextSizeInSimpleAirQualityItem) + extraSize;
		currentGradeValueTextSize = context.getResources().getDimensionPixelSize(R.dimen.gradeValueTextSizeInSimpleAirQualityItem) + extraSize;
		currentGradeDescriptionTextSize = context.getResources().getDimensionPixelSize(R.dimen.gradeDescriptionTextSizeInAirQualityItem) + extraSize;

		forecastParticleNameTextSize = context.getResources().getDimensionPixelSize(R.dimen.particleNameTextSizeInLinearDailyAqiForecastItem) + extraSize;
		forecastGradeDescriptionTextSize = context.getResources().getDimensionPixelSize(R.dimen.particleNameTextSizeInLinearDailyAqiForecastItem) + extraSize;
		forecastDateTextSize = context.getResources().getDimensionPixelSize(R.dimen.dateTextSizeInLinearDailyAqiForecastItem) + extraSize;
	}


	public View makeHeaderViews(LayoutInflater layoutInflater, String addressName, String lastRefreshDateTime) {
		View view = layoutInflater.inflate(R.layout.header_view_in_widget, null, false);
		((TextView) view.findViewById(R.id.address)).setText(addressName);
		((TextView) view.findViewById(R.id.refresh)).setText(ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));

		((TextView) view.findViewById(R.id.address)).setTextSize(TypedValue.COMPLEX_UNIT_PX, addressTextSize);
		((TextView) view.findViewById(R.id.refresh)).setTextSize(TypedValue.COMPLEX_UNIT_PX, refreshDateTimeTextSize);

		return view;
	}

	public void setDataViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime,
	                         AirQualityDto airQualityDto, OnDrawBitmapCallback onDrawBitmapCallback) {
		drawViews(remoteViews, addressName, lastRefreshDateTime, airQualityDto, onDrawBitmapCallback);
	}

	public void setTempDataViews(RemoteViews remoteViews) {
		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(), WeatherResponseProcessor.getTempAirQualityDto(),
				null);
	}

	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime,
	                       AirQualityDto airQualityDto, @Nullable OnDrawBitmapCallback onDrawBitmapCallback) {
		RelativeLayout rootLayout = new RelativeLayout(context);
		LayoutInflater layoutInflater = LayoutInflater.from(context);

		View headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime);
		headerView.setId(R.id.header);

		ViewGroup seventhView = (ViewGroup) layoutInflater.inflate(R.layout.view_seventh_widget, null, false);

		String stationName = context.getString(R.string.measuring_station_name) + ": " + airQualityDto.getCityName();
		((TextView) seventhView.findViewById(R.id.measuring_station_name)).setText(stationName);
		((TextView) seventhView.findViewById(R.id.measuring_station_name)).setTextSize(TypedValue.COMPLEX_UNIT_PX, stationNameTextSize);

		String airQuality = context.getString(R.string.air_quality) + ": " + AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi());
		((TextView) seventhView.findViewById(R.id.airQuality)).setText(airQuality);

		((TextView) seventhView.findViewById(R.id.airQuality)).setTextSize(TypedValue.COMPLEX_UNIT_PX, simpleAirQualityTextSize);

		GridLayout gridLayout = seventhView.findViewById(R.id.airQualityGrid);

		//co, so2, no2 순서로
		final String[] particleNames = {context.getString(R.string.co_str), context.getString(R.string.so2_str),
				context.getString(R.string.no2_str)};
		final int[] iconIds = {R.drawable.co, R.drawable.so2, R.drawable.no2};

		List<String> gradeValueList = new ArrayList<>();
		List<String> gradeDescriptionList = new ArrayList<>();

		gradeValueList.add(airQualityDto.getCurrent().getCo().toString());
		gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getCo()));

		gradeValueList.add(airQualityDto.getCurrent().getSo2().toString());
		gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getSo2()));

		gradeValueList.add(airQualityDto.getCurrent().getNo2().toString());
		gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getNo2()));

		for (int i = 0; i < 3; i++) {
			addAirQualityGridItem(layoutInflater, gridLayout, particleNames[i], gradeValueList.get(i), gradeDescriptionList.get(i)
			);
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

		dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, forecastDateTextSize);
		pm10TextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, forecastParticleNameTextSize);
		pm25TextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, forecastParticleNameTextSize);
		o3TextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, forecastParticleNameTextSize);

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

			dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, forecastDateTextSize);
			pm10TextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, forecastParticleNameTextSize);
			pm25TextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, forecastParticleNameTextSize);
			o3TextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, forecastParticleNameTextSize);

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

		RelativeLayout.LayoutParams headerViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams seventhWidgetViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		headerViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		seventhWidgetViewLayoutParams.addRule(RelativeLayout.BELOW, R.id.header);
		seventhWidgetViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		rootLayout.addView(headerView, headerViewLayoutParams);
		rootLayout.addView(seventhView, seventhWidgetViewLayoutParams);

		drawBitmap(rootLayout, onDrawBitmapCallback, remoteViews);
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

		labelTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, currentParticleNameTextSize);
		gradeValueTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, currentGradeValueTextSize);
		gradeDescriptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, currentGradeDescriptionTextSize);

		gridLayout.addView(view);
	}

	@Override
	public void setDisplayClock(boolean displayClock) {
		widgetDto.setDisplayClock(displayClock);
	}

	@Override
	public void setDataViewsOfSavedData() {
		RemoteViews remoteViews = createRemoteViews(false);
		JsonObject jsonObject = (JsonObject) JsonParser.parseString(widgetDto.getResponseText());

		AirQualityDto airQualityDto = AqicnResponseProcessor.parseTextToAirQualityDto(context, jsonObject);

		setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(),
				airQualityDto, null);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(appWidgetId,
				remoteViews);
	}

}
