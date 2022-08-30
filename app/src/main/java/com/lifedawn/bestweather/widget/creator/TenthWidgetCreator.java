package com.lifedawn.bestweather.widget.creator;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherRequestUtil;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.view.DetailDoubleTemperatureViewForRemoteViews;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;
import com.lifedawn.bestweather.widget.widgetprovider.TenthWidgetProvider;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TenthWidgetCreator extends AbstractWidgetCreator {
	private final DateTimeFormatter refreshDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a h:mm");
	private final int DAY_LENGTH = 8;

	private int addressTextSize;
	private int refreshDateTimeTextSize;
	private int dateTextSize;
	private int tempTextSize;
	private int popTextSize;
	private int rainVolumeTextSize;
	private int snowVolumeTextSize;

	public TenthWidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
		super(context, widgetUpdateCallback, appWidgetId);
	}


	@Override
	public Set<WeatherDataType> getRequestWeatherDataTypeSet() {
		Set<WeatherDataType> set = new HashSet<>();
		set.add(WeatherDataType.dailyForecast);

		return set;
	}

	@Override
	public RemoteViews createTempViews(Integer parentWidth, Integer parentHeight) {
		RemoteViews remoteViews = createBaseRemoteViews();
		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(),
				WeatherResponseProcessor.getTempDailyForecastDtoList(context, DAY_LENGTH), null, parentWidth, parentHeight);
		return remoteViews;
	}

	@Override
	public RemoteViews createRemoteViews() {
		RemoteViews remoteViews = createBaseRemoteViews();
		remoteViews.setOnClickPendingIntent(R.id.root_layout, getOnClickedPendingIntent());

		return remoteViews;
	}

	@Override
	public Class<?> widgetProviderClass() {
		return TenthWidgetProvider.class;
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
		rainVolumeTextSize = context.getResources().getDimensionPixelSize(R.dimen.rainVolumeTextSizeInSimpleWidgetForecastItem) + extraSize;
		snowVolumeTextSize = context.getResources().getDimensionPixelSize(R.dimen.snowVolumeTextSizeInSimpleWidgetForecastItem) + extraSize;
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
	                         List<DailyForecastDto> dailyForecastDtoList, OnDrawBitmapCallback onDrawBitmapCallback) {
		drawViews(remoteViews, addressName, lastRefreshDateTime, dailyForecastDtoList, onDrawBitmapCallback, null, null);
	}


	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime,
	                       List<DailyForecastDto> dailyForecastDtoList, @Nullable OnDrawBitmapCallback onDrawBitmapCallback, @Nullable Integer parentWidth,
	                       @Nullable Integer parentHeight) {
		LayoutInflater layoutInflater = LayoutInflater.from(context);

		View headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime);
		headerView.setId(R.id.header);

		LinearLayout hourAndIconLinearLayout = new LinearLayout(context);
		hourAndIconLinearLayout.setId(R.id.hourAndIconView);
		hourAndIconLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams hourAndIconCellLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
		hourAndIconCellLayoutParams.weight = 1;

		List<Integer> minTempList = new ArrayList<>();
		List<Integer> maxTempList = new ArrayList<>();
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M.d\nE");

		for (int cell = 0; cell < DAY_LENGTH; cell++) {
			if (!dailyForecastDtoList.get(cell).isAvailable_toMakeMinMaxTemp())
				break;
			View view = layoutInflater.inflate(R.layout.view_forecast_item_in_linear, null, false);
			((TextView) view.findViewById(R.id.dateTime)).setText(dailyForecastDtoList.get(cell).getDate().format(dateFormatter));
			((TextView) view.findViewById(R.id.dateTime)).setTextSize(TypedValue.COMPLEX_UNIT_PX, dateTextSize);

			if (dailyForecastDtoList.get(cell).getValuesList().size() == 1) {
				((ImageView) view.findViewById(R.id.leftIcon)).setImageResource(dailyForecastDtoList.get(cell).getValuesList().get(0).getWeatherIcon());
				view.findViewById(R.id.rightIcon).setVisibility(View.GONE);
			} else if (dailyForecastDtoList.get(cell).getValuesList().size() == 2) {
				((ImageView) view.findViewById(R.id.leftIcon)).setImageResource(dailyForecastDtoList.get(cell).getValuesList().get(0).getWeatherIcon());
				((ImageView) view.findViewById(R.id.rightIcon)).setImageResource(dailyForecastDtoList.get(cell).getValuesList().get(1).getWeatherIcon());
			} else if (dailyForecastDtoList.get(cell).getValuesList().size() == 4) {
				((ImageView) view.findViewById(R.id.leftIcon)).setImageResource(dailyForecastDtoList.get(cell).getValuesList().get(1).getWeatherIcon());
				((ImageView) view.findViewById(R.id.rightIcon)).setImageResource(dailyForecastDtoList.get(cell).getValuesList().get(2).getWeatherIcon());
			}

			view.findViewById(R.id.temperature).setVisibility(View.GONE);
			view.findViewById(R.id.popLayout).setVisibility(View.GONE);
			view.findViewById(R.id.rainVolumeLayout).setVisibility(View.GONE);
			view.findViewById(R.id.snowVolumeLayout).setVisibility(View.GONE);

			minTempList.add(Integer.parseInt(dailyForecastDtoList.get(cell).getMinTemp().replace(tempDegree, "")));
			maxTempList.add(Integer.parseInt(dailyForecastDtoList.get(cell).getMaxTemp().replace(tempDegree, "")));
			hourAndIconLinearLayout.addView(view, hourAndIconCellLayoutParams);
		}

		RelativeLayout.LayoutParams headerViewLayoutParams = getHeaderViewLayoutParams();
		RelativeLayout.LayoutParams hourAndIconRowLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams tempRowLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		headerViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
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
		rootLayout.addView(hourAndIconLinearLayout, hourAndIconRowLayoutParams);
		rootLayout.addView(detailSingleTemperatureView, tempRowLayoutParams);

		drawBitmap(rootLayout, onDrawBitmapCallback, remoteViews, parentWidth, parentHeight);
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

		RemoteViews remoteViews = createRemoteViews();
		JsonObject jsonObject = (JsonObject) JsonParser.parseString(widgetDto.getResponseText());

		List<DailyForecastDto> dailyForecastDtoList = WeatherResponseProcessor.parseTextToDailyForecastDtoList(context, jsonObject,
				weatherProviderType, widgetDto.getTimeZoneId());

		setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(),
				dailyForecastDtoList, null);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(appWidgetId,
				remoteViews);
	}

	@Override
	public void setResultViews(int appWidgetId, RemoteViews remoteViews, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader) {

		final List<DailyForecastDto> dailyForecastDtoList = WeatherResponseProcessor.getDailyForecastDtoList(context, multipleRestApiDownloader,
				WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherProviderTypeSet()));
		final boolean successful = !dailyForecastDtoList.isEmpty();

		if (successful) {
			ZoneId zoneId = dailyForecastDtoList.get(0).getDate().getZone();
			ZoneOffset zoneOffset = dailyForecastDtoList.get(0).getDate().getOffset();
			widgetDto.setTimeZoneId(zoneId.getId());
			widgetDto.setLastRefreshDateTime(multipleRestApiDownloader.getRequestDateTime().toString());

			setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(),
					dailyForecastDtoList, new OnDrawBitmapCallback() {
						@Override
						public void onCreatedBitmap(Bitmap bitmap) {

						}
					});
			makeResponseTextToJson(multipleRestApiDownloader, getRequestWeatherDataTypeSet(), widgetDto.getWeatherProviderTypeSet(), widgetDto, zoneOffset);
		}

		widgetDto.setLoadSuccessful(successful);

		super.setResultViews(appWidgetId, remoteViews, multipleRestApiDownloader);
	}
}
