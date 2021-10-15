package com.lifedawn.bestweather.weathers.detailfragment.accuweather.currentconditions;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
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
	}

	public AccuDetailCurrentConditionsFragment setCurrentConditionsResponse(CurrentConditionsResponse currentConditionsResponse) {
		this.currentConditionsResponse = currentConditionsResponse;
		return this;
	}

	@Override
	public void setValuesToViews() {
		//날씨 아이콘, 기온, 체감기온, 습도, 이슬점, 풍향, 풍속, 돌풍, 바람세기, 기압, 자외선, 시정거리,
		//운량, 강수량, 강수형태
		View tempView = addGridItemView();
		View realFeelTempView = addGridItemView();
		View humidityView = addGridItemView();
		View dewPointView = addGridItemView();
		View windDirectionView = addGridItemView();
		View windSpeedView = addGridItemView();
		View windGustView = addGridItemView();
		View windStrengthView = addGridItemView();
		View pressureView = addGridItemView();
		View uvView = addGridItemView();
		View visibilityView = addGridItemView();
		View cloudCoverView = addGridItemView();
		View precipitationVolumeView = addGridItemView();
		View precipitationTypeView = addGridItemView();

		setLabel(tempView, R.string.temperature);
		setLabel(realFeelTempView, R.string.real_feel_temperature);
		setLabel(humidityView, R.string.humidity);
		setLabel(dewPointView, R.string.dew_point);
		setLabel(windDirectionView, R.string.wind_direction);
		setLabel(windSpeedView, R.string.wind_speed);
		setLabel(windGustView, R.string.wind_gust);
		setLabel(windStrengthView, R.string.wind_strength);
		setLabel(pressureView, R.string.pressure);
		setLabel(uvView, R.string.uv_index);
		setLabel(visibilityView, R.string.visibility);
		setLabel(cloudCoverView, R.string.cloud_cover);
		setLabel(precipitationVolumeView, R.string.precipitation_volume);
		setLabel(precipitationTypeView, R.string.precipitation_type);

		CurrentConditionsResponse.Item item = currentConditionsResponse.getItems().get(0);
		setValue(tempView, item.getTemperature().getValue());
		setValue(realFeelTempView, item.getRealFeelTemperature().getValue());
		setValue(humidityView, item.getRelativeHumidity());
		setValue(dewPointView, item.getDewPoint().getValue());
		setValue(windDirectionView, item.getWind().getDirection().getDegrees());
		setValue(windSpeedView, item.getWind().getSpeed().getMetric().getValue());
		setValue(windGustView, item.getWindGust().getSpeed().getMetric().getValue());
		setValue(windStrengthView, WeatherResponseProcessor.getSimpleWindSpeedDescription(item.getWind().getSpeed().getMetric().getValue()));
		setValue(pressureView, item.getPressure().getMetric().getValue());
		setValue(uvView, item.getuVIndexText());
		setValue(visibilityView, item.getVisibility().getMetric().getValue());
		setValue(cloudCoverView, item.getCloudCover());
		setValue(precipitationVolumeView, item.getPrecip1hr().getMetric().getValue());
		setValue(precipitationTypeView, item.getPrecipitationType());
	}
}
