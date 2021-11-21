package com.lifedawn.bestweather.weathers.detailfragment.accuweather.dailyforecast;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.retrofit.responses.accuweather.fivedaysofdailyforecasts.FiveDaysOfDailyForecastsResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.detailfragment.base.BaseDetailForecastFragment;
import com.lifedawn.bestweather.weathers.view.DetailDoubleTemperatureView;
import com.lifedawn.bestweather.weathers.view.DoubleWeatherIconView;
import com.lifedawn.bestweather.weathers.view.DoubleWindDirectionView;
import com.lifedawn.bestweather.weathers.view.FragmentType;
import com.lifedawn.bestweather.weathers.view.TextValueView;

import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccuDetailDailyForecastFragment extends BaseDetailForecastFragment {
	private List<FiveDaysOfDailyForecastsResponse.DailyForecasts> dailyForecastsList;

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.toolbar.fragmentTitle.setText(R.string.detail_daily_forecast);
	}

	public AccuDetailDailyForecastFragment setDailyForecastsList(List<FiveDaysOfDailyForecastsResponse.DailyForecasts> dailyForecastsList) {
		this.dailyForecastsList = dailyForecastsList;
		return this;
	}

	@Override
	protected void setDataViewsByList() {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(getString(R.string.date_pattern));
				Context context = getContext();

				for (FiveDaysOfDailyForecastsResponse.DailyForecasts daily : dailyForecastsList) {
					// 	WeatherResponseProcessor.convertDateTimeOfDailyForecast(Long.parseLong(daily.getEpochDate()) * 1000L, zoneId).format(dateTimeFormatter)


					weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(
							ContextCompat.getDrawable(context, AccuWeatherResponseProcessor.getWeatherIconImg(daily.getDay().getIcon())),
							ContextCompat.getDrawable(context, AccuWeatherResponseProcessor.getWeatherIconImg(daily.getNight().getIcon()))));
					minTempList.add(ValueUnits.convertTemperature(daily.getTemperature().getMinimum().getValue(), tempUnit));
					maxTempList.add(ValueUnits.convertTemperature(daily.getTemperature().getMaximum().getValue(), tempUnit));

					popList.add(getDayNightValueStr(daily.getDay().getPrecipitationProbability(), daily.getNight().getPrecipitationProbability()));
					precipitationVolumeList.add(
							getDayNightValueStr(daily.getDay().getTotalLiquid().getValue(), daily.getNight().getTotalLiquid().getValue()));

					precipitationOfRainList.add(getDayNightValueStr(daily.getDay().getRainProbability(), daily.getNight().getRainProbability()));
					rainVolumeList.add(getDayNightValueStr(daily.getDay().getRain().getValue(), daily.getNight().getRain().getValue()));

					precipitationOfSnowList.add(getDayNightValueStr(daily.getDay().getSnowProbability(), daily.getNight().getSnowProbability()));
					snowVolumeList.add(getDayNightValueStr(ValueUnits.convertCMToMM(daily.getDay().getSnow().getValue()).toString(),
							ValueUnits.convertCMToMM(daily.getNight().getSnow().getValue()).toString()));

					windDirectionList.add(
							new DoubleWindDirectionView.WindDirectionObj(Integer.parseInt(daily.getDay().getWind().getDirection().getDegrees()),
									Integer.parseInt(daily.getNight().getWind().getDirection().getDegrees())));
					windSpeedList.add(getDayNightValueStr(
							ValueUnits.convertWindSpeedForAccu(daily.getDay().getWind().getSpeed().getValue(), windUnit).toString(),
							ValueUnits.convertWindSpeedForAccu(daily.getNight().getWind().getSpeed().getValue(), windUnit).toString()));
					windStrengthList.add(getDayNightValueStr(
							WeatherResponseProcessor.getSimpleWindSpeedDescription(daily.getDay().getWind().getSpeed().getValue()),
							WeatherResponseProcessor.getSimpleWindSpeedDescription(daily.getNight().getWind().getSpeed().getValue())));
					windGustList.add(getDayNightValueStr(
							ValueUnits.convertWindSpeedForAccu(daily.getDay().getWindGust().getSpeed().getValue(), windUnit).toString(),
							ValueUnits.convertWindSpeedForAccu(daily.getNight().getWindGust().getSpeed().getValue(), windUnit).toString()));

					hoursOfPrecipitationList.add(
							getDayNightValueStr(daily.getDay().getHoursOfPrecipitation(), daily.getNight().getHoursOfPrecipitation()));
					hoursOfRainList.add(getDayNightValueStr(daily.getDay().getHoursOfRain(), daily.getNight().getHoursOfRain()));
					cloudinessList.add(getDayNightValueStr(daily.getDay().getCloudCover(), daily.getNight().getCloudCover()));
				}

			}
		});
	}

	@Override
	protected void setDataViewsByTable() {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				// 최저/최고기온,최저/최고체감기온,(낮과 밤의날씨상태,강수여부,강수형태,강수강도,강수확률(nullable),천둥번개확률(nullable)
				// 강우확률(nullable),강설확률(nullable),풍속(nullable),풍향,돌풍(nullable)
				// 강우량(nullable),강설량(nullable),비내리는시간(강우시간),강수시간,운량,강수량)

				//accu 순서 : 날짜, 낮/밤의 날씨, 최저/최고 기온, 낮/밤의 강수확률, 강수량, 강우확률, 강우량, 강설확률, 강설량
				//풍향, 풍속, 바람세기, 돌풍, 강수지속시간, 강우지속시간, 운량
				final int dateRowHeight = (int) getResources().getDimension(R.dimen.dateValueRowHeightInCOMMON);
				final int weatherRowHeight = (int) getResources().getDimension(R.dimen.doubleWeatherIconValueRowHeightInD);
				final int tempRowHeight = (int) getResources().getDimension(R.dimen.doubleTemperatureRowHeightInD);
				final int windDirectionRowHeight = (int) getResources().getDimension(R.dimen.doubleWindDirectionIconValueRowHeightInD);
				final int defaultTextRowHeight = (int) getResources().getDimension(R.dimen.defaultValueRowHeightInD);

				final int columnsCount = dailyForecastsList.size();
				final int columnWidth = (int) getResources().getDimension(R.dimen.valueColumnWidthInDDaily);
				final int viewWidth = columnsCount * columnWidth;
				DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(getString(R.string.date_pattern));

				List<String> dateList = new ArrayList<>();
				List<DoubleWeatherIconView.WeatherIconObj> weatherIconObjList = new ArrayList<>();
				List<Integer> minTempList = new ArrayList<>();
				List<Integer> maxTempList = new ArrayList<>();
				List<String> popList = new ArrayList<>();
				List<String> precipitationVolumeList = new ArrayList<>();
				List<String> precipitationOfRainList = new ArrayList<>();
				List<String> rainVolumeList = new ArrayList<>();
				List<String> precipitationOfSnowList = new ArrayList<>();
				List<String> snowVolumeList = new ArrayList<>();
				List<DoubleWindDirectionView.WindDirectionObj> windDirectionList = new ArrayList<>();
				List<String> windSpeedList = new ArrayList<>();
				List<String> windStrengthList = new ArrayList<>();
				List<String> windGustList = new ArrayList<>();
				List<String> hoursOfPrecipitationList = new ArrayList<>();
				List<String> hoursOfRainList = new ArrayList<>();
				List<String> cloudinessList = new ArrayList<>();

				Context context = getContext();

				for (FiveDaysOfDailyForecastsResponse.DailyForecasts daily : dailyForecastsList) {
					dateList.add(
							WeatherResponseProcessor.convertDateTimeOfDailyForecast(Long.parseLong(daily.getEpochDate()) * 1000L, zoneId).format(
									dateTimeFormatter));
					weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(
							ContextCompat.getDrawable(context, AccuWeatherResponseProcessor.getWeatherIconImg(daily.getDay().getIcon())),
							ContextCompat.getDrawable(context, AccuWeatherResponseProcessor.getWeatherIconImg(daily.getNight().getIcon()))));
					minTempList.add(ValueUnits.convertTemperature(daily.getTemperature().getMinimum().getValue(), tempUnit));
					maxTempList.add(ValueUnits.convertTemperature(daily.getTemperature().getMaximum().getValue(), tempUnit));

					popList.add(getDayNightValueStr(daily.getDay().getPrecipitationProbability(), daily.getNight().getPrecipitationProbability()));
					precipitationVolumeList.add(
							getDayNightValueStr(daily.getDay().getTotalLiquid().getValue(), daily.getNight().getTotalLiquid().getValue()));

					precipitationOfRainList.add(getDayNightValueStr(daily.getDay().getRainProbability(), daily.getNight().getRainProbability()));
					rainVolumeList.add(getDayNightValueStr(daily.getDay().getRain().getValue(), daily.getNight().getRain().getValue()));

					precipitationOfSnowList.add(getDayNightValueStr(daily.getDay().getSnowProbability(), daily.getNight().getSnowProbability()));
					snowVolumeList.add(getDayNightValueStr(ValueUnits.convertCMToMM(daily.getDay().getSnow().getValue()).toString(),
							ValueUnits.convertCMToMM(daily.getNight().getSnow().getValue()).toString()));

					windDirectionList.add(
							new DoubleWindDirectionView.WindDirectionObj(Integer.parseInt(daily.getDay().getWind().getDirection().getDegrees()),
									Integer.parseInt(daily.getNight().getWind().getDirection().getDegrees())));
					windSpeedList.add(getDayNightValueStr(
							ValueUnits.convertWindSpeedForAccu(daily.getDay().getWind().getSpeed().getValue(), windUnit).toString(),
							ValueUnits.convertWindSpeedForAccu(daily.getNight().getWind().getSpeed().getValue(), windUnit).toString()));
					windStrengthList.add(getDayNightValueStr(
							WeatherResponseProcessor.getSimpleWindSpeedDescription(daily.getDay().getWind().getSpeed().getValue()),
							WeatherResponseProcessor.getSimpleWindSpeedDescription(daily.getNight().getWind().getSpeed().getValue())));
					windGustList.add(getDayNightValueStr(
							ValueUnits.convertWindSpeedForAccu(daily.getDay().getWindGust().getSpeed().getValue(), windUnit).toString(),
							ValueUnits.convertWindSpeedForAccu(daily.getNight().getWindGust().getSpeed().getValue(), windUnit).toString()));

					hoursOfPrecipitationList.add(
							getDayNightValueStr(daily.getDay().getHoursOfPrecipitation(), daily.getNight().getHoursOfPrecipitation()));
					hoursOfRainList.add(getDayNightValueStr(daily.getDay().getHoursOfRain(), daily.getNight().getHoursOfRain()));
					cloudinessList.add(getDayNightValueStr(daily.getDay().getCloudCover(), daily.getNight().getCloudCover()));
				}

				addLabelView(R.drawable.date, getString(R.string.date), dateRowHeight);
				addLabelView(R.drawable.day_clear, getString(R.string.weather), weatherRowHeight);
				addLabelView(R.drawable.temperature, getString(R.string.temperature), tempRowHeight);
				addLabelView(R.drawable.realfeeltemperature, getString(R.string.probability_of_precipitation), defaultTextRowHeight);
				addLabelView(R.drawable.precipitationvolume, getString(R.string.precipitation_volume), defaultTextRowHeight);
				addLabelView(R.drawable.por, getString(R.string.precipitation_of_rain), defaultTextRowHeight);
				addLabelView(R.drawable.rainvolume, getString(R.string.rain_volume), defaultTextRowHeight);
				addLabelView(R.drawable.pos, getString(R.string.precipitation_of_snow), defaultTextRowHeight);
				addLabelView(R.drawable.snowvolume, getString(R.string.snow_volume), defaultTextRowHeight);
				addLabelView(R.drawable.winddirection, getString(R.string.wind_direction), windDirectionRowHeight);
				addLabelView(R.drawable.windspeed, getString(R.string.wind_speed), defaultTextRowHeight);
				addLabelView(R.drawable.windstrength, getString(R.string.wind_strength), defaultTextRowHeight);
				addLabelView(R.drawable.windgust, getString(R.string.wind_gust), defaultTextRowHeight);
				addLabelView(R.drawable.temp_icon, getString(R.string.hours_of_precipitation), defaultTextRowHeight);
				addLabelView(R.drawable.temp_icon, getString(R.string.hours_of_rain), defaultTextRowHeight);
				addLabelView(R.drawable.cloudiness, getString(R.string.cloud_cover), defaultTextRowHeight);

				//accu 순서 : 날짜, 낮/밤의 날씨, 최저/최고 기온, 낮/밤의 강수확률, 강수량, 강우확률, 강우량, 강설확률, 강설량
				//풍향, 풍속, 바람세기, 돌풍, 강수지속시간, 강우지속시간, 운량

				TextValueView dateRow = new TextValueView(context, FragmentType.Detail, viewWidth, dateRowHeight, columnWidth);
				DoubleWeatherIconView weatherIconRow = new DoubleWeatherIconView(context, FragmentType.Detail, viewWidth, weatherRowHeight,
						columnWidth);
				DetailDoubleTemperatureView tempRow = new DetailDoubleTemperatureView(context, FragmentType.Detail, viewWidth, tempRowHeight,
						columnWidth, minTempList, maxTempList);
				TextValueView popRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
				TextValueView precipitationVolumeRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight,
						columnWidth);
				TextValueView precipitationOfRainRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight,
						columnWidth);
				TextValueView rainVolumeRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
				TextValueView precipitationOfSnowRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight,
						columnWidth);
				TextValueView snowVolumeRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
				DoubleWindDirectionView windDirectionRow = new DoubleWindDirectionView(context, FragmentType.Detail, viewWidth,
						windDirectionRowHeight, columnWidth);
				TextValueView windSpeedRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
				TextValueView windStrengthRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
				TextValueView windGustRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
				TextValueView hoursOfPrecipitationRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight,
						columnWidth);
				TextValueView hoursOfRainRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
				TextValueView cloudCoverRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);

				dateRow.setValueList(dateList);
				weatherIconRow.setIcons(weatherIconObjList);
				popRow.setValueList(popList);
				rainVolumeRow.setValueList(rainVolumeList);
				snowVolumeRow.setValueList(snowVolumeList);
				windDirectionRow.setIcons(windDirectionList);
				windSpeedRow.setValueList(windSpeedList);
				windStrengthRow.setValueList(windStrengthList);
				windGustRow.setValueList(windGustList);
				cloudCoverRow.setValueList(cloudinessList);
				precipitationVolumeRow.setValueList(precipitationVolumeList);
				precipitationOfRainRow.setValueList(precipitationOfRainList);
				precipitationOfSnowRow.setValueList(precipitationOfSnowList);
				hoursOfPrecipitationRow.setValueList(hoursOfPrecipitationList);
				hoursOfRainRow.setValueList(hoursOfRainList);

				LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				rowLayoutParams.gravity = Gravity.CENTER;

				binding.forecastView.addView(dateRow, rowLayoutParams);
				binding.forecastView.addView(weatherIconRow, rowLayoutParams);
				binding.forecastView.addView(tempRow, rowLayoutParams);
				binding.forecastView.addView(popRow, rowLayoutParams);
				binding.forecastView.addView(precipitationVolumeRow, rowLayoutParams);
				binding.forecastView.addView(precipitationOfRainRow, rowLayoutParams);
				binding.forecastView.addView(rainVolumeRow, rowLayoutParams);
				binding.forecastView.addView(precipitationOfSnowRow, rowLayoutParams);
				binding.forecastView.addView(snowVolumeRow, rowLayoutParams);
				binding.forecastView.addView(windDirectionRow, rowLayoutParams);
				binding.forecastView.addView(windSpeedRow, rowLayoutParams);
				binding.forecastView.addView(windStrengthRow, rowLayoutParams);
				binding.forecastView.addView(windGustRow, rowLayoutParams);
				binding.forecastView.addView(hoursOfPrecipitationRow, rowLayoutParams);
				binding.forecastView.addView(hoursOfRainRow, rowLayoutParams);
				binding.forecastView.addView(cloudCoverRow, rowLayoutParams);
			}
		});
	}


	private String getDayNightValueStr(String day, String night) {
		String v = "";
		if (day != null) {
			v += day + " / ";
		} else {
			v += " / ";
		}
		if (night != null) {
			v += night;
		} else {
			v += "";
		}
		return v;
	}
}
