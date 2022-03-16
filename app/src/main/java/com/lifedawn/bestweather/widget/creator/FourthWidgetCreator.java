package com.lifedawn.bestweather.widget.creator;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Bitmap;
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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.view.DetailDoubleTemperatureViewForRemoteViews;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;
import com.lifedawn.bestweather.widget.widgetprovider.FourthWidgetProvider;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FourthWidgetCreator extends AbstractWidgetCreator {
	private final DateTimeFormatter refreshDateTimeFormatter;

	private int addressTextSize;
	private int refreshDateTimeTextSize;
	private int dateTextSize;
	private int tempTextSize;
	private int popTextSize;
	private int rainVolumeTextSize;
	private int snowVolumeTextSize;
	private int currentPrecipitationTextSize;
	private int currentAirQualityTextSize;
	private int currentLabelTextSize;
	private int currentTempTextSize;
	private int currentAirQualityLabelTextSize;

	private final int cellCount = 5;

	public FourthWidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
		super(context, widgetUpdateCallback, appWidgetId);
		refreshDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a h:mm");
	}

	@Override
	public Set<WeatherDataType> getRequestWeatherDataTypeSet() {
		Set<WeatherDataType> set = new HashSet<>();
		set.add(WeatherDataType.currentConditions);
		set.add(WeatherDataType.dailyForecast);
		set.add(WeatherDataType.airQuality);

		return set;
	}

	@Override
	public RemoteViews createTempViews(Integer parentWidth, Integer parentHeight) {
		RemoteViews remoteViews = createBaseRemoteViews();
		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(), WeatherResponseProcessor.getTempAirQualityDto(),
				WeatherResponseProcessor.getTempCurrentConditionsDto(context),
				WeatherResponseProcessor.getTempDailyForecastDtoList(context, cellCount), null, parentWidth, parentHeight);
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
		return FourthWidgetProvider.class;
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
		rainVolumeTextSize = context.getResources().getDimensionPixelSize(R.dimen.rainVolumeTextSizeInSimpleWidgetForecastItem) + extraSize;
		snowVolumeTextSize = context.getResources().getDimensionPixelSize(R.dimen.snowVolumeTextSizeInSimpleWidgetForecastItem) + extraSize;
		dateTextSize = context.getResources().getDimensionPixelSize(R.dimen.dateTimeTextSizeInSimpleWidgetForecastItem) + extraSize;
		currentPrecipitationTextSize = context.getResources().getDimensionPixelSize(R.dimen.precipitationTextSizeInCurrentConditionsViewForSimpleWidget) + extraSize;
		currentAirQualityTextSize = context.getResources().getDimensionPixelSize(R.dimen.airQualityTextSizeInCurrentConditionsViewForSimpleWidget) + extraSize;
		currentAirQualityLabelTextSize = context.getResources().getDimensionPixelSize(R.dimen.airQualityTextSizeInCurrentConditionsViewForSimpleWidget) + extraSize;
		currentLabelTextSize = context.getResources().getDimensionPixelSize(R.dimen.currentLabelTextSizeInCurrentConditionsViewForSimpleWidget) + extraSize;
		currentTempTextSize = context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInCurrentConditionsViewForSimpleWidget) + extraSize;
	}


	public View makeCurrentConditionsViews(LayoutInflater layoutInflater, CurrentConditionsDto currentConditionsDto,
	                                       AirQualityDto airQualityDto) {


		View view = layoutInflater.inflate(R.layout.view_current_conditions_for_simple_widget, null, false);
		((TextView) view.findViewById(R.id.temperature)).setText(currentConditionsDto.getTemp().replace(tempDegree, "°"));
		((ImageView) view.findViewById(R.id.weatherIcon)).setImageResource(currentConditionsDto.getWeatherIcon());

		String precipitation = "";
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto.getPrecipitationVolume();
		} else {
			precipitation = context.getString(R.string.not_precipitation);
		}
		((TextView) view.findViewById(R.id.precipitation)).setText(precipitation);

		String airQuality = context.getString(R.string.air_quality) + ": ";
		if (airQualityDto.isSuccessful()) {
			airQuality += AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi());
		} else {
			airQuality += context.getString(R.string.noData);
		}

		((TextView) view.findViewById(R.id.airQuality)).setText(airQuality);

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

		drawViews(remoteViews, addressName, lastRefreshDateTime, airQualityDto, currentConditionsDto, dailyForecastDtoList,
				onDrawBitmapCallback, null, null);
	}


	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, AirQualityDto airQualityDto, CurrentConditionsDto currentConditionsDto,
	                       List<DailyForecastDto> dailyForecastDtoList, @Nullable OnDrawBitmapCallback onDrawBitmapCallback, @Nullable Integer parentWidth,
	                       @Nullable Integer parentHeight) {
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
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("E");
		String pop = null;

		boolean haveRain = false;
		boolean haveSnow = false;

		for (int cell = 0; cell < cellCount; cell++) {
			if (dailyForecastDtoList.get(cell).isSingle()) {
				if (dailyForecastDtoList.get(cell).getSingleValues().isHasRainVolume()) {
					haveRain = true;
				}
				if (dailyForecastDtoList.get(cell).getSingleValues().isHasSnowVolume()) {
					haveSnow = true;
				}
			} else {
				if (dailyForecastDtoList.get(cell).getAmValues().isHasRainVolume() ||
						dailyForecastDtoList.get(cell).getPmValues().isHasRainVolume()) {
					haveRain = true;
				}
				if (dailyForecastDtoList.get(cell).getAmValues().isHasSnowVolume() ||
						dailyForecastDtoList.get(cell).getPmValues().isHasSnowVolume()) {
					haveSnow = true;
				}
			}
		}

		final String mm = "mm";
		final String cm = "cm";

		boolean isSingle = false;
		Double rainVolume = 0.0;
		Double snowVolume = 0.0;

		for (int cell = 0; cell < cellCount; cell++) {
			isSingle = dailyForecastDtoList.get(cell).isSingle();
			rainVolume = 0.0;
			snowVolume = 0.0;

			View view = layoutInflater.inflate(R.layout.view_forecast_item_in_linear, null, false);
			//hour, weatherIcon
			((TextView) view.findViewById(R.id.dateTime)).setText(dailyForecastDtoList.get(cell).getDate().format(dateFormatter));

			((TextView) view.findViewById(R.id.dateTime)).setTextSize(TypedValue.COMPLEX_UNIT_PX, dateTextSize);
			((TextView) view.findViewById(R.id.pop)).setTextSize(TypedValue.COMPLEX_UNIT_PX, popTextSize);

			if (isSingle) {
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

			if (haveRain) {
				if (isSingle) {
					if (dailyForecastDtoList.get(cell).getSingleValues().isHasRainVolume()) {
						rainVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getSingleValues().getRainVolume().replace(mm, "")
								.replace(cm, ""));
					}
				} else {
					if (dailyForecastDtoList.get(cell).getAmValues().isHasRainVolume()) {
						rainVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getAmValues().getRainVolume().replace(mm, "")
								.replace(cm, ""));
					}
					if (dailyForecastDtoList.get(cell).getPmValues().isHasRainVolume()) {
						rainVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getPmValues().getRainVolume().replace(mm, "")
								.replace(cm, ""));
					}
				}
				if (rainVolume == 0.0) {
					view.findViewById(R.id.rainVolumeLayout).setVisibility(View.INVISIBLE);
				} else {
					((TextView) view.findViewById(R.id.rainVolume)).setText(String.format(Locale.getDefault(), "%.1f", rainVolume));
					((TextView) view.findViewById(R.id.rainVolume)).setTextSize(TypedValue.COMPLEX_UNIT_PX, rainVolumeTextSize);
				}
			} else {
				view.findViewById(R.id.rainVolumeLayout).setVisibility(View.GONE);
			}

			if (haveSnow) {
				if (isSingle) {
					if (dailyForecastDtoList.get(cell).getSingleValues().isHasSnowVolume()) {
						snowVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getSingleValues().getSnowVolume().replace(mm, "")
								.replace(cm, ""));
					}
				} else {
					if (dailyForecastDtoList.get(cell).getAmValues().isHasSnowVolume()) {
						snowVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getAmValues().getSnowVolume().replace(mm, "")
								.replace(cm, ""));
					}
					if (dailyForecastDtoList.get(cell).getPmValues().isHasSnowVolume()) {
						snowVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getPmValues().getSnowVolume().replace(mm, "")
								.replace(cm, ""));
					}
				}
				if (snowVolume == 0.0) {
					view.findViewById(R.id.snowVolumeLayout).setVisibility(View.INVISIBLE);
				} else {
					((TextView) view.findViewById(R.id.snowVolume)).setText(String.format(Locale.getDefault(), "%.1f", snowVolume));
					((TextView) view.findViewById(R.id.snowVolume)).setTextSize(TypedValue.COMPLEX_UNIT_PX, snowVolumeTextSize);
				}
			} else {
				view.findViewById(R.id.snowVolumeLayout).setVisibility(View.GONE);
			}

			view.findViewById(R.id.temperature).setVisibility(View.GONE);

			minTempList.add(Integer.parseInt(dailyForecastDtoList.get(cell).getMinTemp().replace(tempDegree, "")));
			maxTempList.add(Integer.parseInt(dailyForecastDtoList.get(cell).getMaxTemp().replace(tempDegree, "")));
			hourAndIconLinearLayout.addView(view, hourAndIconCellLayoutParams);
		}

		RelativeLayout.LayoutParams headerViewLayoutParams = getHeaderViewLayoutParams();
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

		RemoteViews remoteViews = createRemoteViews();
		JsonObject jsonObject = (JsonObject) JsonParser.parseString(widgetDto.getResponseText());

		AirQualityDto airQualityDto = AqicnResponseProcessor.parseTextToAirQualityDto(jsonObject);
		CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.parseTextToCurrentConditionsDto(context, jsonObject,
				weatherProviderType, widgetDto.getLatitude(), widgetDto.getLongitude());

		List<DailyForecastDto> dailyForecastDtoList = WeatherResponseProcessor.parseTextToDailyForecastDtoList(context, jsonObject,
				weatherProviderType);

		setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), airQualityDto, currentConditionsDto,
				dailyForecastDtoList, null);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(appWidgetId,
				remoteViews);
	}

	@Override
	public void setResultViews(int appWidgetId, RemoteViews remoteViews, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader) {
		final WeatherProviderType weatherProviderType = WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherProviderTypeSet());

		final CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.getCurrentConditionsDto(context, multipleRestApiDownloader,
				weatherProviderType);
		final List<DailyForecastDto> dailyForecastDtoList = WeatherResponseProcessor.getDailyForecastDtoList(context, multipleRestApiDownloader,
				weatherProviderType);

		final boolean successful = currentConditionsDto != null && !dailyForecastDtoList.isEmpty();

		if (successful) {
			ZoneId zoneId = currentConditionsDto.getCurrentTime().getZone();
			ZoneOffset zoneOffset = currentConditionsDto.getCurrentTime().getOffset();
			widgetDto.setTimeZoneId(zoneId.getId());
			widgetDto.setLastRefreshDateTime(multipleRestApiDownloader.getRequestDateTime().toString());

			AirQualityDto airQualityDto = WeatherResponseProcessor.getAirQualityDto(context, multipleRestApiDownloader,
					zoneOffset);


			setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), airQualityDto,
					currentConditionsDto, dailyForecastDtoList, new OnDrawBitmapCallback() {
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
