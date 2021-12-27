package com.lifedawn.bestweather.weathers.detailfragment.accuweather.currentconditions;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WindUtil;
import com.lifedawn.bestweather.weathers.detailfragment.base.BaseDetailCurrentConditionsFragment;

import org.jetbrains.annotations.NotNull;

public class AccuDetailCurrentConditionsFragment extends BaseDetailCurrentConditionsFragment {
	private CurrentConditionsResponse currentConditionsResponse;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setValuesToViews();
	}

	public AccuDetailCurrentConditionsFragment setCurrentConditionsResponse(CurrentConditionsResponse currentConditionsResponse) {
		this.currentConditionsResponse = currentConditionsResponse;
		return this;
	}

	@Override
	public void setValuesToViews() {
		//날씨 아이콘, 기온, 체감기온, 습도, 이슬점, 풍향, 풍속, 돌풍, 바람세기, 기압, 자외선, 시정거리,
		//운량, 강수량, 강수형태
		CurrentConditionsResponse.Item item = currentConditionsResponse.getItems().get(0);
		String tempUnitStr = ValueUnits.convertToStr(getContext(), tempUnit);
		String percent = ValueUnits.convertToStr(getContext(), ValueUnits.percent);

		binding.conditionsGrid.requestLayout();

		addGridItem(R.string.weather, AccuWeatherResponseProcessor.getWeatherIconDescription(item.getWeatherIcon()),
				AccuWeatherResponseProcessor.getWeatherIconImg(item.getWeatherIcon()), null);
		addGridItem(R.string.temperature,
				ValueUnits.convertTemperature(item.getTemperature().getMetric().getValue(), tempUnit) + tempUnitStr,
				R.drawable.temperature, null);
		addGridItem(R.string.real_feel_temperature,
				ValueUnits.convertTemperature(item.getRealFeelTemperature().getMetric().getValue(), tempUnit) + tempUnitStr,
				R.drawable.realfeeltemperature, null);
		addGridItem(R.string.humidity, item.getRelativeHumidity() + percent, R.drawable.humidity, null);
		addGridItem(R.string.dew_point, ValueUnits.convertTemperature(item.getDewPoint().getMetric().getValue(), tempUnit) + tempUnitStr,
				R.drawable.dewpoint,
				null);
		View windDirectionView = addGridItem(R.string.wind_direction, WindUtil.parseWindDirectionDegreeAsStr(getContext(), item.getWind().getDirection().getDegrees()),
				R.drawable.arrow, null);
		((ImageView) windDirectionView.findViewById(R.id.label_icon)).setRotation(Integer.parseInt(item.getWind().getDirection().getDegrees()) + 180);

		addGridItem(R.string.wind_speed,
				ValueUnits.convertWindSpeedForAccu(item.getWind().getSpeed().getMetric().getValue(), windUnit) + ValueUnits.convertToStr(getContext(), windUnit),
				R.drawable.windspeed, null);
		addGridItem(R.string.wind_gust,
				ValueUnits.convertWindSpeedForAccu(item.getWindGust().getSpeed().getMetric().getValue(), windUnit) + ValueUnits.convertToStr(getContext(), windUnit),
				R.drawable.windgust, null);
		addGridItem(R.string.wind_strength,
				WindUtil.getSimpleWindSpeedDescription(item.getWind().getSpeed().getMetric().getValue()),
				R.drawable.windstrength, null);
		addGridItem(R.string.pressure, item.getPressure().getMetric().getValue() + ValueUnits.convertToStr(getContext(), ValueUnits.hpa),
				R.drawable.pressure, null);
		addGridItem(R.string.uv_index, item.getuVIndex(), R.drawable.uv, null);
		addGridItem(R.string.visibility,
				ValueUnits.convertVisibilityForAccu(item.getVisibility().getMetric().getValue(), visibilityUnit) + ValueUnits.convertToStr(getContext(),
						visibilityUnit),
				R.drawable.visibility, null);
		addGridItem(R.string.cloud_cover, item.getCloudCover() + percent, R.drawable.cloudiness, null);
		addGridItem(R.string.precipitation_volume, item.getPrecip1hr().getMetric().getValue().equals("0.0") ?
						getString(R.string.not_available) :
						item.getPrecip1hr().getMetric().getValue() + ValueUnits.convertToStr(getContext(), ValueUnits.mm),
				R.drawable.pop, null);
		addGridItem(R.string.precipitation_type, AccuWeatherResponseProcessor.getPty(item.getPrecipitationType()), R.drawable.temp_icon, null);
	}
}
