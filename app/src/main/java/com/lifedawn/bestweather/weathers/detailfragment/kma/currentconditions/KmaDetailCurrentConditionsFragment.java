package com.lifedawn.bestweather.weathers.detailfragment.kma.currentconditions;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.weathers.dataprocessing.util.SunRiseSetUtil;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WindDirectionConverter;
import com.lifedawn.bestweather.weathers.detailfragment.base.BaseDetailCurrentConditionsFragment;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.TimeZone;

public class KmaDetailCurrentConditionsFragment extends BaseDetailCurrentConditionsFragment {
	private FinalCurrentConditions finalCurrentConditions;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setValuesToViews();
	}

	public KmaDetailCurrentConditionsFragment setFinalCurrentConditions(FinalCurrentConditions finalCurrentConditions) {
		this.finalCurrentConditions = finalCurrentConditions;
		return this;
	}

	@Override
	public void setValuesToViews() {
		// 기온,1시간강수량,습도,강수형태,풍향,풍속
		String tempUnitStr = ValueUnits.convertToStr(getContext(), tempUnit);

		binding.conditionsGrid.requestLayout();

		SunriseSunsetCalculator sunriseSunsetCalculator = new SunriseSunsetCalculator(new Location(latitude, longitude),
				TimeZone.getTimeZone(zoneId.getId()));
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(zoneId.getId()));
		Calendar sunRise = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(calendar);
		Calendar sunSet = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(calendar);

		addGridItem(R.string.weather,
				KmaResponseProcessor.getWeatherPtyIconDescription(finalCurrentConditions.getPrecipitationType()),
				KmaResponseProcessor.getWeatherPtyIconImg(finalCurrentConditions.getPrecipitationType(), SunRiseSetUtil.isNight(calendar,
						sunRise, sunSet)),
				null);

		addGridItem(R.string.precipitation_volume, finalCurrentConditions.getPrecipitation1Hour().equals("0") ?
						getString(R.string.not_available) :
						finalCurrentConditions.getPrecipitation1Hour() + ValueUnits.convertToStr(getContext(), ValueUnits.mm),
				R.drawable.precipitationvolume, null);

		addGridItem(R.string.temperature, ValueUnits.convertTemperature(finalCurrentConditions.getTemperature(), tempUnit) + tempUnitStr,
				R.drawable.temperature, null);
		addGridItem(R.string.humidity, finalCurrentConditions.getHumidity() + ValueUnits.convertToStr(getContext(), ValueUnits.percent), R.drawable.humidity,
				null);
		View windDirectionView = addGridItem(R.string.wind_direction, WindDirectionConverter.windDirection(getContext(), finalCurrentConditions.getWindDirection()),
				R.drawable.arrow,
				null);
		((ImageView) windDirectionView.findViewById(R.id.label_icon)).setRotation(Integer.parseInt(finalCurrentConditions.getWindDirection()) + 180);
		addGridItem(R.string.wind_speed,
				ValueUnits.convertWindSpeed(finalCurrentConditions.getWindSpeed(), windUnit) + ValueUnits.convertToStr(getContext(), windUnit),
				R.drawable.windspeed, null);
		addGridItem(R.string.wind_strength, WeatherResponseProcessor.getSimpleWindSpeedDescription(finalCurrentConditions.getWindSpeed()),
				R.drawable.windstrength, null);


	}
}
