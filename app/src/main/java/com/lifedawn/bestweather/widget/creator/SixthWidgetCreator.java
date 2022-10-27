package com.lifedawn.bestweather.widget.creator;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherRequestUtil;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherUtil;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;
import com.lifedawn.bestweather.widget.widgetprovider.SixthWidgetProvider;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SixthWidgetCreator extends AbstractWidgetCreator {

	public SixthWidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
		super(context, widgetUpdateCallback, appWidgetId);
	}

	@Override
	public RemoteViews createTempViews(Integer parentWidth, Integer parentHeight) {
		RemoteViews remoteViews = createBaseRemoteViews();

		RemoteViewsUtil.onSuccessfulProcess(remoteViews);

		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(),
				WeatherResponseProcessor.getTempCurrentConditionsDto(context),
				WeatherResponseProcessor.getTempAirQualityDto(), null, parentWidth, parentHeight);
		return remoteViews;
	}


	@Override
	public Class<?> widgetProviderClass() {
		return SixthWidgetProvider.class;
	}

	@Override
	public Set<WeatherDataType> getRequestWeatherDataTypeSet() {
		Set<WeatherDataType> set = new HashSet<>();
		set.add(WeatherDataType.currentConditions);
		set.add(WeatherDataType.airQuality);

		return set;
	}


	public void setDataViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, CurrentConditionsDto currentConditionsDto,
	                         AirQualityDto airQualityDto, OnDrawBitmapCallback onDrawBitmapCallback) {
		drawViews(remoteViews, addressName, lastRefreshDateTime, currentConditionsDto, airQualityDto, onDrawBitmapCallback, null, null);
	}


	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime,
	                       CurrentConditionsDto currentConditionsDto, AirQualityDto airQualityDto, @Nullable OnDrawBitmapCallback onDrawBitmapCallback, @Nullable Integer parentWidth,
	                       @Nullable Integer parentHeight) {
		RelativeLayout rootLayout = new RelativeLayout(context);
		final LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime);
		headerView.setId(R.id.header);

		ViewGroup sixWidgetView = (ViewGroup) layoutInflater.inflate(R.layout.view_sixth_widget, null, false);

		String stationName = context.getString(R.string.measuring_station_name) + ": " + airQualityDto.getCityName();
		((TextView) sixWidgetView.findViewById(R.id.measuring_station_name)).setText(stationName);


		((TextView) sixWidgetView.findViewById(R.id.temperature)).setText(currentConditionsDto.getTemp());
		((ImageView) sixWidgetView.findViewById(R.id.weatherIcon)).setImageResource(currentConditionsDto.getWeatherIcon());

		if (currentConditionsDto.getYesterdayTemp() != null) {
			String yesterdayCompText = WeatherUtil.makeTempCompareToYesterdayText(currentConditionsDto.getTemp(),
					currentConditionsDto.getYesterdayTemp(), tempUnit, context);
			((TextView) sixWidgetView.findViewById(R.id.yesterdayTemperature)).setText(yesterdayCompText);
		} else {
			sixWidgetView.findViewById(R.id.yesterdayTemperature).setVisibility(View.GONE);
		}

		String feelsLikeTemp = context.getString(R.string.feelsLike) + ": " + currentConditionsDto.getFeelsLikeTemp();
		((TextView) sixWidgetView.findViewById(R.id.feelsLikeTemp)).setText(feelsLikeTemp);

		String precipitation = "";
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto.getPrecipitationVolume();
		} else {
			precipitation = context.getString(R.string.not_precipitation);
		}

		((TextView) sixWidgetView.findViewById(R.id.precipitation)).setText(precipitation);
		String simpleAirQuality = context.getString(R.string.air_quality) + ": " + AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi());

		String airQuality = context.getString(R.string.air_quality) + ": ";
		if (airQualityDto.isSuccessful()) {
			airQuality += AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi());
		} else {
			airQuality += context.getString(R.string.noData);
		}

		((TextView) sixWidgetView.findViewById(R.id.airQuality)).setText(airQuality);

		GridLayout airQualityGridLayout = sixWidgetView.findViewById(R.id.airQualityGrid);

		if (airQualityDto.isSuccessful()) {

			//pm10, pm2.5, o3, co, so2, no2 순서로
			final String[] particleNames = {context.getString(R.string.pm10_str), context.getString(R.string.pm25_str),
					context.getString(R.string.o3_str), context.getString(R.string.co_str), context.getString(R.string.so2_str),
					context.getString(R.string.no2_str)};
			final int[] iconIds = {R.drawable.pm10, R.drawable.pm25, R.drawable.o3, R.drawable.co, R.drawable.so2, R.drawable.no2};

			List<String> gradeValueList = new ArrayList<>();
			List<String> gradeDescriptionList = new ArrayList<>();

			gradeValueList.add(airQualityDto.getCurrent().getPm10().toString());
			gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getPm10()));

			gradeValueList.add(airQualityDto.getCurrent().getPm25().toString());
			gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getPm25()));

			gradeValueList.add(airQualityDto.getCurrent().getO3().toString());
			gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getO3()));

			gradeValueList.add(airQualityDto.getCurrent().getCo().toString());
			gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getCo()));

			gradeValueList.add(airQualityDto.getCurrent().getSo2().toString());
			gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getSo2()));

			gradeValueList.add(airQualityDto.getCurrent().getNo2().toString());
			gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getNo2()));

			for (int i = 0; i < 6; i++) {
				addAirQualityGridItem(layoutInflater, airQualityGridLayout, particleNames[i], gradeValueList.get(i), gradeDescriptionList.get(i),
						iconIds[i]);
			}
		} else {
			airQualityGridLayout.setVisibility(View.GONE);
		}

		RelativeLayout.LayoutParams headerViewLayoutParams = getHeaderViewLayoutParams();
		RelativeLayout.LayoutParams sixWidgetViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		headerViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		sixWidgetViewLayoutParams.addRule(RelativeLayout.BELOW, R.id.header);
		sixWidgetViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		rootLayout.addView(headerView, headerViewLayoutParams);
		rootLayout.addView(sixWidgetView, sixWidgetViewLayoutParams);

		drawBitmap(rootLayout, onDrawBitmapCallback, remoteViews, parentWidth, parentHeight);
	}

	private void addAirQualityGridItem(LayoutInflater layoutInflater, GridLayout gridLayout, String label, String gradeValue,
	                                   String gradeDescription, int iconId) {
		LinearLayout view = (LinearLayout) layoutInflater.inflate(R.layout.air_quality_item, null);
		((ImageView) view.findViewById(R.id.label_icon)).setImageResource(iconId);

		TextView labelTextView = view.findViewById(R.id.label);
		TextView gradeValueTextView = view.findViewById(R.id.value_int);
		TextView gradeDescriptionTextView = view.findViewById(R.id.value_str);

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
		WeatherProviderType weatherProviderType = WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherProviderTypeSet());

		if (widgetDto.isTopPriorityKma() && widgetDto.getCountryCode().equals("KR")) {
			weatherProviderType = WeatherProviderType.KMA_WEB;
		}
		WeatherRequestUtil.initWeatherSourceUniqueValues(weatherProviderType, true, context);

		zoneId = ZoneId.of(widgetDto.getTimeZoneId());

		RemoteViews remoteViews = createRemoteViews();

		JsonObject jsonObject = (JsonObject) JsonParser.parseString(widgetDto.getResponseText());

		CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.parseTextToCurrentConditionsDto(context, jsonObject,
				weatherProviderType, widgetDto.getLatitude(), widgetDto.getLongitude(), zoneId);
		AirQualityDto airQualityDto = AqicnResponseProcessor.parseTextToAirQualityDto(jsonObject);

		setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), currentConditionsDto,
				airQualityDto, null);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		RemoteViewsUtil.onSuccessfulProcess(remoteViews);

		appWidgetManager.updateAppWidget(appWidgetId,
				remoteViews);
	}

	@Override
	public void setResultViews(int appWidgetId, @Nullable @org.jetbrains.annotations.Nullable WeatherRestApiDownloader weatherRestApiDownloader, ZoneId zoneId) {

		this.zoneId = zoneId;
		final CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.getCurrentConditionsDto(context, weatherRestApiDownloader,
				WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherProviderTypeSet()), zoneId);
		final AirQualityDto airQualityDto = WeatherResponseProcessor.getAirQualityDto(weatherRestApiDownloader, null);
		final boolean successful = currentConditionsDto != null && airQualityDto.isSuccessful();

		if (successful) {
			ZoneOffset zoneOffset = currentConditionsDto.getCurrentTime().getOffset();
			widgetDto.setTimeZoneId(zoneId.getId());
			widgetDto.setLastRefreshDateTime(weatherRestApiDownloader.getRequestDateTime().toString());

			makeResponseTextToJson(weatherRestApiDownloader, getRequestWeatherDataTypeSet(), widgetDto.getWeatherProviderTypeSet(), widgetDto, zoneOffset);
		}

		widgetDto.setLoadSuccessful(successful);

		super.setResultViews(appWidgetId, weatherRestApiDownloader, zoneId);
	}
}
