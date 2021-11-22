package com.lifedawn.bestweather.weathers.detailfragment.accuweather.hourlyforecast;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.interfaces.OnClickedListViewItemListener;
import com.lifedawn.bestweather.retrofit.responses.accuweather.twelvehoursofhourlyforecasts.TwelveHoursOfHourlyForecastsResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
	}

	public void setHourlyItemList(List<TwelveHoursOfHourlyForecastsResponse.Item> hourlyItemList) {
		this.hourlyItemList = hourlyItemList;
	}


	@Override
	protected void setDataViewsByList() {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				Context context = getContext();
				String tempDegree = getString(R.string.degree_symbol);
				String percent = ValueUnits.convertToStr(context, ValueUnits.percent);
				String mm = ValueUnits.convertToStr(context, ValueUnits.mm);
				List<HourlyForecastListItemObj> hourlyForecastListItemObjs = new ArrayList<>();
				DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(clockUnit == ValueUnits.clock12 ?
						getString(R.string.datetime_pattern_in_detail_forecast_clock12) :
						getString(R.string.datetime_pattern_in_detail_forecast_clock24));

				final String zero = "0";

				for (TwelveHoursOfHourlyForecastsResponse.Item hourly : hourlyItemList) {
					HourlyForecastListItemObj item = new HourlyForecastListItemObj();

					item.setDateTime(
							WeatherResponseProcessor.convertDateTimeOfHourlyForecast(Long.parseLong(hourly.getEpochDateTime()) * 1000L,
									zoneId).format(dateTimeFormatter))
							.setTemp(ValueUnits.convertTemperature(hourly.getTemperature().getValue(), tempUnit) + tempDegree)
							.setWeatherIconId(AccuWeatherResponseProcessor.getWeatherIconImg(hourly.getWeatherIcon()))
							.setPop((int) (Double.parseDouble(hourly.getPrecipitationProbability())) + percent)
							.setRainVolume(hourly.getRain().getValue().equals(zero) ? null : hourly.getRain().getValue() + mm)
							.setSnowVolume(hourly.getSnow().getValue().equals(zero) ? null :
									ValueUnits.convertCMToMM(hourly.getSnow().getValue()) + mm);
					
					hourlyForecastListItemObjs.add(item);
				}

				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							HourlyForecastListAdapter adapter = new HourlyForecastListAdapter(getContext(), new OnClickedListViewItemListener<Integer>() {
								@Override
								public void onClickedItem(Integer position) {

								}
							});
							adapter.setHourlyForecastListItemObjs(hourlyForecastListItemObjs);
							binding.listview.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
							binding.listview.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
							binding.listview.setAdapter(adapter);
						}
					});
				}
			}
		});
	}

	@Override
	protected void setDataViewsByTable() {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
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


				dateRow = new DateView(context, FragmentType.Detail, viewWidth, dateRowHeight, columnWidth);
				ClockView clockRow = new ClockView(context, FragmentType.Detail, viewWidth, clockRowHeight, columnWidth);
				SingleWeatherIconView weatherIconRow = new SingleWeatherIconView(context, FragmentType.Detail, viewWidth, weatherRowHeight,
						columnWidth);
				TextValueView realFeelTempRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
				TextValueView popRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
				TextValueView probabilityOfRainRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
				TextValueView rainVolumeRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
				TextValueView probabilityOfSnowRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
				TextValueView snowVolumeRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
				TextValueView precipitationTypeRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
				TextValueView precipitationIntensityRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight,
						columnWidth);
				SingleWindDirectionView windDirectionRow = new SingleWindDirectionView(context, FragmentType.Detail, viewWidth,
						windDirectionRowHeight, columnWidth);
				TextValueView windSpeedRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
				TextValueView windStrengthRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
				TextValueView windGustRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
				TextValueView humidityRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
				TextValueView uvIndexRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
				TextValueView visibilityRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
				TextValueView dewPointRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
				TextValueView cloudCoverRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);

				List<ZonedDateTime> dateTimeList = new ArrayList<>();
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
				List<Integer> windDirectionList = new ArrayList<>();
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
							WeatherResponseProcessor.convertDateTimeOfHourlyForecast(Long.parseLong(hourly.getEpochDateTime()) * 1000L, zoneId));
					weatherIconObjList.add(new SingleWeatherIconView.WeatherIconObj(
							ContextCompat.getDrawable(context, AccuWeatherResponseProcessor.getWeatherIconImg(hourly.getWeatherIcon()))));

					tempList.add(ValueUnits.convertTemperature(hourly.getTemperature().getValue(), tempUnit));
					realFeelTempList.add(ValueUnits.convertTemperature(hourly.getRealFeelTemperature().getValue(), tempUnit).toString());

					popList.add(String.valueOf((int) (Double.parseDouble(hourly.getPrecipitationProbability()))));
					probabilityOfRainList.add(String.valueOf((int) (Double.parseDouble(hourly.getRainProbability()))));
					rainVolumeList.add(hourly.getRain() == null ? "-" : hourly.getRain().getValue());
					probabilityOfSnowList.add(String.valueOf((int) (Double.parseDouble(hourly.getSnowProbability()))));
					snowVolumeList.add(hourly.getSnow() == null ? "-" : ValueUnits.convertCMToMM(hourly.getSnow().getValue()).toString());

					precipitationTypeList.add(hourly.getPrecipitationType());
					precipitationIntensityList.add(hourly.getPrecipitationIntensity());
					windDirectionList.add(Integer.parseInt(hourly.getWind().getDirection().getDegrees()));
					windSpeedList.add(ValueUnits.convertWindSpeedForAccu(hourly.getWind().getSpeed().getValue(), windUnit).toString());
					windStrengthList.add(WeatherResponseProcessor.getSimpleWindSpeedDescription(hourly.getWind().getSpeed().getValue()));
					windGustList.add(
							hourly.getWindGust() == null ? "-" : ValueUnits.convertWindSpeedForAccu(hourly.getWindGust().getSpeed().getValue(),
									windUnit).toString());

					humidityList.add(hourly.getRelativeHumidity());
					dewPointList.add(hourly.getDewPoint().getValue());
					cloudCoverList.add(hourly.getCloudCover());
					visibilityList.add(ValueUnits.convertVisibility(hourly.getVisibility().getValue(), visibilityUnit));
					uvIndexList.add(hourly.getuVIndex());

					index++;
				}

				weatherIconRow.setWeatherImgs(weatherIconObjList);
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

				windDirectionRow.setWindDirectionObjList(windDirectionList);
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
				DetailSingleTemperatureView tempRow = new DetailSingleTemperatureView(context, FragmentType.Detail, tempList, viewWidth,
						tempRowHeight, columnWidth);


				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							addLabelView(R.drawable.date, getString(R.string.date), dateRowHeight);
							addLabelView(R.drawable.time, getString(R.string.clock), clockRowHeight);
							addLabelView(R.drawable.day_clear, getString(R.string.weather), weatherRowHeight);
							addLabelView(R.drawable.temperature, getString(R.string.temperature), tempRowHeight);
							addLabelView(R.drawable.realfeeltemperature, getString(R.string.real_feel_temperature), defaultTextRowHeight);
							addLabelView(R.drawable.pop, getString(R.string.probability_of_precipitation), defaultTextRowHeight);
							addLabelView(R.drawable.por, getString(R.string.probability_of_rain), defaultTextRowHeight);
							addLabelView(R.drawable.rainvolume, getString(R.string.rain_volume), defaultTextRowHeight);
							addLabelView(R.drawable.pos, getString(R.string.probability_of_snow), defaultTextRowHeight);
							addLabelView(R.drawable.snowvolume, getString(R.string.snow_volume), defaultTextRowHeight);
							addLabelView(R.drawable.temp_icon, getString(R.string.precipitation_type), defaultTextRowHeight);
							addLabelView(R.drawable.temp_icon, getString(R.string.precipitation_intensity), defaultTextRowHeight);
							addLabelView(R.drawable.winddirection, getString(R.string.wind_direction), windDirectionRowHeight);
							addLabelView(R.drawable.windspeed, getString(R.string.wind_speed), defaultTextRowHeight);
							addLabelView(R.drawable.windstrength, getString(R.string.wind_strength), defaultTextRowHeight);
							addLabelView(R.drawable.windgust, getString(R.string.wind_gust), defaultTextRowHeight);
							addLabelView(R.drawable.humidity, getString(R.string.humidity), defaultTextRowHeight);
							addLabelView(R.drawable.uv, getString(R.string.uv_index), defaultTextRowHeight);
							addLabelView(R.drawable.visibility, getString(R.string.visibility), defaultTextRowHeight);
							addLabelView(R.drawable.dewpoint, getString(R.string.dew_point), defaultTextRowHeight);
							addLabelView(R.drawable.cloudiness, getString(R.string.cloud_cover), defaultTextRowHeight);

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
					});
				}
			}
		});

	}

}
