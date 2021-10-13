package com.lifedawn.bestweather.weathers.detailfragment.kma.currentconditions;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.weathers.detailfragment.base.BaseDetailCurrentConditionsFragment;

import org.jetbrains.annotations.NotNull;

public class KmaCurrentConditionsDetailFragment extends BaseDetailCurrentConditionsFragment {
	private FinalCurrentConditions finalCurrentConditions;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	public KmaCurrentConditionsDetailFragment setFinalCurrentConditions(FinalCurrentConditions finalCurrentConditions) {
		this.finalCurrentConditions = finalCurrentConditions;
		return this;
	}

	@Override
	public void setValuesToViews() {
		// 기온,1시간강수량,습도,강수형태,풍향,풍속
		View tempView = addGridItemView();
		View humidityView = addGridItemView();
		View windDirectionView = addGridItemView();
		View windSpeedView = addGridItemView();
		View windStrengthView = addGridItemView();
		View precipitationVolumeView = addGridItemView();
		View precipitationTypeView = addGridItemView();

		setLabel(tempView, R.string.temperature);
		setLabel(humidityView, R.string.humidity);
		setLabel(windDirectionView, R.string.wind_direction);
		setLabel(windSpeedView, R.string.wind_speed);
		setLabel(windStrengthView, R.string.wind_strength);
		setLabel(precipitationVolumeView, R.string.precipitation_volume);
		setLabel(precipitationTypeView, R.string.precipitation_type);

		setValue(tempView, finalCurrentConditions.getTemperature());
		setValue(humidityView, finalCurrentConditions.getHumidity());
		setValue(windDirectionView, finalCurrentConditions.getWindDirection());
		setValue(windSpeedView, finalCurrentConditions.getWindSpeed());
		setValue(windStrengthView, WeatherResponseProcessor.getSimpleWindSpeedDescription(finalCurrentConditions.getWindSpeed()));
		setValue(precipitationVolumeView, finalCurrentConditions.getPrecipitation1Hour());
		setValue(precipitationTypeView, KmaResponseProcessor.getWeatherPtyIconDescription(finalCurrentConditions.getPrecipitationType()));
	}
}
