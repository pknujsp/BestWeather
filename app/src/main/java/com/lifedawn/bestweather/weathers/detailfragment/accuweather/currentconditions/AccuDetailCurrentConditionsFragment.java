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

		binding.conditionsGrid.removeAllViews();

		addGridItem(R.string.weather, AccuWeatherResponseProcessor.getWeatherIconDescription(item.getWeatherIcon()),
				AccuWeatherResponseProcessor.getWeatherIconImg(item.getWeatherIcon()));
		addGridItem(R.string.temperature,
				ValueUnits.convertTemperature(item.getTemperature().getMetric().getValue(), tempUnit) + tempUnitStr, null);
		addGridItem(R.string.real_feel_temperature,
				ValueUnits.convertTemperature(item.getRealFeelTemperature().getMetric().getValue(), tempUnit) + tempUnitStr, null);
		addGridItem(R.string.humidity, item.getRelativeHumidity() + percent, null);
		addGridItem(R.string.dew_point, ValueUnits.convertTemperature(item.getDewPoint().getMetric().getValue(), tempUnit) + tempUnitStr, null);
		View windDirectionView = addGridItem(R.string.wind_direction, WindUtil.parseWindDirectionDegreeAsStr(getContext(), item.getWind().getDirection().getDegrees()),
				R.drawable.arrow);
		((ImageView) windDirectionView.findViewById(R.id.label_icon)).setRotation(Integer.parseInt(item.getWind().getDirection().getDegrees()) + 180);

		addGridItem(R.string.wind_speed,
				ValueUnits.convertWindSpeedForAccu(item.getWind().getSpeed().getMetric().getValue(), windUnit) + ValueUnits.convertToStr(getContext(), windUnit),
				null);
		addGridItem(R.string.wind_gust,
				ValueUnits.convertWindSpeedForAccu(item.getWindGust().getSpeed().getMetric().getValue(), windUnit) + ValueUnits.convertToStr(getContext(), windUnit),
				null);
		addGridItem(R.string.wind_strength,
				WindUtil.getSimpleWindSpeedDescription(item.getWind().getSpeed().getMetric().getValue()),
				null);
		addGridItem(R.string.pressure, item.getPressure().getMetric().getValue() + ValueUnits.convertToStr(getContext(), ValueUnits.hpa),
				null);
		addGridItem(R.string.uv_index, item.getuVIndex(), null);
		addGridItem(R.string.visibility,
				ValueUnits.convertVisibilityForAccu(item.getVisibility().getMetric().getValue(), visibilityUnit) + ValueUnits.convertToStr(getContext(),
						visibilityUnit),
				null);
		addGridItem(R.string.cloud_cover, item.getCloudCover() + percent, null);
		addGridItem(R.string.precipitation_volume, item.getPrecip1hr().getMetric().getValue().equals("0.0") ?
						getString(R.string.not_available) :
						item.getPrecip1hr().getMetric().getValue() + ValueUnits.convertToStr(getContext(), ValueUnits.mm),
				null);
		addGridItem(R.string.precipitation_type, AccuWeatherResponseProcessor.getPty(item.getPrecipitationType()), null);
	}
}
