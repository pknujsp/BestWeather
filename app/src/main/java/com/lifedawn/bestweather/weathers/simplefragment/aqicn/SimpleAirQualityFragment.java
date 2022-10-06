package com.lifedawn.bestweather.weathers.simplefragment.aqicn;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.gridlayout.widget.GridLayout;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.databinding.FragmentAirQualitySimpleBinding;
import com.lifedawn.bestweather.retrofit.responses.aqicn.AqiCnGeolocalizedFeedResponse;
import com.lifedawn.bestweather.theme.AppTheme;
import com.lifedawn.bestweather.weathers.WeatherFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.util.LocationDistance;
import com.lifedawn.bestweather.weathers.detailfragment.aqicn.DetailAirQualityFragment;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;
import com.lifedawn.bestweather.weathers.FragmentType;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;


public class SimpleAirQualityFragment extends Fragment implements IWeatherValues {
	private FragmentAirQualitySimpleBinding binding;
	private AirQualityDto airQualityDto;
	private AqiCnGeolocalizedFeedResponse aqiCnGeolocalizedFeedResponse;
	private Double latitude;
	private Double longitude;
	private ZoneId zoneId;
	private Bundle bundle;

	public SimpleAirQualityFragment setAirQualityDto(AirQualityDto airQualityDto) {
		this.airQualityDto = airQualityDto;
		return this;
	}

	public SimpleAirQualityFragment setAqiCnGeolocalizedFeedResponse(AqiCnGeolocalizedFeedResponse aqiCnGeolocalizedFeedResponse) {
		this.aqiCnGeolocalizedFeedResponse = aqiCnGeolocalizedFeedResponse;
		return this;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bundle = savedInstanceState != null ? savedInstanceState : getArguments();

		latitude = bundle.getDouble(BundleKey.Latitude.name());
		longitude = bundle.getDouble(BundleKey.Longitude.name());
		zoneId = (ZoneId) bundle.getSerializable(BundleKey.TimeZone.name());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentAirQualitySimpleBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.progressResultView.setContentView(binding.contentContainer);
		binding.progressResultView.setTextColor(Color.WHITE);

		binding.weatherCardViewHeader.forecastName.setText(R.string.air_quality);
		binding.weatherCardViewHeader.compareForecast.setVisibility(View.GONE);
		binding.weatherCardViewHeader.detailForecast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				DetailAirQualityFragment detailAirQualityFragment = new DetailAirQualityFragment();
				DetailAirQualityFragment.setResponse(aqiCnGeolocalizedFeedResponse);

				Bundle bundle = new Bundle();
				bundle.putSerializable(BundleKey.TimeZone.name(), zoneId);
				bundle.putDouble(BundleKey.Latitude.name(), latitude);
				bundle.putDouble(BundleKey.Longitude.name(), longitude);
				detailAirQualityFragment.setArguments(bundle);

				String tag = getString(R.string.tag_detail_air_quality_fragment);
				FragmentManager fragmentManager = getParentFragment().getParentFragmentManager();

				fragmentManager.beginTransaction().hide(
						fragmentManager.findFragmentByTag(WeatherFragment.class.getName())).add(R.id.fragment_container,
						detailAirQualityFragment, tag).addToBackStack(tag).commit();
			}
		});
		setValuesToViews();
	}


	@Override
	public void setValuesToViews() {
		//응답 실패한 경우
		if (!airQualityDto.isSuccessful()) {
			binding.progressResultView.onFailed(getString(R.string.error));
			binding.weatherCardViewHeader.detailForecast.setVisibility(View.GONE);
		} else {
			binding.weatherCardViewHeader.detailForecast.setVisibility(View.VISIBLE);
			binding.progressResultView.onSuccessful();

			//측정소와의 거리 계산 후 50km이상의 거리에 있으면 표시보류
			final Double distance = LocationDistance.distance(latitude, longitude,
					airQualityDto.getLatitude(), airQualityDto.getLongitude(), LocationDistance.Unit.KM);

			String noData = getString(R.string.noData);
			String distanceStr = String.format("%.2f", distance) + getString(R.string.km);
			if (distance > 100) {
				distanceStr += ", " + getString(R.string.the_measuring_station_is_very_far_away);
			}
			binding.distanceToMeasuringStation.setText(distanceStr);
			binding.measuringStationName.setText(airQualityDto.getCityName() != null ? airQualityDto.getCityName() : noData);

			final int overallGrade = airQualityDto.getAqi();
			final String currentOverallDescription = AqicnResponseProcessor.getGradeDescription(overallGrade);
			final int currentOverallColor = AqicnResponseProcessor.getGradeColorId(overallGrade);

			binding.currentAirquality.setText(overallGrade == -1 ? noData : currentOverallDescription);
			binding.currentAirquality.setTextColor(currentOverallColor);

			if (!airQualityDto.getCurrent().isHasPm10()) {
				addGridItem(null, R.string.pm10_str, R.drawable.pm10);
			} else {
				addGridItem(airQualityDto.getCurrent().getPm10(), R.string.pm10_str, R.drawable.pm10);
			}

			if (!airQualityDto.getCurrent().isHasPm25()) {
				addGridItem(null, R.string.pm25_str, R.drawable.pm25);
			} else {
				addGridItem(airQualityDto.getCurrent().getPm25(), R.string.pm25_str, R.drawable.pm25);
			}

			if (!airQualityDto.getCurrent().isHasO3()) {
				addGridItem(null, R.string.o3_str, R.drawable.o3);
			} else {
				addGridItem(airQualityDto.getCurrent().getO3(), R.string.o3_str, R.drawable.o3);
			}

			if (!airQualityDto.getCurrent().isHasCo()) {
				addGridItem(null, R.string.co_str, R.drawable.co);
			} else {
				addGridItem(airQualityDto.getCurrent().getCo(), R.string.co_str, R.drawable.co);
			}

			if (!airQualityDto.getCurrent().isHasSo2()) {
				addGridItem(null, R.string.so2_str, R.drawable.so2);
			} else {
				addGridItem(airQualityDto.getCurrent().getSo2(), R.string.so2_str, R.drawable.so2);
			}

			if (!airQualityDto.getCurrent().isHasNo2()) {
				addGridItem(null, R.string.no2_str, R.drawable.no2);
			} else {
				addGridItem(airQualityDto.getCurrent().getNo2(), R.string.no2_str, R.drawable.no2);
			}

			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M.d E", Locale.getDefault());

			List<AirQualityDto.DailyForecast> forecastList = airQualityDto.getDailyForecastList();
			final int textColor = AppTheme.getColor(getContext(), R.attr.textColorInWeatherCard);

			LayoutInflater layoutInflater = LayoutInflater.from(getContext());
			View labelView = layoutInflater.inflate(R.layout.air_quality_simple_forecast_item, null);

			labelView.findViewById(R.id.date).setVisibility(View.INVISIBLE);

			((TextView) labelView.findViewById(R.id.pm10)).setText(getString(R.string.air_quality));
			((TextView) labelView.findViewById(R.id.pm25)).setVisibility(View.GONE);
			((TextView) labelView.findViewById(R.id.o3)).setVisibility(View.GONE);

			((TextView) labelView.findViewById(R.id.date)).setTextColor(textColor);
			((TextView) labelView.findViewById(R.id.pm10)).setTextColor(textColor);
			//((TextView) labelView.findViewById(R.id.pm25)).setTextColor(textColor);
			//((TextView) labelView.findViewById(R.id.o3)).setTextColor(textColor);
			binding.forecast.addView(labelView);

			for (AirQualityDto.DailyForecast forecastObj : forecastList) {
				View forecastItemView = layoutInflater.inflate(R.layout.air_quality_simple_forecast_item, null);

				((TextView) forecastItemView.findViewById(R.id.date)).setText(forecastObj.getDate().format(dateTimeFormatter));
				((TextView) forecastItemView.findViewById(R.id.date)).setTextColor(textColor);
				((TextView) forecastItemView.findViewById(R.id.pm25)).setVisibility(View.GONE);
				((TextView) forecastItemView.findViewById(R.id.o3)).setVisibility(View.GONE);

				int grade = -1;
				if (forecastObj.isHasPm10()) {
					grade = Math.max(grade, forecastObj.getPm10().getAvg());
				}
				if (forecastObj.isHasPm25()) {
					grade = Math.max(grade, forecastObj.getPm25().getAvg());
				}
				if (forecastObj.isHasO3()) {
					grade = Math.max(grade, forecastObj.getO3().getAvg());
				}

				((TextView) forecastItemView.findViewById(R.id.pm10)).setText(grade == -1
						? noData : AqicnResponseProcessor.getGradeDescription(grade));
				((TextView) forecastItemView.findViewById(R.id.pm10)).setTextColor(grade == -1 ? ContextCompat.getColor(getContext(),
						R.color.not_data_color) : AqicnResponseProcessor.getGradeColorId(grade));

				/*
				((TextView) forecastItemView.findViewById(R.id.pm25)).setText(
						!forecastObj.isHasPm25() ? noData : AqicnResponseProcessor.getGradeDescription(forecastObj.getPm25().getAvg()));
				((TextView) forecastItemView.findViewById(R.id.pm25)).setTextColor(!forecastObj.isHasPm25() ?
						ContextCompat.getColor(getContext(),
								R.color.not_data_color) : AqicnResponseProcessor.getGradeColorId(forecastObj.getPm25().getAvg()));

				((TextView) forecastItemView.findViewById(R.id.o3)).setText(
						!forecastObj.isHasO3() ? noData : AqicnResponseProcessor.getGradeDescription(forecastObj.getO3().getAvg()));
				((TextView) forecastItemView.findViewById(R.id.o3)).setTextColor(!forecastObj.isHasO3() ?
						ContextCompat.getColor(getContext(),
								R.color.not_data_color) : AqicnResponseProcessor.getGradeColorId(forecastObj.getO3().getAvg()));
				 */
				binding.forecast.addView(forecastItemView);
			}
		}


	}

	protected final View addGridItem(@Nullable Integer value, int labelDescriptionId, @NonNull Integer labelIconId) {
		View gridItem = getLayoutInflater().inflate(R.layout.air_quality_item, null);
		((ImageView) gridItem.findViewById(R.id.label_icon)).setVisibility(View.GONE);
		((TextView) gridItem.findViewById(R.id.label)).setText(labelDescriptionId);
		((TextView) gridItem.findViewById(R.id.label)).setTextColor(AppTheme.getTextColor(getContext(), FragmentType.Simple));
		((TextView) gridItem.findViewById(R.id.value_int)).setText(value == null ? "?" : value.toString());
		((TextView) gridItem.findViewById(R.id.value_int)).setTextColor(AppTheme.getTextColor(getContext(), FragmentType.Simple));
		((TextView) gridItem.findViewById(R.id.value_str)).setText(
				value == null ? getString(R.string.noData) : AqicnResponseProcessor.getGradeDescription(value));
		((TextView) gridItem.findViewById(R.id.value_str)).setTextColor(
				value == null ? ContextCompat.getColor(getContext(), R.color.not_data_color) : AqicnResponseProcessor.getGradeColorId(
						value));

		int cellCount = binding.grid.getChildCount();
		int row = cellCount / binding.grid.getColumnCount();
		int column = cellCount % binding.grid.getColumnCount();

		GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
		layoutParams.columnSpec = GridLayout.spec(column, GridLayout.FILL, 1);
		layoutParams.rowSpec = GridLayout.spec(row, GridLayout.FILL, 1);

		binding.grid.addView(gridItem, layoutParams);
		return gridItem;
	}
}