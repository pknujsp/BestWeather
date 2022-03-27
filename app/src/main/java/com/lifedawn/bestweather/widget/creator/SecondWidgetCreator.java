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
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;
import com.lifedawn.bestweather.weathers.view.DetailSingleTemperatureView;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;
import com.lifedawn.bestweather.widget.widgetprovider.SecondWidgetProvider;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SecondWidgetCreator extends AbstractWidgetCreator {
	private final DateTimeFormatter refreshDateTimeFormatter;

	private int addressTextSize;
	private int refreshDateTimeTextSize;
	private int hourTextSize;
	private int tempTextSize;
	private int popTextSize;
	private int rainVolumeTextSize;
	private int snowVolumeTextSize;
	private int currentPrecipitationTextSize;
	private int currentAirQualityTextSize;
	private int currentLabelTextSize;
	private int currentTempTextSize;
	private int currentAirQualityLabelTextSize;

	private final int cellCount = 7;

	public SecondWidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
		super(context, widgetUpdateCallback, appWidgetId);
		refreshDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a h:mm");
	}

	@Override
	public Set<WeatherDataType> getRequestWeatherDataTypeSet() {
		Set<WeatherDataType> set = new HashSet<>();
		set.add(WeatherDataType.currentConditions);
		set.add(WeatherDataType.hourlyForecast);
		set.add(WeatherDataType.airQuality);

		return set;
	}

	@Override
	public RemoteViews createTempViews(Integer parentWidth, Integer parentHeight) {
		RemoteViews remoteViews = createBaseRemoteViews();
		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(), WeatherResponseProcessor.getTempAirQualityDto(),
				WeatherResponseProcessor.getTempCurrentConditionsDto(context),
				WeatherResponseProcessor.getTempHourlyForecastDtoList(context, cellCount), null, parentWidth, parentHeight);
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
		return SecondWidgetProvider.class;
	}

	@Override
	public void setTextSize(int amount) {
		final int absSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, Math.abs(amount),
				context.getResources().getDisplayMetrics());
		final int extraSize = amount >= 0 ? absSize : absSize * -1;

		addressTextSize = context.getResources().getDimensionPixelSize(R.dimen.addressTextSizeInCommonWidgetHeader) + extraSize;
		refreshDateTimeTextSize = context.getResources().getDimensionPixelSize(R.dimen.refreshDateTimeTextSizeInCommonWidgetHeader) + extraSize;
		tempTextSize = context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInSimpleWidgetForecastItem) + extraSize;
		hourTextSize = context.getResources().getDimensionPixelSize(R.dimen.dateTimeTextSizeInSimpleWidgetForecastItem) + extraSize;
		popTextSize = context.getResources().getDimensionPixelSize(R.dimen.popTextSizeInSimpleWidgetForecastItem) + extraSize;
		rainVolumeTextSize = context.getResources().getDimensionPixelSize(R.dimen.rainVolumeTextSizeInSimpleWidgetForecastItem) + extraSize;
		snowVolumeTextSize = context.getResources().getDimensionPixelSize(R.dimen.snowVolumeTextSizeInSimpleWidgetForecastItem) + extraSize;
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
	                         List<HourlyForecastDto> hourlyForecastDtoList, OnDrawBitmapCallback onDrawBitmapCallback) {
		drawViews(remoteViews, addressName, lastRefreshDateTime, airQualityDto, currentConditionsDto, hourlyForecastDtoList,
				onDrawBitmapCallback, null, null);
	}


	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, AirQualityDto airQualityDto, CurrentConditionsDto currentConditionsDto,
	                       List<HourlyForecastDto> hourlyForecastDtoList, @Nullable OnDrawBitmapCallback onDrawBitmapCallback,
	                       @Nullable Integer parentWidth, @Nullable Integer parentHeight) {
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

		final String mm = "mm";
		final String cm = "cm";
		boolean haveRain = false;
		boolean haveSnow = false;

		for (int cell = 0; cell < cellCount; cell++) {
			if (hourlyForecastDtoList.get(cell).isHasRain()) {
				haveRain = true;
			}
			if (hourlyForecastDtoList.get(cell).isHasSnow()) {
				haveSnow = true;
			}
		}

		List<Integer> tempList = new ArrayList<>();
		final String degree = tempDegree;
		DateTimeFormatter hour0Formatter = DateTimeFormatter.ofPattern("E 0");

		for (int cell = 0; cell < cellCount; cell++) {
			View view = layoutInflater.inflate(R.layout.view_forecast_item_in_linear, null, false);
			//hour, weatherIcon, pop
			if (hourlyForecastDtoList.get(cell).getHours().getHour() == 0) {
				((TextView) view.findViewById(R.id.dateTime)).setText(hourlyForecastDtoList.get(cell).getHours().format(hour0Formatter));
			} else {
				((TextView) view.findViewById(R.id.dateTime)).setText(String.valueOf(hourlyForecastDtoList.get(cell).getHours().getHour()));
			}
			((ImageView) view.findViewById(R.id.leftIcon)).setImageResource(hourlyForecastDtoList.get(cell).getWeatherIcon());
			tempList.add(Integer.parseInt(hourlyForecastDtoList.get(cell).getTemp().replace(degree, "")));

			((TextView) view.findViewById(R.id.pop)).setText(hourlyForecastDtoList.get(cell).getPop());

			if (haveRain) {
				if (hourlyForecastDtoList.get(cell).isHasRain()) {
					((TextView) view.findViewById(R.id.rainVolume)).setText(hourlyForecastDtoList.get(cell).getRainVolume().replace(mm
							, "").replace(cm, ""));
					((TextView) view.findViewById(R.id.rainVolume)).setTextSize(TypedValue.COMPLEX_UNIT_PX, rainVolumeTextSize);
				} else {
					view.findViewById(R.id.rainVolumeLayout).setVisibility(View.INVISIBLE);
				}
			} else {
				view.findViewById(R.id.rainVolumeLayout).setVisibility(View.GONE);
			}

			if (haveSnow) {
				if (hourlyForecastDtoList.get(cell).isHasSnow()) {
					((TextView) view.findViewById(R.id.snowVolume)).setText(hourlyForecastDtoList.get(cell).getSnowVolume().replace(mm
							, "").replace(cm, ""));
					((TextView) view.findViewById(R.id.snowVolume)).setTextSize(TypedValue.COMPLEX_UNIT_PX, snowVolumeTextSize);
				} else {
					view.findViewById(R.id.snowVolumeLayout).setVisibility(View.INVISIBLE);
				}
			} else {
				view.findViewById(R.id.snowVolumeLayout).setVisibility(View.GONE);
			}

			((TextView) view.findViewById(R.id.dateTime)).setTextSize(TypedValue.COMPLEX_UNIT_PX, hourTextSize);
			((TextView) view.findViewById(R.id.pop)).setTextSize(TypedValue.COMPLEX_UNIT_PX, popTextSize);

			view.findViewById(R.id.temperature).setVisibility(View.GONE);
			view.findViewById(R.id.rightIcon).setVisibility(View.GONE);

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

		DetailSingleTemperatureView detailSingleTemperatureView = new DetailSingleTemperatureView(context, tempList);
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
		List<HourlyForecastDto> hourlyForecastDtoList = WeatherResponseProcessor.parseTextToHourlyForecastDtoList(context, jsonObject,
				weatherProviderType, widgetDto.getLatitude(), widgetDto.getLongitude());

		setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), airQualityDto, currentConditionsDto,
				hourlyForecastDtoList, null);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(appWidgetId,
				remoteViews);
	}


	@Override
	public void setResultViews(int appWidgetId, RemoteViews remoteViews, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader) {
		ZoneId zoneId = null;
		ZoneOffset zoneOffset = null;

		final WeatherProviderType weatherProviderType = WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherProviderTypeSet());

		final CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.getCurrentConditionsDto(context, multipleRestApiDownloader,
				weatherProviderType);
		final List<HourlyForecastDto> hourlyForecastDtoList = WeatherResponseProcessor.getHourlyForecastDtoList(context, multipleRestApiDownloader,
				weatherProviderType);

		final boolean successful = currentConditionsDto != null && !hourlyForecastDtoList.isEmpty();

		if (successful) {
			zoneId = currentConditionsDto.getCurrentTime().getZone();
			zoneOffset = currentConditionsDto.getCurrentTime().getOffset();
			widgetDto.setTimeZoneId(zoneId.getId());
			widgetDto.setLastRefreshDateTime(multipleRestApiDownloader.getRequestDateTime().toString());

			AirQualityDto airQualityDto = WeatherResponseProcessor.getAirQualityDto(context, multipleRestApiDownloader,
					zoneOffset);

			setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), airQualityDto,
					currentConditionsDto, hourlyForecastDtoList, new OnDrawBitmapCallback() {
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
