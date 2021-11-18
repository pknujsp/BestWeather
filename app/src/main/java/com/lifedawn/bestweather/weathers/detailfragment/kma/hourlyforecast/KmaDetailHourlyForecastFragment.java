package com.lifedawn.bestweather.weathers.detailfragment.kma.hourlyforecast;

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
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.detailfragment.base.BaseDetailForecastFragment;
import com.lifedawn.bestweather.weathers.view.ClockView;
import com.lifedawn.bestweather.weathers.view.DateView;
import com.lifedawn.bestweather.weathers.view.DetailSingleTemperatureView;
import com.lifedawn.bestweather.weathers.view.FragmentType;
import com.lifedawn.bestweather.weathers.view.TextValueView;
import com.lifedawn.bestweather.weathers.view.SingleWeatherIconView;
import com.lifedawn.bestweather.weathers.view.SingleWindDirectionView;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class KmaDetailHourlyForecastFragment extends BaseDetailForecastFragment {
	private List<FinalHourlyForecast> finalHourlyForecastList;
	
	public void setFinalHourlyForecastList(List<FinalHourlyForecast> finalHourlyForecastList) {
		this.finalHourlyForecastList = finalHourlyForecastList;
	}
	
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
	
	@Override
	public void setValuesToViews() {
		//kma hourly forecast detail :
		//단기예보 : 강수확률, 강수형태(pty), 1시간강수량, 습도, 1시간신적설, 하늘상태(sky), 1시간기온, 최저/최고기온, 풍속, 풍향, 파고
		//초단기예보 : 기온, 1시간강수량, 하늘상태(sky), 습도, 강수형태(pty), 낙뢰, 풍향, 풍속
		//공통 : 날짜, 시각, 하늘상태, 기온, 강수확률, 강수량, 신적설, 낙뢰, 풍향, 풍속, 바람세기, 습도
		binding.forecastView.removeAllViews();
		binding.labels.removeAllViews();
		
		Context context = getContext();
		
		final int dateRowHeight = (int) getResources().getDimension(R.dimen.dateValueRowHeightInCOMMON);
		final int clockRowHeight = (int) getResources().getDimension(R.dimen.clockValueRowHeightInCOMMON);
		final int weatherRowHeight = (int) getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC);
		final int tempRowHeight = (int) getResources().getDimension(R.dimen.singleTemperatureRowHeightInCOMMON);
		final int windDirectionRowHeight = (int) getResources().getDimension(R.dimen.singleWindDirectionIconValueRowHeightInD);
		final int defaultTextRowHeight = (int) getResources().getDimension(R.dimen.defaultValueRowHeightInSC);
		
		final int columnsCount = finalHourlyForecastList.size();
		final int columnWidth = (int) getResources().getDimension(R.dimen.valueColumnWidthInDHourly);
		final int viewWidth = columnsCount * columnWidth;
		
		addLabelView(R.drawable.date, getString(R.string.date), dateRowHeight);
		addLabelView(R.drawable.time, getString(R.string.clock), clockRowHeight);
		addLabelView(R.drawable.day_clear, getString(R.string.weather), weatherRowHeight);
		addLabelView(R.drawable.temperature, getString(R.string.temperature), tempRowHeight);
		addLabelView(R.drawable.pop, getString(R.string.probability_of_precipitation), defaultTextRowHeight);
		addLabelView(R.drawable.precipitationvolume, getString(R.string.precipitation_volume), defaultTextRowHeight);
		addLabelView(R.drawable.snowvolume, getString(R.string.fresh_snow_cover), defaultTextRowHeight);
		addLabelView(R.drawable.thunderstorm, getString(R.string.thunderstorm), defaultTextRowHeight);
		addLabelView(R.drawable.humidity, getString(R.string.humidity), defaultTextRowHeight);
		addLabelView(R.drawable.winddirection, getString(R.string.wind_direction), windDirectionRowHeight);
		addLabelView(R.drawable.windspeed, getString(R.string.wind_speed), defaultTextRowHeight);
		addLabelView(R.drawable.windstrength, getString(R.string.wind_strength), defaultTextRowHeight);
		
		dateRow = new DateView(context, FragmentType.Detail, viewWidth, dateRowHeight, columnWidth);
		ClockView clockRow = new ClockView(context, FragmentType.Detail, viewWidth, clockRowHeight, columnWidth);
		SingleWeatherIconView weatherIconRow = new SingleWeatherIconView(context, FragmentType.Detail, viewWidth, weatherRowHeight,
				columnWidth);
		TextValueView probabilityOfPrecipitationRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight,
				columnWidth);
		TextValueView precipitationVolumeRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight,
				columnWidth);
		TextValueView freshSnowCoverRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView thunderstormRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView humidityRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
		SingleWindDirectionView windDirectionRow = new SingleWindDirectionView(context, FragmentType.Detail, viewWidth,
				windDirectionRowHeight, columnWidth);
		TextValueView windSpeedRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView windStrengthRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight, columnWidth);
		
		//날짜, 시각 --------------------------------------------------------------------------
		List<ZonedDateTime> dateTimeList = new ArrayList<>();
		for (FinalHourlyForecast finalHourlyForecast : finalHourlyForecastList) {
			dateTimeList.add(finalHourlyForecast.getFcstDateTime());
		}
		dateRow.init(dateTimeList);
		clockRow.setClockList(dateTimeList);
		//하늘상태
		
		//기온, 강수확률, 강수량, 신적설, 낙뢰, 풍향, 풍속, 바람세기, 습도
		List<Integer> tempList = new ArrayList<>();
		List<String> probabilityOfPrecipitationList = new ArrayList<>();
		List<String> precipitationVolumeList = new ArrayList<>();
		List<String> freshSnowCoverList = new ArrayList<>();
		List<String> thunderStormList = new ArrayList<>();
		List<Integer> windDirectionList = new ArrayList<>();
		List<String> windSpeedList = new ArrayList<>();
		List<String> windStrengthList = new ArrayList<>();
		List<String> humidityList = new ArrayList<>();
		List<SingleWeatherIconView.WeatherIconObj> weatherIconObjList = new ArrayList<>();
		
		for (FinalHourlyForecast finalHourlyForecast : finalHourlyForecastList) {
			weatherIconObjList.add(new SingleWeatherIconView.WeatherIconObj(
					ContextCompat.getDrawable(context, KmaResponseProcessor.getWeatherSkyIconImg(finalHourlyForecast.getSky(), false))));
			//기온
			tempList.add(ValueUnits.convertTemperature(finalHourlyForecast.getTemp1Hour(), tempUnit));
			//강수확률
			probabilityOfPrecipitationList.add(finalHourlyForecast.getProbabilityOfPrecipitation());
			//강수량
			precipitationVolumeList.add(finalHourlyForecast.getRainPrecipitation1Hour());
			//신적설
			freshSnowCoverList.add(finalHourlyForecast.getSnowPrecipitation1Hour());
			//낙뢰
			thunderStormList.add(finalHourlyForecast.getLightning() == null ? "" : "O");
			//풍향
			windDirectionList.add(Integer.parseInt(finalHourlyForecast.getWindDirection()));
			//풍속
			windSpeedList.add(ValueUnits.convertWindSpeed(finalHourlyForecast.getWindSpeed(), windUnit).toString());
			//바람세기
			windStrengthList.add(WeatherResponseProcessor.getSimpleWindSpeedDescription(finalHourlyForecast.getWindSpeed()));
			//습도
			humidityList.add(finalHourlyForecast.getHumidity());
		}
		
		DetailSingleTemperatureView tempRow = new DetailSingleTemperatureView(context, FragmentType.Detail, tempList, viewWidth,
				tempRowHeight, columnWidth);
		
		probabilityOfPrecipitationRow.setValueList(probabilityOfPrecipitationList);
		precipitationVolumeRow.setValueList(precipitationVolumeList);
		freshSnowCoverRow.setValueList(freshSnowCoverList);
		thunderstormRow.setValueList(thunderStormList);
		humidityRow.setValueList(humidityList);
		windDirectionRow.setWindDirectionObjList(windDirectionList);
		windSpeedRow.setValueList(windSpeedList);
		windStrengthRow.setValueList(windStrengthList);
		weatherIconRow.setWeatherImgs(weatherIconObjList);
		
		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		rowLayoutParams.gravity = Gravity.CENTER;
		
		binding.forecastView.addView(dateRow, rowLayoutParams);
		binding.forecastView.addView(clockRow, rowLayoutParams);
		binding.forecastView.addView(weatherIconRow, rowLayoutParams);
		binding.forecastView.addView(tempRow, rowLayoutParams);
		binding.forecastView.addView(probabilityOfPrecipitationRow, rowLayoutParams);
		binding.forecastView.addView(precipitationVolumeRow, rowLayoutParams);
		binding.forecastView.addView(freshSnowCoverRow, rowLayoutParams);
		binding.forecastView.addView(thunderstormRow, rowLayoutParams);
		binding.forecastView.addView(humidityRow, rowLayoutParams);
		binding.forecastView.addView(windDirectionRow, rowLayoutParams);
		binding.forecastView.addView(windSpeedRow, rowLayoutParams);
		binding.forecastView.addView(windStrengthRow, rowLayoutParams);
	}
}
