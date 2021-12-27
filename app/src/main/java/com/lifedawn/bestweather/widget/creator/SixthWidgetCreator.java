package com.lifedawn.bestweather.widget.creator;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.gridlayout.widget.GridLayout;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SixthWidgetCreator extends AbstractWidgetCreator {
	private final DateTimeFormatter refreshDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a h:mm");

	private int addressTextSize;
	private int refreshDateTimeTextSize;
	private int precipitationTextSize;
	private int stationNameTextSize;
	private int tempTextSize;
	private int simpleAirQualityTextSize;
	private int particleNameTextSize;
	private int gradeValueTextSize;
	private int gradeDescriptionTextSize;

	public SixthWidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
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
			remoteViews.setOnClickPendingIntent(R.id.root_layout, getOnClickedPendingIntent(remoteViews));
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
		precipitationTextSize = context.getResources().getDimensionPixelSize(R.dimen.precipitationtTextSizeInSixthWidget) + extraSize;
		tempTextSize = context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInSixthWidget) + extraSize;
		simpleAirQualityTextSize = context.getResources().getDimensionPixelSize(R.dimen.airQualityTextSizeInSixthWidget) + extraSize;
		particleNameTextSize = context.getResources().getDimensionPixelSize(R.dimen.labelTextSizeInAirQualityItem) + extraSize;
		gradeValueTextSize = context.getResources().getDimensionPixelSize(R.dimen.gradeValueTextSizeInAirQualityItem) + extraSize;
		gradeDescriptionTextSize = context.getResources().getDimensionPixelSize(R.dimen.gradeDescriptionTextSizeInAirQualityItem) + extraSize;
		stationNameTextSize = context.getResources().getDimensionPixelSize(R.dimen.stationNameTextSizeInSixthWidget) + extraSize;
	}


	public View makeHeaderViews(LayoutInflater layoutInflater, String addressName, String lastRefreshDateTime) {
		View view = layoutInflater.inflate(R.layout.header_view_in_widget, null, false);
		((TextView) view.findViewById(R.id.address)).setText(addressName);
		((TextView) view.findViewById(R.id.refresh)).setText(ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));

		((TextView) view.findViewById(R.id.address)).setTextSize(TypedValue.COMPLEX_UNIT_PX, addressTextSize);
		((TextView) view.findViewById(R.id.refresh)).setTextSize(TypedValue.COMPLEX_UNIT_PX, refreshDateTimeTextSize);

		return view;
	}

	public void setDataViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, CurrentConditionsDto currentConditionsDto,
	                         AirQualityDto airQualityDto, OnDrawBitmapCallback onDrawBitmapCallback) {
		drawViews(remoteViews, addressName, lastRefreshDateTime, currentConditionsDto, airQualityDto, onDrawBitmapCallback);
	}

	public void setTempDataViews(RemoteViews remoteViews) {
		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(),
				WeatherResponseProcessor.getTempCurrentConditionsDto(context),
				WeatherResponseProcessor.getTempAirQualityDto(), null);
	}

	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime,
	                       CurrentConditionsDto currentConditionsDto, AirQualityDto airQualityDto, @Nullable OnDrawBitmapCallback onDrawBitmapCallback) {
		RelativeLayout rootLayout = new RelativeLayout(context);
		final LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime);
		headerView.setId(R.id.header);

		ViewGroup sixWidgetView = (ViewGroup) layoutInflater.inflate(R.layout.view_sixth_widget, null, false);

		String stationName = context.getString(R.string.measuring_station_name) + ": " + airQualityDto.getCityName();
		((TextView) sixWidgetView.findViewById(R.id.measuring_station_name)).setText(stationName);
		((TextView) sixWidgetView.findViewById(R.id.measuring_station_name)).setTextSize(TypedValue.COMPLEX_UNIT_PX, stationNameTextSize);

		((TextView) sixWidgetView.findViewById(R.id.temperature)).setTextSize(TypedValue.COMPLEX_UNIT_PX, tempTextSize);
		((TextView) sixWidgetView.findViewById(R.id.precipitation)).setTextSize(TypedValue.COMPLEX_UNIT_PX, precipitationTextSize);
		((TextView) sixWidgetView.findViewById(R.id.airQuality)).setTextSize(TypedValue.COMPLEX_UNIT_PX, simpleAirQualityTextSize);

		((TextView) sixWidgetView.findViewById(R.id.temperature)).setText(currentConditionsDto.getTemp());

		String precipitation = "";
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto.getPrecipitationVolume();
		} else {
			precipitation = context.getString(R.string.not_precipitation);
		}

		((TextView) sixWidgetView.findViewById(R.id.precipitation)).setText(precipitation);
		String simpleAirQuality = context.getString(R.string.air_quality) + ": " + airQualityDto.getAqi() + ", " +
				AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi());
		((TextView) sixWidgetView.findViewById(R.id.airQuality)).setText(simpleAirQuality);

		GridLayout airQualityGridLayout = sixWidgetView.findViewById(R.id.airQualityGrid);

		//pm10, pm2.5, o3, co, so2, no2 순서로
		final String[] particleNames = {context.getString(R.string.pm10_str), context.getString(R.string.pm25_str),
				context.getString(R.string.o3_str), context.getString(R.string.co_str), context.getString(R.string.so2_str),
				context.getString(R.string.no2_str)};
		final int[] iconIds = {R.drawable.pm10, R.drawable.pm25, R.drawable.o3, R.drawable.co, R.drawable.so2, R.drawable.no2};

		List<String> gradeValueList = new ArrayList<>();
		List<String> gradeDescriptionList = new ArrayList<>();
		List<Integer> gradeTextColorList = new ArrayList<>();

		gradeValueList.add(airQualityDto.getCurrent().getPm10().toString());
		gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getPm10()));
		gradeTextColorList.add(AqicnResponseProcessor.getGradeColorId(airQualityDto.getCurrent().getPm10()));

		gradeValueList.add(airQualityDto.getCurrent().getPm25().toString());
		gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getPm25()));
		gradeTextColorList.add(AqicnResponseProcessor.getGradeColorId(airQualityDto.getCurrent().getPm25()));

		gradeValueList.add(airQualityDto.getCurrent().getO3().toString());
		gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getO3()));
		gradeTextColorList.add(AqicnResponseProcessor.getGradeColorId(airQualityDto.getCurrent().getO3()));

		gradeValueList.add(airQualityDto.getCurrent().getCo().toString());
		gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getCo()));
		gradeTextColorList.add(AqicnResponseProcessor.getGradeColorId(airQualityDto.getCurrent().getCo()));

		gradeValueList.add(airQualityDto.getCurrent().getSo2().toString());
		gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getSo2()));
		gradeTextColorList.add(AqicnResponseProcessor.getGradeColorId(airQualityDto.getCurrent().getSo2()));

		gradeValueList.add(airQualityDto.getCurrent().getNo2().toString());
		gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getNo2()));
		gradeTextColorList.add(AqicnResponseProcessor.getGradeColorId(airQualityDto.getCurrent().getNo2()));

		for (int i = 0; i < 6; i++) {
			addAirQualityGridItem(layoutInflater, airQualityGridLayout, particleNames[i], gradeValueList.get(i), gradeDescriptionList.get(i),
					gradeTextColorList.get(i), iconIds[i]);
		}

		RelativeLayout.LayoutParams headerViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams sixWidgetViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		headerViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		sixWidgetViewLayoutParams.addRule(RelativeLayout.BELOW, R.id.header);
		sixWidgetViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		rootLayout.addView(headerView, headerViewLayoutParams);
		rootLayout.addView(sixWidgetView, sixWidgetViewLayoutParams);

		drawBitmap(rootLayout, onDrawBitmapCallback, remoteViews);
	}

	private void addAirQualityGridItem(LayoutInflater layoutInflater, GridLayout gridLayout, String label, String gradeValue,
	                                   String gradeDescription, int textColor, int iconId) {
		RelativeLayout view = (RelativeLayout) layoutInflater.inflate(R.layout.air_quality_item, null);
		((ImageView) view.findViewById(R.id.label_icon)).setImageResource(iconId);

		TextView labelTextView = view.findViewById(R.id.label);
		TextView gradeValueTextView = view.findViewById(R.id.value_int);
		TextView gradeDescriptionTextView = view.findViewById(R.id.value_str);

		labelTextView.setText(label);
		gradeValueTextView.setText(gradeValue);
		gradeDescriptionTextView.setText(gradeDescription);
		gradeDescriptionTextView.setTextColor(textColor);

		labelTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, particleNameTextSize);
		gradeValueTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, gradeValueTextSize);
		gradeDescriptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, gradeDescriptionTextSize);

		gridLayout.addView(view);
	}

	@Override
	public void setDisplayClock(boolean displayClock) {
		widgetDto.setDisplayClock(displayClock);
	}

	@Override
	public void setDataViewsOfSavedData() {
		WeatherSourceType weatherSourceType =  WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherSourceTypeSet());

		if (widgetDto.isTopPriorityKma() && widgetDto.getCountryCode().equals("KR")) {
			weatherSourceType = WeatherSourceType.KMA_WEB;
		}

		RemoteViews remoteViews = createRemoteViews(false);
		JsonObject jsonObject = (JsonObject) JsonParser.parseString(widgetDto.getResponseText());

		CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.parseTextToCurrentConditionsDto(context, jsonObject,
				weatherSourceType, widgetDto.getLatitude(), widgetDto.getLongitude());
		AirQualityDto airQualityDto = AqicnResponseProcessor.parseTextToAirQualityDto(context, jsonObject);

		setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), currentConditionsDto,
				airQualityDto, null);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(appWidgetId,
				remoteViews);
	}

}
