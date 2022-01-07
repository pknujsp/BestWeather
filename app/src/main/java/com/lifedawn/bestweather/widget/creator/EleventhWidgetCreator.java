package com.lifedawn.bestweather.widget.creator;

import android.appwidget.AppWidgetManager;
import android.content.Context;
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
import androidx.core.content.ContextCompat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class EleventhWidgetCreator extends AbstractWidgetCreator {
	private final DateTimeFormatter refreshDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a h:mm");

	private int addressTextSize;
	private int refreshDateTimeTextSize;
	private int hourTextSize;
	private int tempTextSize;
	private int popTextSize;
	private int rainVolumeTextSize;
	private int snowVolumeTextSize;
	private int weatherSourceNameTextSize;

	private final int cellCount = 9;

	public EleventhWidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
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
		hourTextSize = context.getResources().getDimensionPixelSize(R.dimen.dateTimeTextSizeInSimpleWidgetForecastItem) + extraSize;
		tempTextSize = context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInSimpleWidgetForecastItem) + extraSize;
		popTextSize = context.getResources().getDimensionPixelSize(R.dimen.popTextSizeInSimpleWidgetForecastItem) + extraSize;
		rainVolumeTextSize = context.getResources().getDimensionPixelSize(R.dimen.rainVolumeTextSizeInSimpleWidgetForecastItem) + extraSize;
		snowVolumeTextSize = context.getResources().getDimensionPixelSize(R.dimen.snowVolumeTextSizeInSimpleWidgetForecastItem) + extraSize;
		weatherSourceNameTextSize = context.getResources().getDimensionPixelSize(R.dimen.weatherSourceNameTextSizeInWidget) + extraSize;
	}


	public View makeHeaderViews(LayoutInflater layoutInflater, String addressName, String lastRefreshDateTime) {
		View view = layoutInflater.inflate(R.layout.header_view_in_widget, null, false);
		((TextView) view.findViewById(R.id.address)).setText(addressName);
		((TextView) view.findViewById(R.id.refresh)).setText(ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));

		((TextView) view.findViewById(R.id.address)).setTextSize(TypedValue.COMPLEX_UNIT_PX, addressTextSize);
		((TextView) view.findViewById(R.id.refresh)).setTextSize(TypedValue.COMPLEX_UNIT_PX, refreshDateTimeTextSize);

		return view;
	}

	public void setDataViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, ArrayMap<WeatherDataSourceType,
			List<HourlyForecastDto>> hourlyForecastDtoListMap,
	                         OnDrawBitmapCallback onDrawBitmapCallback) {
		drawViews(remoteViews, addressName, lastRefreshDateTime, hourlyForecastDtoListMap, onDrawBitmapCallback);
	}

	public void setTempDataViews(RemoteViews remoteViews) {
		ArrayMap<WeatherDataSourceType, List<HourlyForecastDto>> hourlyForecastDtoListMap = new ArrayMap<>();
		hourlyForecastDtoListMap.put(WeatherDataSourceType.ACCU_WEATHER, WeatherResponseProcessor.getTempHourlyForecastDtoList(context,
				cellCount));
		hourlyForecastDtoListMap.put(WeatherDataSourceType.OWM_ONECALL, WeatherResponseProcessor.getTempHourlyForecastDtoList(context,
				cellCount));

		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(),
				hourlyForecastDtoListMap,
				null);
	}

	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime,
	                       ArrayMap<WeatherDataSourceType, List<HourlyForecastDto>> hourlyForecastDtoListMap, @Nullable OnDrawBitmapCallback onDrawBitmapCallback) {
		RelativeLayout rootLayout = new RelativeLayout(context);
		LayoutInflater layoutInflater = LayoutInflater.from(context);

		View headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime);
		headerView.setId(R.id.header);

		DateTimeFormatter hour0Formatter = DateTimeFormatter.ofPattern("E 0");

		//첫번째로 일치하는 시각을 찾는다. 첫 시각이 kma가 12시, owm이 13시 이면 13시를 첫 시작으로 하여 화면을 표시
		Set<WeatherDataSourceType> weatherDataSourceTypeSet = hourlyForecastDtoListMap.keySet();
		ZonedDateTime firstDateTime = null;

		for (List<HourlyForecastDto> hourlyForecastDtoList : hourlyForecastDtoListMap.values()) {
			if (firstDateTime == null) {
				firstDateTime = ZonedDateTime.of(hourlyForecastDtoList.get(0).getHours().toLocalDateTime(),
						hourlyForecastDtoList.get(0).getHours().getZone());
			} else if (firstDateTime.isBefore(hourlyForecastDtoList.get(0).getHours())) {
				firstDateTime = ZonedDateTime.of(hourlyForecastDtoList.get(0).getHours().toLocalDateTime(),
						hourlyForecastDtoList.get(0).getHours().getZone());
			}
		}

		final long firstHours = TimeUnit.SECONDS.toHours(firstDateTime.toEpochSecond());
		long hours = 0;
		Map<WeatherDataSourceType, Integer> firstBeginIdxMap = new HashMap<>();
		for (WeatherDataSourceType weatherDataSourceType : hourlyForecastDtoListMap.keySet()) {
			hours = TimeUnit.SECONDS.toHours(hourlyForecastDtoListMap.get(weatherDataSourceType).get(0).getHours().toEpochSecond());
			firstBeginIdxMap.put(weatherDataSourceType, (int) (firstHours - hours));
		}

		//시각을 먼저 표시
		String hour = null;
		LinearLayout hoursRow = new LinearLayout(context);
		hoursRow.setId(R.id.hoursRow);
		hoursRow.setOrientation(LinearLayout.HORIZONTAL);

		LinearLayout.LayoutParams hourTextViewLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
		hourTextViewLayoutParams.weight = 1;

		final int textColor = ContextCompat.getColor(context, R.color.widgetTextColor);

		for (int i = 0; i < cellCount; i++) {
			hour = firstDateTime.getHour() == 0 ? firstDateTime.format(hour0Formatter) : String.valueOf(firstDateTime.getHour());
			firstDateTime = firstDateTime.plusHours(1);

			TextView textView = new TextView(context);
			textView.setText(hour);
			textView.setTextColor(textColor);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, hourTextSize);
			textView.setGravity(Gravity.CENTER);
			textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

			hoursRow.addView(textView, hourTextViewLayoutParams);
		}

		final LinearLayout forecastTable = new LinearLayout(context);
		forecastTable.setOrientation(LinearLayout.VERTICAL);

		final LinearLayout.LayoutParams forecastRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
		forecastRowLayoutParams.weight = 1;

		final String mm = "mm";
		final String cm = "cm";

		for (WeatherDataSourceType weatherDataSourceType : weatherDataSourceTypeSet) {
			List<HourlyForecastDto> hourlyForecastDtoList = hourlyForecastDtoListMap.get(weatherDataSourceType);

			LinearLayout row = new LinearLayout(context);
			row.setOrientation(LinearLayout.VERTICAL);
			row.setLayoutParams(forecastRowLayoutParams);

			LinearLayout hourlyForecastListView = new LinearLayout(context);
			hourlyForecastListView.setOrientation(LinearLayout.HORIZONTAL);

			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.gravity = Gravity.CENTER;
			layoutParams.weight = 1;

			boolean haveRain = false;
			boolean haveSnow = false;

			int count = cellCount + firstBeginIdxMap.get(weatherDataSourceType);

			for (int cell = firstBeginIdxMap.get(weatherDataSourceType); cell < count; cell++) {
				if (hourlyForecastDtoList.get(cell).isHasRain()) {
					if (!haveRain) {
						haveRain = true;
					}
				}
				if (hourlyForecastDtoList.get(cell).isHasSnow()) {
					if (!haveSnow) {
						haveSnow = true;
					}
				}
			}

			for (int cell = firstBeginIdxMap.get(weatherDataSourceType); cell < count; cell++) {
				View view = layoutInflater.inflate(R.layout.view_forecast_item_in_linear, null, false);
				((ImageView) view.findViewById(R.id.leftIcon)).setImageResource(hourlyForecastDtoList.get(cell).getWeatherIcon());

				if (!hourlyForecastDtoList.get(cell).getPop().equals("-")) {
					((TextView) view.findViewById(R.id.pop)).setText(hourlyForecastDtoList.get(cell).getPop());
				} else {
					view.findViewById(R.id.popLayout).setVisibility(View.INVISIBLE);
				}

				if (haveRain) {
					if (hourlyForecastDtoList.get(cell).isHasRain()) {
						((TextView) view.findViewById(R.id.rainVolume)).setText(hourlyForecastDtoList.get(cell).getRainVolume().replace(mm,
								"").replace(cm, ""));
						((TextView) view.findViewById(R.id.rainVolume)).setTextSize(TypedValue.COMPLEX_UNIT_PX, rainVolumeTextSize);
					} else {
						view.findViewById(R.id.rainVolumeLayout).setVisibility(View.INVISIBLE);
					}
				} else {
					view.findViewById(R.id.rainVolumeLayout).setVisibility(View.GONE);
				}
				if (haveSnow) {
					if (hourlyForecastDtoList.get(cell).isHasSnow()) {
						((TextView) view.findViewById(R.id.snowVolume)).setText(hourlyForecastDtoList.get(cell).getSnowVolume().replace(mm,
								"").replace(cm, ""));
						((TextView) view.findViewById(R.id.snowVolume)).setTextSize(TypedValue.COMPLEX_UNIT_PX, snowVolumeTextSize);
					} else {
						view.findViewById(R.id.snowVolumeLayout).setVisibility(View.INVISIBLE);
					}
				} else {
					view.findViewById(R.id.snowVolumeLayout).setVisibility(View.GONE);
				}

				((TextView) view.findViewById(R.id.pop)).setTextSize(TypedValue.COMPLEX_UNIT_PX, popTextSize);
				((TextView) view.findViewById(R.id.temperature)).setTextSize(TypedValue.COMPLEX_UNIT_PX, tempTextSize);
				((TextView) view.findViewById(R.id.temperature)).setText(hourlyForecastDtoList.get(cell).getTemp());

				view.findViewById(R.id.dateTime).setVisibility(View.GONE);
				view.findViewById(R.id.rightIcon).setVisibility(View.GONE);

				hourlyForecastListView.addView(view, layoutParams);
			}


			String weatherSource = null;
			int icon = 0;
			if (weatherDataSourceType == WeatherDataSourceType.KMA_WEB) {
				weatherSource = context.getString(R.string.kma);
				icon = R.drawable.kmaicon;
			} else if (weatherDataSourceType == WeatherDataSourceType.ACCU_WEATHER) {
				weatherSource = context.getString(R.string.accu_weather);
				icon = R.drawable.accuicon;
			} else if (weatherDataSourceType == WeatherDataSourceType.OWM_ONECALL
					|| weatherDataSourceType == WeatherDataSourceType.OWM_INDIVIDUAL) {
				weatherSource = context.getString(R.string.owm);
				icon = R.drawable.owmicon;
			}

			View weatherSourceView = layoutInflater.inflate(R.layout.weather_data_source_view, null);
			((TextView) weatherSourceView.findViewById(R.id.source)).setTextSize(TypedValue.COMPLEX_UNIT_PX, weatherSourceNameTextSize);
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
				ViewGroup.LayoutParams.MATCH_PARENT);

		headerViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		hoursRowLayoutParams.addRule(RelativeLayout.BELOW, R.id.header);
		forecastViewsLayoutParams.addRule(RelativeLayout.BELOW, R.id.hoursRow);
		forecastViewsLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		rootLayout.addView(headerView, headerViewLayoutParams);
		rootLayout.addView(hoursRow, hoursRowLayoutParams);
		rootLayout.addView(forecastTable, forecastViewsLayoutParams);

		drawBitmap(rootLayout, onDrawBitmapCallback, remoteViews);
	}


	@Override
	public void setDisplayClock(boolean displayClock) {
		widgetDto.setDisplayClock(displayClock);
	}

	@Override
	public void setDataViewsOfSavedData() {
		RemoteViews remoteViews = createRemoteViews(false);
		JsonObject jsonObject = (JsonObject) JsonParser.parseString(widgetDto.getResponseText());

		ArrayMap<WeatherDataSourceType, List<HourlyForecastDto>> weatherSourceTypeListArrayMap = new ArrayMap<>();
		Set<WeatherDataSourceType> weatherDataSourceTypeSet = widgetDto.getWeatherSourceTypeSet();

		for (WeatherDataSourceType weatherDataSourceType : weatherDataSourceTypeSet) {
			weatherSourceTypeListArrayMap.put(weatherDataSourceType,
					WeatherResponseProcessor.parseTextToHourlyForecastDtoList(context, jsonObject, weatherDataSourceType, widgetDto.getLatitude(),
							widgetDto.getLongitude()));
		}
		setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(),
				weatherSourceTypeListArrayMap, null);

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(appWidgetId,
				remoteViews);
	}

}
