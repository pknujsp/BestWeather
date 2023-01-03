package com.lifedawn.bestweather.ui.widget.creator;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.constants.WeatherProviderType;
import com.lifedawn.bestweather.commons.constants.WeatherDataType;
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil;
import com.lifedawn.bestweather.data.remote.retrofit.callback.WeatherRestApiDownloader;
import com.lifedawn.bestweather.ui.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.ui.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.ui.weathers.dataprocessing.util.WeatherRequestUtil;
import com.lifedawn.bestweather.ui.weathers.dataprocessing.util.WeatherUtil;
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto;
import com.lifedawn.bestweather.data.local.weather.models.CurrentConditionsDto;
import com.lifedawn.bestweather.ui.widget.OnDrawBitmapCallback;
import com.lifedawn.bestweather.ui.widget.widgetprovider.FirstWidgetProvider;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

public class FirstWidgetCreator extends AbstractWidgetCreator {


	public FirstWidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
		super(context, widgetUpdateCallback, appWidgetId);
	}


	@Override
	public Set<WeatherDataType> getRequestWeatherDataTypeSet() {
		Set<WeatherDataType> set = new HashSet<>();
		set.add(WeatherDataType.currentConditions);
		set.add(WeatherDataType.airQuality);
		return set;
	}

	@Override
	public RemoteViews createTempViews(Integer parentWidth, Integer parentHeight) {
		RemoteViews remoteViews = createBaseRemoteViews();

		RemoteViewsUtil.onSuccessfulProcess(remoteViews);

		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(),
				WeatherResponseProcessor.getTempAirQualityDto(), WeatherResponseProcessor.getTempCurrentConditionsDto(context),
				null, parentWidth, parentHeight);
		return remoteViews;
	}


	@Override
	public Class<?> widgetProviderClass() {
		return FirstWidgetProvider.class;
	}

	public void setClockTimeZone(RemoteViews remoteViews) {
		ZoneId zoneId;
		if (widgetDto.getTimeZoneId() == null) {
			zoneId = ZoneId.systemDefault();
		} else {
			zoneId = widgetDto.isDisplayLocalClock() ? ZoneId.of(widgetDto.getTimeZoneId()) : ZoneId.systemDefault();
		}

		//remoteViews.setString(R.id.dateClock, "setTimeZone", zoneId.getId());
		//remoteViews.setString(R.id.timeClock, "setTimeZone", zoneId.getId());
	}

	public View makeCurrentConditionsViews(LayoutInflater layoutInflater, CurrentConditionsDto currentConditionsDto,
	                                       AirQualityDto airQualityDto) {
		View view = layoutInflater.inflate(R.layout.view_first_widget, null, false);

		((TextView) view.findViewById(R.id.temperature)).setText(currentConditionsDto.getTemp());
		((ImageView) view.findViewById(R.id.weatherIcon)).setImageResource(currentConditionsDto.getWeatherIcon());

		String airQuality = context.getString(R.string.air_quality) + ": ";
		if (airQualityDto.isSuccessful()) {
			airQuality += AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi());
		} else {
			airQuality += context.getString(R.string.noData);
		}

		((TextView) view.findViewById(R.id.airQuality)).setText(airQuality);

		String precipitation = "";
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto.getPrecipitationVolume();
		} else {
			precipitation = context.getString(R.string.not_precipitation);
		}
		String feelsLikeTemp = context.getString(R.string.feelsLike) + ": " + currentConditionsDto.getFeelsLikeTemp();
		((TextView) view.findViewById(R.id.feelsLikeTemp)).setText(feelsLikeTemp);

		((TextView) view.findViewById(R.id.precipitation)).setText(precipitation);
		String humidity = context.getString(R.string.humidity) + ": " + currentConditionsDto.getHumidity();
		((TextView) view.findViewById(R.id.humidity)).setText(humidity);
		((TextView) view.findViewById(R.id.windDirection)).setText(currentConditionsDto.getWindDirection());
		((TextView) view.findViewById(R.id.windSpeed)).setText(currentConditionsDto.getWindSpeed());
		((TextView) view.findViewById(R.id.windStrength)).setText(currentConditionsDto.getWindStrength());
		view.findViewById(R.id.windDirectionArrow).setRotation(currentConditionsDto.getWindDirectionDegree() + 180);

		if (currentConditionsDto.getYesterdayTemp() != null) {
			String yesterdayCompText = WeatherUtil.makeTempCompareToYesterdayText(currentConditionsDto.getTemp(),
					currentConditionsDto.getYesterdayTemp(), tempUnit, context);
			((TextView) view.findViewById(R.id.yesterdayTemperature)).setText(yesterdayCompText);
		} else {
			view.findViewById(R.id.yesterdayTemperature).setVisibility(View.GONE);
		}
		return view;
	}

	public void setDataViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, AirQualityDto airQualityDto, CurrentConditionsDto currentConditionsDto,
	                         OnDrawBitmapCallback onDrawBitmapCallback) {
		drawViews(remoteViews, addressName, lastRefreshDateTime, airQualityDto, currentConditionsDto, onDrawBitmapCallback, null, null);
	}


	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, AirQualityDto airQualityDto, CurrentConditionsDto currentConditionsDto,
	                       @Nullable OnDrawBitmapCallback onDrawBitmapCallback, @Nullable Integer parentWidth, @Nullable Integer parentHeight) {
		LayoutInflater layoutInflater = LayoutInflater.from(context);

		View headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime);
		headerView.setId(R.id.header);

		View currentConditionsView = makeCurrentConditionsViews(layoutInflater, currentConditionsDto, airQualityDto);
		currentConditionsView.setId(R.id.currentConditions);

		RelativeLayout.LayoutParams headerViewLayoutParams = getHeaderViewLayoutParams();
		RelativeLayout.LayoutParams currentConditionsViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		headerViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		currentConditionsViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		currentConditionsViewLayoutParams.addRule(RelativeLayout.BELOW, R.id.header);

		RelativeLayout rootLayout = new RelativeLayout(context);

		rootLayout.addView(headerView, headerViewLayoutParams);
		rootLayout.addView(currentConditionsView, currentConditionsViewLayoutParams);

		drawBitmap(rootLayout, onDrawBitmapCallback, remoteViews, parentWidth, parentHeight);
	}


	@Override
	public void setDataViewsOfSavedData() {
		WeatherProviderType weatherProviderType = WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherProviderTypeSet());

		if (widgetDto.isTopPriorityKma() && widgetDto.getCountryCode().equals("KR")) {
			weatherProviderType = WeatherProviderType.KMA_WEB;
		}
		WeatherRequestUtil.initWeatherSourceUniqueValues(weatherProviderType, true, context);

		RemoteViews remoteViews = createRemoteViews();

		zoneId = ZoneId.of(widgetDto.getTimeZoneId());

		JsonObject jsonObject = (JsonObject) JsonParser.parseString(widgetDto.getResponseText());
		AirQualityDto airQualityDto = AqicnResponseProcessor.parseTextToAirQualityDto(jsonObject);
		CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.parseTextToCurrentConditionsDto(context, jsonObject,
				weatherProviderType, widgetDto.getLatitude(), widgetDto.getLongitude(), zoneId);

		setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), airQualityDto, currentConditionsDto, null);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		RemoteViewsUtil.onSuccessfulProcess(remoteViews);

		appWidgetManager.updateAppWidget(appWidgetId,
				remoteViews);
	}

	@Override
	public void setResultViews(int appWidgetId, @Nullable @org.jetbrains.annotations.Nullable WeatherRestApiDownloader weatherRestApiDownloader, ZoneId zoneId) {
		this.zoneId = zoneId;
		ZoneOffset zoneOffset = null;

		final WeatherProviderType weatherProviderType = WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherProviderTypeSet());
		final CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.getCurrentConditionsDto(context, weatherRestApiDownloader,
				weatherProviderType, zoneId);

		boolean successful = currentConditionsDto != null;

		if (successful) {
			widgetDto.setLastRefreshDateTime(weatherRestApiDownloader.getRequestDateTime().toString());
			zoneOffset = currentConditionsDto.getCurrentTime().getOffset();

			widgetDto.setTimeZoneId(zoneId.getId());

			makeResponseTextToJson(weatherRestApiDownloader, getRequestWeatherDataTypeSet(), widgetDto.getWeatherProviderTypeSet(), widgetDto, zoneOffset);
		}

		widgetDto.setLoadSuccessful(successful);

		super.setResultViews(appWidgetId, weatherRestApiDownloader, zoneId);
	}

	@Override
	public void setDisplayClock(boolean displayClock) {
		widgetDto.setDisplayClock(displayClock);
	}

}
