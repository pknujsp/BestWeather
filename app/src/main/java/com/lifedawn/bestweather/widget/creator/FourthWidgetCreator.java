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
import com.lifedawn.bestweather.forremoteviews.RemoteViewsUtil;
import com.lifedawn.bestweather.retrofit.util.WeatherRestApiDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherRequestUtil;
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

		RemoteViewsUtil.onSuccessfulProcess(remoteViews);

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

		String airQuality = null;
		if (airQualityDto.isSuccessful()) {
			airQuality = AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi());
		} else {
			airQuality = context.getString(R.string.noData);
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
			if (dailyForecastDtoList.get(cell).getValuesList().size() == 1) {
				if (dailyForecastDtoList.get(cell).getValuesList().get(0).isHasRainVolume()) {
					haveRain = true;
				}
				if (dailyForecastDtoList.get(cell).getValuesList().get(0).isHasSnowVolume()) {
					haveSnow = true;
				}
			} else if (dailyForecastDtoList.get(cell).getValuesList().size() == 2) {
				if (dailyForecastDtoList.get(cell).getValuesList().get(0).isHasRainVolume() ||
						dailyForecastDtoList.get(cell).getValuesList().get(1).isHasRainVolume()) {
					haveRain = true;
				}
				if (dailyForecastDtoList.get(cell).getValuesList().get(0).isHasSnowVolume() ||
						dailyForecastDtoList.get(cell).getValuesList().get(1).isHasSnowVolume()) {
					haveSnow = true;
				}
			} else if (dailyForecastDtoList.get(cell).getValuesList().size() == 4) {
				if (dailyForecastDtoList.get(cell).getValuesList().get(0).isHasPrecipitationVolume() ||
						dailyForecastDtoList.get(cell).getValuesList().get(1).isHasPrecipitationVolume() ||
						dailyForecastDtoList.get(cell).getValuesList().get(2).isHasPrecipitationVolume() ||
						dailyForecastDtoList.get(cell).getValuesList().get(3).isHasPrecipitationVolume()) {
					haveRain = true;
				}
			}
		}

		final String mm = "mm";
		final String cm = "cm";

		Double rainVolume = 0.0;
		Double snowVolume = 0.0;

		for (int cell = 0; cell < cellCount; cell++) {
			rainVolume = 0.0;
			snowVolume = 0.0;

			View view = layoutInflater.inflate(R.layout.view_forecast_item_in_linear, null, false);
			//hour, weatherIcon
			((TextView) view.findViewById(R.id.dateTime)).setText(dailyForecastDtoList.get(cell).getDate().format(dateFormatter));

			((TextView) view.findViewById(R.id.dateTime)).setTextSize(TypedValue.COMPLEX_UNIT_PX, dateTextSize);
			((TextView) view.findViewById(R.id.pop)).setTextSize(TypedValue.COMPLEX_UNIT_PX, popTextSize);

			if (dailyForecastDtoList.get(cell).getValuesList().size() == 1) {
				((ImageView) view.findViewById(R.id.leftIcon)).setImageResource(dailyForecastDtoList.get(cell).getValuesList().get(0).getWeatherIcon());
				pop = dailyForecastDtoList.get(cell).getValuesList().get(0).getPop();

				view.findViewById(R.id.rightIcon).setVisibility(View.GONE);
			} else if (dailyForecastDtoList.get(cell).getValuesList().size() == 2) {
				((ImageView) view.findViewById(R.id.leftIcon)).setImageResource(dailyForecastDtoList.get(cell).getValuesList().get(0).getWeatherIcon());
				((ImageView) view.findViewById(R.id.rightIcon)).setImageResource(dailyForecastDtoList.get(cell).getValuesList().get(1).getWeatherIcon());
				pop = dailyForecastDtoList.get(cell).getValuesList().get(0).getPop() + "/" +
						dailyForecastDtoList.get(cell).getValuesList().get(1).getPop();

			} else if (dailyForecastDtoList.get(cell).getValuesList().size() == 4) {
				((ImageView) view.findViewById(R.id.leftIcon)).setImageResource(dailyForecastDtoList.get(cell).getValuesList().get(1).getWeatherIcon());
				((ImageView) view.findViewById(R.id.rightIcon)).setImageResource(dailyForecastDtoList.get(cell).getValuesList().get(2).getWeatherIcon());
				pop = "-/-";
			}
			((TextView) view.findViewById(R.id.pop)).setText(pop);

			if (haveRain) {
				if (dailyForecastDtoList.get(cell).getValuesList().size() == 1) {
					if (dailyForecastDtoList.get(cell).getValuesList().get(0).isHasRainVolume()) {
						rainVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getValuesList().get(0).getRainVolume().replace(mm, "")
								.replace(cm, ""));
					}
				} else if (dailyForecastDtoList.get(cell).getValuesList().size() == 2) {
					if (dailyForecastDtoList.get(cell).getValuesList().get(0).isHasRainVolume()) {
						rainVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getValuesList().get(0).getRainVolume().replace(mm, "")
								.replace(cm, ""));
					}
					if (dailyForecastDtoList.get(cell).getValuesList().get(1).isHasRainVolume()) {
						rainVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getValuesList().get(1).getRainVolume().replace(mm, "")
								.replace(cm, ""));
					}
				} else if (dailyForecastDtoList.get(cell).getValuesList().size() == 4) {
					if (dailyForecastDtoList.get(cell).getValuesList().get(0).isHasPrecipitationVolume() ||
							dailyForecastDtoList.get(cell).getValuesList().get(1).isHasPrecipitationVolume()) {
						rainVolume = rainVolume + Double.parseDouble(dailyForecastDtoList.get(cell).getValuesList().get(0).getPrecipitationVolume().replace(mm
								, "")) +
								Double.parseDouble(dailyForecastDtoList.get(cell).getValuesList().get(1).getPrecipitationVolume().replace(mm
										, ""));
					}
					if (dailyForecastDtoList.get(cell).getValuesList().get(2).isHasPrecipitationVolume() ||
							dailyForecastDtoList.get(cell).getValuesList().get(3).isHasPrecipitationVolume()) {
						rainVolume = rainVolume + Double.parseDouble(dailyForecastDtoList.get(cell).getValuesList().get(2).getPrecipitationVolume().replace(mm
								, "")) + Double.parseDouble(dailyForecastDtoList.get(cell).getValuesList().get(3).getPrecipitationVolume().replace(mm
								, ""));
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
				if (dailyForecastDtoList.get(cell).getValuesList().size() == 1) {
					if (dailyForecastDtoList.get(cell).getValuesList().get(0).isHasSnowVolume()) {
						snowVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getValuesList().get(0).getSnowVolume().replace(mm, "")
								.replace(cm, ""));
					}
				} else {
					if (dailyForecastDtoList.get(cell).getValuesList().get(0).isHasSnowVolume()) {
						snowVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getValuesList().get(0).getSnowVolume().replace(mm, "")
								.replace(cm, ""));
					}
					if (dailyForecastDtoList.get(cell).getValuesList().get(1).isHasSnowVolume()) {
						snowVolume += Double.parseDouble(dailyForecastDtoList.get(cell).getValuesList().get(1).getSnowVolume().replace(mm, "")
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
		WeatherRequestUtil.initWeatherSourceUniqueValues(weatherProviderType, true, context);

		zoneId = ZoneId.of(widgetDto.getTimeZoneId());

		RemoteViews remoteViews = createRemoteViews();
		RemoteViewsUtil.onSuccessfulProcess(remoteViews);
		JsonObject jsonObject = (JsonObject) JsonParser.parseString(widgetDto.getResponseText());

		AirQualityDto airQualityDto = AqicnResponseProcessor.parseTextToAirQualityDto(jsonObject);
		CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.parseTextToCurrentConditionsDto(context, jsonObject,
				weatherProviderType, widgetDto.getLatitude(), widgetDto.getLongitude(), zoneId);

		List<DailyForecastDto> dailyForecastDtoList = WeatherResponseProcessor.parseTextToDailyForecastDtoList(context, jsonObject,
				weatherProviderType, zoneId);

		setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), airQualityDto, currentConditionsDto,
				dailyForecastDtoList, null);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(appWidgetId,
				remoteViews);
	}

	@Override
	public void setResultViews(int appWidgetId, RemoteViews remoteViews, @Nullable @org.jetbrains.annotations.Nullable WeatherRestApiDownloader weatherRestApiDownloader, ZoneId zoneId) {
		this.zoneId = zoneId;
		final WeatherProviderType weatherProviderType = WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherProviderTypeSet());

		final CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.getCurrentConditionsDto(context, weatherRestApiDownloader,
				weatherProviderType, zoneId);
		final List<DailyForecastDto> dailyForecastDtoList = WeatherResponseProcessor.getDailyForecastDtoList(context, weatherRestApiDownloader,
				weatherProviderType, zoneId);

		final boolean successful = currentConditionsDto != null && !dailyForecastDtoList.isEmpty();

		if (successful) {
			ZoneOffset zoneOffset = currentConditionsDto.getCurrentTime().getOffset();
			widgetDto.setTimeZoneId(zoneId.getId());
			widgetDto.setLastRefreshDateTime(weatherRestApiDownloader.getRequestDateTime().toString());

			AirQualityDto airQualityDto = WeatherResponseProcessor.getAirQualityDto(weatherRestApiDownloader,
					zoneOffset);


			setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), airQualityDto,
					currentConditionsDto, dailyForecastDtoList, new OnDrawBitmapCallback() {
						@Override
						public void onCreatedBitmap(Bitmap bitmap) {

						}
					});
			makeResponseTextToJson(weatherRestApiDownloader, getRequestWeatherDataTypeSet(), widgetDto.getWeatherProviderTypeSet(), widgetDto, zoneOffset);
		}

		widgetDto.setLoadSuccessful(successful);
		super.setResultViews(appWidgetId, remoteViews, weatherRestApiDownloader, zoneId);
	}
}
