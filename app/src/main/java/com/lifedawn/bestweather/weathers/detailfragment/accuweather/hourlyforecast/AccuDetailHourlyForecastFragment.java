package com.lifedawn.bestweather.weathers.detailfragment.accuweather.hourlyforecast;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.responses.accuweather.twelvehoursofhourlyforecasts.TwelveHoursOfHourlyForecastsResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.detailfragment.base.BaseDetailForecastFragment;
import com.lifedawn.bestweather.weathers.view.ClockView;
import com.lifedawn.bestweather.weathers.view.DateView;
import com.lifedawn.bestweather.weathers.view.DetailSingleTemperatureView;
import com.lifedawn.bestweather.weathers.view.FragmentType;
import com.lifedawn.bestweather.weathers.view.SingleWeatherIconView;
import com.lifedawn.bestweather.weathers.view.SingleWindDirectionView;
import com.lifedawn.bestweather.weathers.view.TextValueView;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AccuDetailHourlyForecastFragment extends BaseDetailForecastFragment {
	private List<TwelveHoursOfHourlyForecastsResponse.Item> hourlyItemList;

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.toolbar.fragmentTitle.setText(R.string.detail_hourly_forecast);
		setValuesToViews();
	}

	public void setHourlyItemList(List<TwelveHoursOfHourlyForecastsResponse.Item> hourlyItemList) {
		this.hourlyItemList = hourlyItemList;
	}

	@Override
	public void setValuesToViews() {
		//순서 : 날짜, 시각, 날씨, 기온, 체감기온, 강수확률, 강우확률, 강우량, 강설확률, 강설량, 강수형태, 강수강도
		//풍향, 풍속, 바람세기, 돌풍, 상대습도, 자외선지수, 시정거리, 이슬점, 운량
		Context context = getContext();

		final int dateRowHeight = (int) getResources().getDimension(R.dimen.dateValueRowHeightInCOMMON);
		final int clockRowHeight = (int) getResources().getDimension(R.dimen.clockValueRowHeightInCOMMON);
		final int weatherRowHeight = (int) getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInD);
		final int tempRowHeight = (int) getResources().getDimension(R.dimen.singleTemperatureRowHeightInCOMMON);
		final int windDirectionRowHeight = (int) getResources().getDimension(R.dimen.singleWindDirectionIconValueRowHeightInD);
		final int defaultTextRowHeight = (int) getResources().getDimension(R.dimen.defaultValueRowHeightInD);

		final int columnsCount = hourlyItemList.size();
		final int columnWidth = (int) getResources().getDimension(R.dimen.valueColumnWidthInDHourly);
		final int viewWidth = columnsCount * columnWidth;

		addLabelView(R.drawable.date, getString(R.string.date), dateRowHeight);
		addLabelView(R.drawable.time, getString(R.string.clock), clockRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.weather), weatherRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.temperature), tempRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.real_feel_temperature), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.probability_of_precipitation), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.probability_of_rain), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.rain_volume), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.probability_of_snow), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.snow_volume), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.precipitation_type), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.precipitation_intensity), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.wind_direction), windDirectionRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.wind_speed), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.wind_strength), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.wind_gust), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.humidity), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.uv_index), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.visibility), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.dew_point), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.cloud_cover), defaultTextRowHeight);

		dateRow = new DateView(context, FragmentType.Detail, viewWidth, dateRowHeight, columnWidth);
		ClockView clockRow = new ClockView(context, FragmentType.Detail, viewWidth, clockRowHeight, columnWidth);
		SingleWeatherIconView weatherIconRow = new SingleWeatherIconView(context, FragmentType.Detail, viewWidth, weatherRowHeight, columnWidth);
		TextValueView realFeelTempRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView popRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView probabilityOfRainRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView rainVolumeRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView probabilityOfSnowRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView snowVolumeRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView precipitationTypeRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView precipitationIntensityRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
		SingleWindDirectionView windDirectionRow = new SingleWindDirectionView(context, FragmentType.Detail, viewWidth, windDirectionRowHeight, columnWidth);
		TextValueView windSpeedRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView windStrengthRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView windGustRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView humidityRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView uvIndexRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView visibilityRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView dewPointRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView cloudCoverRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);

		List<LocalDateTime> dateTimeList = new ArrayList<>();
		List<SingleWeatherIconView.WeatherIconObj> weatherIconObjList = new ArrayList<>();
		List<Integer> tempList = new ArrayList<>();
		List<String> realFeelTempList = new ArrayList<>();
		List<String> popList = new ArrayList<>();
		List<String> probabilityOfRainList = new ArrayList<>();
		List<String> rainVolumeList = new ArrayList<>();
		List<String> probabilityOfSnowList = new ArrayList<>();
		List<String> snowVolumeList = new ArrayList<>();
		List<String> precipitationTypeList = new ArrayList<>();
		List<String> precipitationIntensityList = new ArrayList<>();
		List<SingleWindDirectionView.WindDirectionObj> windDirectionList = new ArrayList<>();
		List<String> windSpeedList = new ArrayList<>();
		List<String> windStrengthList = new ArrayList<>();
		List<String> windGustList = new ArrayList<>();
		List<String> humidityList = new ArrayList<>();
		List<String> visibilityList = new ArrayList<>();
		List<String> uvIndexList = new ArrayList<>();
		List<String> dewPointList = new ArrayList<>();
		List<String> cloudCoverList = new ArrayList<>();

		int index = 0;
		for (TwelveHoursOfHourlyForecastsResponse.Item hourly : hourlyItemList) {
			dateTimeList.add(
					WeatherResponseProcessor.convertDateTimeOfHourlyForecast(Long.parseLong(hourly.getEpochDateTime()) * 1000L, timeZone));
			weatherIconObjList.add(new SingleWeatherIconView.WeatherIconObj(WeatherSourceType.ACCU_WEATHER, hourly.getWeatherIcon(),
					dateTimeList.get(index), context));

			tempList.add(ValueUnits.convertTemperature(hourly.getTemperature().getValue(), tempUnit));
			realFeelTempList.add(ValueUnits.convertTemperature(hourly.getRealFeelTemperature().getValue(), tempUnit).toString());

			popList.add(String.valueOf((int) (Double.parseDouble(hourly.getPrecipitationProbability()) * 100.0)));
			probabilityOfRainList.add(String.valueOf((int) (Double.parseDouble(hourly.getRainProbability()) * 100.0)));
			rainVolumeList.add(hourly.getRain() == null ? "-" : hourly.getRain().getValue());
			probabilityOfSnowList.add(String.valueOf((int) (Double.parseDouble(hourly.getSnowProbability()) * 100.0)));
			snowVolumeList.add(hourly.getSnow() == null ? "-" : ValueUnits.convertCMToMM(hourly.getSnow().getValue()).toString());

			precipitationTypeList.add(hourly.getPrecipitationType());
			precipitationIntensityList.add(hourly.getPrecipitationIntensity());
			windDirectionList.add(new SingleWindDirectionView.WindDirectionObj(Integer.parseInt(hourly.getWind().getDirection().getDegrees())));
			windSpeedList.add(ValueUnits.convertWindSpeedForAccu(hourly.getWind().getSpeed().getValue(), windUnit).toString());
			windStrengthList.add(WeatherResponseProcessor.getSimpleWindSpeedDescription(hourly.getWind().getSpeed().getValue()));
			windGustList.add(hourly.getWindGust() == null ? "-" :
					ValueUnits.convertWindSpeedForAccu(hourly.getWindGust().getSpeed().getValue(), windUnit).toString());

			humidityList.add(hourly.getRelativeHumidity());
			dewPointList.add(hourly.getDewPoint().getValue());
			cloudCoverList.add(hourly.getCloudCover());
			visibilityList.add(ValueUnits.convertVisibility(hourly.getVisibility().getValue(), visibilityUnit));
			uvIndexList.add(hourly.getuVIndex());

			index++;
		}

		dateRow.init(dateTimeList);
		clockRow.setClockList(dateTimeList);
		realFeelTempRow.setValueList(realFeelTempList);
		popRow.setValueList(popList);
		probabilityOfRainRow.setValueList(probabilityOfRainList);
		rainVolumeRow.setValueList(rainVolumeList);
		probabilityOfSnowRow.setValueList(probabilityOfSnowList);
		snowVolumeRow.setValueList(snowVolumeList);
		precipitationTypeRow.setValueList(precipitationTypeList);
		precipitationIntensityRow.setValueList(precipitationIntensityList);

		windDirectionRow.setIcons(windDirectionList);
		windSpeedRow.setValueList(windSpeedList);
		windStrengthRow.setValueList(windStrengthList);
		windGustRow.setValueList(windGustList);

		humidityRow.setValueList(humidityList);
		dewPointRow.setValueList(dewPointList);
		cloudCoverRow.setValueList(cloudCoverList);
		visibilityRow.setValueList(visibilityList);
		uvIndexRow.setValueList(uvIndexList);

		//순서 : 날짜, 시각, 날씨, 기온, 체감기온, 강수확률, 강우확률, 강우량, 강설확률, 강설량, 강수형태, 강수강도
		//풍향, 풍속, 바람세기, 돌풍, 상대습도, 자외선지수, 시정거리, 이슬점, 운량
		DetailSingleTemperatureView tempRow = new DetailSingleTemperatureView(context, FragmentType.Detail, tempList, viewWidth, tempRowHeight, columnWidth);

		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		rowLayoutParams.gravity = Gravity.CENTER;

		binding.forecastView.addView(dateRow, rowLayoutParams);
		binding.forecastView.addView(clockRow, rowLayoutParams);
		binding.forecastView.addView(weatherIconRow, rowLayoutParams);
		binding.forecastView.addView(tempRow, rowLayoutParams);
		binding.forecastView.addView(realFeelTempRow, rowLayoutParams);
		binding.forecastView.addView(popRow, rowLayoutParams);
		binding.forecastView.addView(probabilityOfRainRow, rowLayoutParams);
		binding.forecastView.addView(rainVolumeRow, rowLayoutParams);
		binding.forecastView.addView(probabilityOfSnowRow, rowLayoutParams);
		binding.forecastView.addView(snowVolumeRow, rowLayoutParams);
		binding.forecastView.addView(precipitationTypeRow, rowLayoutParams);
		binding.forecastView.addView(precipitationIntensityRow, rowLayoutParams);
		binding.forecastView.addView(windDirectionRow, rowLayoutParams);
		binding.forecastView.addView(windSpeedRow, rowLayoutParams);
		binding.forecastView.addView(windStrengthRow, rowLayoutParams);
		binding.forecastView.addView(windGustRow, rowLayoutParams);
		binding.forecastView.addView(humidityRow, rowLayoutParams);
		binding.forecastView.addView(uvIndexRow, rowLayoutParams);
		binding.forecastView.addView(visibilityRow, rowLayoutParams);
		binding.forecastView.addView(dewPointRow, rowLayoutParams);
		binding.forecastView.addView(cloudCoverRow, rowLayoutParams);
	}
}
