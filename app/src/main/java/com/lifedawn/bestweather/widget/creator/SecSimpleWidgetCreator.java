package com.lifedawn.bestweather.widget.creator;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import androidx.core.content.ContextCompat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;
import com.lifedawn.bestweather.weathers.view.DetailDoubleTemperatureView;
import com.lifedawn.bestweather.weathers.view.DetailDoubleTemperatureViewForRemoteViews;
import com.lifedawn.bestweather.weathers.view.DetailSingleTemperatureView;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.MeasureSpec.EXACTLY;

public class SecSimpleWidgetCreator extends AbstractWidgetCreator {
	private final DateTimeFormatter refreshDateTimeFormatter;

	private int addressTextSize;
	private int refreshDateTimeTextSize;
	private int dateTextSize;
	private int tempTextSize;
	private int popTextSize;
	private int currentPrecipitationTextSize;
	private int currentAirQualityTextSize;
	private int currentLabelTextSize;
	private int currentTempTextSize;
	private int currentAirQualityLabelTextSize;

	private final int cellCount = 5;

	public SecSimpleWidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
		super(context, widgetUpdateCallback, appWidgetId);
		refreshDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a h:mm");
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
		tempTextSize = context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInSimpleWidgetForecastItem) + extraSize;
		popTextSize = context.getResources().getDimensionPixelSize(R.dimen.popTextSizeInSimpleWidgetForecastItem) + extraSize;
		dateTextSize = context.getResources().getDimensionPixelSize(R.dimen.dateTimeTextSizeInSimpleWidgetForecastItem) + extraSize;
		currentPrecipitationTextSize = context.getResources().getDimensionPixelSize(R.dimen.precipitationTextSizeInCurrentConditionsViewForSimpleWidget) + extraSize;
		currentAirQualityTextSize = context.getResources().getDimensionPixelSize(R.dimen.airQualityTextSizeInCurrentConditionsViewForSimpleWidget) + extraSize;
		currentAirQualityLabelTextSize = context.getResources().getDimensionPixelSize(R.dimen.airQualityTextSizeInCurrentConditionsViewForSimpleWidget) + extraSize;
		currentLabelTextSize = context.getResources().getDimensionPixelSize(R.dimen.currentLabelTextSizeInCurrentConditionsViewForSimpleWidget) + extraSize;
		currentTempTextSize = context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInCurrentConditionsViewForSimpleWidget) + extraSize;
	}


	public View makeCurrentConditionsViews(LayoutInflater layoutInflater, CurrentConditionsDto currentConditionsDto,
	                                       AirQualityDto airQualityDto) {
		final String celsius = "C";
		final String fahrenheit = "F";

		View view = layoutInflater.inflate(R.layout.view_current_conditions_for_simple_widget, null, false);
		((TextView) view.findViewById(R.id.temperature)).setText(currentConditionsDto.getTemp().replace(celsius, "").replace(fahrenheit, ""));
		((ImageView) view.findViewById(R.id.weatherIcon)).setImageResource(currentConditionsDto.getWeatherIcon());

		String precipitation = "";
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto.getPrecipitationVolume();
		} else {
			precipitation = context.getString(R.string.not_precipitation);
		}
		((TextView) view.findViewById(R.id.precipitation)).setText(precipitation);
		((TextView) view.findViewById(R.id.airQuality)).setText(AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi()));

		((TextView) view.findViewById(R.id.currentLabel)).setTextSize(TypedValue.COMPLEX_UNIT_PX, currentLabelTextSize);
		((TextView) view.findViewById(R.id.temperature)).setTextSize(TypedValue.COMPLEX_UNIT_PX, currentTempTextSize);
		((TextView) view.findViewById(R.id.precipitation)).setTextSize(TypedValue.COMPLEX_UNIT_PX, currentPrecipitationTextSize);
		((TextView) view.findViewById(R.id.airQuality)).setTextSize(TypedValue.COMPLEX_UNIT_PX, currentAirQualityTextSize);
		((TextView) view.findViewById(R.id.airQualityLabel)).setTextSize(TypedValue.COMPLEX_UNIT_PX, currentAirQualityLabelTextSize);

		return view;
	}


	public View makeHeaderViews(LayoutInflater layoutInflater, String addressName, String lastRefreshDateTime) {
		View view = layoutInflater.inflate(R.layout.header_view_in_widget, null, false);
		((TextView) view.findViewById(R.id.address)).setText(addressName);
		((TextView) view.findViewById(R.id.refresh)).setText(ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));

		((TextView) view.findViewById(R.id.address)).setTextSize(TypedValue.COMPLEX_UNIT_PX, addressTextSize);
		((TextView) view.findViewById(R.id.refresh)).setTextSize(TypedValue.COMPLEX_UNIT_PX, refreshDateTimeTextSize);

		return view;
	}

	public void setDataViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, AirQualityDto airQualityDto, CurrentConditionsDto currentConditionsDto,
	                         List<DailyForecastDto> dailyForecastDtoList, OnDrawBitmapCallback onDrawBitmapCallback) {

		drawViews(remoteViews, addressName, lastRefreshDateTime, airQualityDto, currentConditionsDto, dailyForecastDtoList, onDrawBitmapCallback);
	}

	public void setTempDataViews(RemoteViews remoteViews) {
		List<DailyForecastDto> dailyForecastDtoList = new ArrayList<>();
		final String pop = "20%";
		final String minTemp = "8";
		final String maxTemp = "17";
		ZonedDateTime now = ZonedDateTime.now();

		for (int i = 0; i < cellCount; i++) {
			DailyForecastDto dailyForecastDto = new DailyForecastDto();
			dailyForecastDto.setDate(now).setAmValues(new DailyForecastDto.Values()).setPmValues(new DailyForecastDto.Values())
					.setMinTemp(minTemp).setMaxTemp(maxTemp);
			dailyForecastDto.getAmValues().setWeatherIcon(R.drawable.day_clear).setPop(pop);
			dailyForecastDto.getPmValues().setWeatherIcon(R.drawable.day_clear).setPop(pop);

			dailyForecastDtoList.add(dailyForecastDto);
			now = now.plusDays(1);
		}

		CurrentConditionsDto tempCurrentConditions = new CurrentConditionsDto();
		tempCurrentConditions.setTemp("15°");
		tempCurrentConditions.setWeatherIcon(R.drawable.day_clear);

		AirQualityDto tempAirQualityDto = new AirQualityDto();
		tempAirQualityDto.setAqi(10);

		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(), tempAirQualityDto, tempCurrentConditions,
				dailyForecastDtoList, null);
	}

	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, AirQualityDto airQualityDto, CurrentConditionsDto currentConditionsDto,
	                       List<DailyForecastDto> dailyForecastDtoList, @Nullable OnDrawBitmapCallback onDrawBitmapCallback) {
		LayoutInflater layoutInflater = LayoutInflater.from(context);

		View headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime);
		headerView.setId(R.id.header);

		View currentConditionsView = makeCurrentConditionsViews(layoutInflater, currentConditionsDto, airQualityDto);
		currentConditionsView.setId(R.id.currentConditions);

		LinearLayout hourAndIconLinearLayout = new LinearLayout(context);
		hourAndIconLinearLayout.setId(R.id.hourAndIconView);
		hourAndIconLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams hourAndIconCellLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		hourAndIconCellLayoutParams.gravity = Gravity.CENTER;
		hourAndIconCellLayoutParams.weight = 1;

		List<Integer> minTempList = new ArrayList<>();
		List<Integer> maxTempList = new ArrayList<>();
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEE");
		String pop = null;

		for (int cell = 0; cell < cellCount; cell++) {
			View view = layoutInflater.inflate(R.layout.view_forecast_item_in_linear, null, false);
			//hour, weatherIcon
			((TextView) view.findViewById(R.id.dateTime)).setText(dailyForecastDtoList.get(cell).getDate().format(dateFormatter));

			((TextView) view.findViewById(R.id.dateTime)).setTextSize(TypedValue.COMPLEX_UNIT_PX, dateTextSize);
			((TextView) view.findViewById(R.id.pop)).setTextSize(TypedValue.COMPLEX_UNIT_PX, popTextSize);

			if (dailyForecastDtoList.get(cell).isSingle()) {
				((ImageView) view.findViewById(R.id.leftIcon)).setImageResource(dailyForecastDtoList.get(cell).getSingleValues().getWeatherIcon());
				pop = dailyForecastDtoList.get(cell).getSingleValues().getPop();

				view.findViewById(R.id.rightIcon).setVisibility(View.GONE);
			} else {
				((ImageView) view.findViewById(R.id.leftIcon)).setImageResource(dailyForecastDtoList.get(cell).getAmValues().getWeatherIcon());
				((ImageView) view.findViewById(R.id.rightIcon)).setImageResource(dailyForecastDtoList.get(cell).getPmValues().getWeatherIcon());
				pop = dailyForecastDtoList.get(cell).getAmValues().getPop() + "/" +
						dailyForecastDtoList.get(cell).getPmValues().getPop();
			}
			((TextView) view.findViewById(R.id.pop)).setText(pop);

			view.findViewById(R.id.temperature).setVisibility(View.GONE);

			minTempList.add(Integer.parseInt(dailyForecastDtoList.get(cell).getMinTemp().replace(tempDegree, "")));
			maxTempList.add(Integer.parseInt(dailyForecastDtoList.get(cell).getMaxTemp().replace(tempDegree, "")));
			hourAndIconLinearLayout.addView(view, hourAndIconCellLayoutParams);
		}

		RelativeLayout.LayoutParams headerViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams currentConditionsViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		RelativeLayout.LayoutParams hourAndIconRowLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams tempRowLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		headerViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		currentConditionsViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		currentConditionsViewLayoutParams.addRule(RelativeLayout.BELOW, R.id.header);
		hourAndIconRowLayoutParams.addRule(RelativeLayout.BELOW, R.id.header);
		hourAndIconRowLayoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.currentConditions);
		tempRowLayoutParams.addRule(RelativeLayout.BELOW, R.id.hourAndIconView);
		tempRowLayoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.currentConditions);
		tempRowLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		DetailDoubleTemperatureViewForRemoteViews detailSingleTemperatureView = new DetailDoubleTemperatureViewForRemoteViews(context,
				minTempList, maxTempList);
		detailSingleTemperatureView.setTempTextSizePx(tempTextSize);

		RelativeLayout rootLayout = new RelativeLayout(context);

		rootLayout.addView(headerView, headerViewLayoutParams);
		rootLayout.addView(currentConditionsView, currentConditionsViewLayoutParams);
		rootLayout.addView(hourAndIconLinearLayout, hourAndIconRowLayoutParams);
		rootLayout.addView(detailSingleTemperatureView, tempRowLayoutParams);

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

		final int[] widgetSize = getWidgetExactSizeInPx(appWidgetManager);
		final float widgetPadding = context.getResources().getDimension(R.dimen.widget_padding);

		final int widthSpec = View.MeasureSpec.makeMeasureSpec((int) (widgetSize[0] - widgetPadding * 2), EXACTLY);
		final int heightSpec = View.MeasureSpec.makeMeasureSpec((int) (widgetSize[1] - widgetPadding * 2), EXACTLY);

		rootLayout.measure(widthSpec, heightSpec);
		rootLayout.layout(0, 0, rootLayout.getMeasuredWidth(), rootLayout.getMeasuredHeight());

		rootLayout.setDrawingCacheEnabled(true);
		rootLayout.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

		Bitmap viewBmp = rootLayout.getDrawingCache();
		if (onDrawBitmapCallback != null) {
			onDrawBitmapCallback.onCreatedBitmap(viewBmp);
		}
		remoteViews.setImageViewBitmap(R.id.valuesView, viewBmp);
	}

	@Override
	public void setDisplayClock(boolean displayClock) {
		widgetDto.setDisplayClock(displayClock);
	}

	@Override
	public void setDataViewsOfSavedData() {
		WeatherSourceType weatherSourceType = WeatherSourceType.valueOf(widgetDto.getWeatherSourceType());

		if (widgetDto.isTopPriorityKma() && widgetDto.getCountryCode().equals("KR")) {
			weatherSourceType = WeatherSourceType.KMA;
		}

		RemoteViews remoteViews = createRemoteViews(false);
		JsonObject jsonObject = (JsonObject) JsonParser.parseString(widgetDto.getResponseText());

		AirQualityDto airQualityDto = AqicnResponseProcessor.parseTextToAirQualityDto(context, jsonObject);
		CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.parseTextToCurrentConditionsDto(context, jsonObject,
				weatherSourceType, widgetDto.getLatitude(), widgetDto.getLongitude());

		List<DailyForecastDto> dailyForecastDtoList = WeatherResponseProcessor.parseTextToDailyForecastDtoList(context, jsonObject,
				weatherSourceType);

		setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), airQualityDto, currentConditionsDto,
				dailyForecastDtoList, null);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(appWidgetId,
				remoteViews);
	}

}
