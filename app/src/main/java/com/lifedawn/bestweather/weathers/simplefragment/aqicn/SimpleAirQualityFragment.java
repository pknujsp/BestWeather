package com.lifedawn.bestweather.weathers.simplefragment.aqicn;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;


public class SimpleAirQualityFragment extends Fragment implements IWeatherValues {
	private FragmentAirQualitySimpleBinding binding;
	private AirQualityDto airQualityDto;
	private AqiCnGeolocalizedFeedResponse aqiCnGeolocalizedFeedResponse;
	private Double latitude;
	private Double longitude;
	private String addressName;
	private String countryCode;
	private WeatherDataSourceType mainWeatherDataSourceType;
	private ZoneId zoneId;
	private ValueUnits clockUnit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		latitude = bundle.getDouble(BundleKey.Latitude.name());
		longitude = bundle.getDouble(BundleKey.Longitude.name());
		addressName = bundle.getString(BundleKey.AddressName.name());
		countryCode = bundle.getString(BundleKey.CountryCode.name());
		mainWeatherDataSourceType = (WeatherDataSourceType) bundle.getSerializable(BundleKey.WeatherDataSource.name());
		zoneId = (ZoneId) bundle.getSerializable(BundleKey.TimeZone.name());

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		clockUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_clock), ValueUnits.clock12.name()));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentAirQualitySimpleBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.progressResultView.setContentView(binding.contentContainer);

		binding.weatherCardViewHeader.forecastName.setText(R.string.air_quality);
		binding.weatherCardViewHeader.compareForecast.setVisibility(View.GONE);
		binding.weatherCardViewHeader.detailForecast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				DetailAirQualityFragment detailAirQualityFragment = new DetailAirQualityFragment();
				detailAirQualityFragment.setResponse(aqiCnGeolocalizedFeedResponse);

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

	public SimpleAirQualityFragment setGeolocalizedFeedResponse(AqiCnGeolocalizedFeedResponse aqiCnGeolocalizedFeedResponse) {
		this.aqiCnGeolocalizedFeedResponse = aqiCnGeolocalizedFeedResponse;
		return this;
	}

	public void setAirQualityDto(AirQualityDto airQualityDto) {
		this.airQualityDto = airQualityDto;
	}

	@Override
	public void setValuesToViews() {
		//응답 실패한 경우
		if (aqiCnGeolocalizedFeedResponse == null || !aqiCnGeolocalizedFeedResponse.getStatus().equals("ok")) {
			binding.progressResultView.onFailed(getString(R.string.error));
			binding.weatherCardViewHeader.detailForecast.setVisibility(View.GONE);
			return;
		} else {
			binding.weatherCardViewHeader.detailForecast.setVisibility(View.VISIBLE);
			binding.progressResultView.onSuccessful();
		}

		//측정소와의 거리 계산 후 50km이상의 거리에 있으면 표시보류
		final Double distance = LocationDistance.distance(latitude, longitude,
				Double.parseDouble(aqiCnGeolocalizedFeedResponse.getData().getCity().getGeo().get(0)),
				Double.parseDouble(aqiCnGeolocalizedFeedResponse.getData().getCity().getGeo().get(1)), LocationDistance.Unit.KM);

		String notData = getString(R.string.noData);
		String distanceStr = String.format("%.2f", distance) + getString(R.string.km);
		if (distance > 100) {
			distanceStr += ", " + getString(R.string.the_measuring_station_is_very_far_away);
		}
		binding.distanceToMeasuringStation.setText(distanceStr);

		if (aqiCnGeolocalizedFeedResponse.getData().getCity().getName() != null) {
			binding.measuringStationName.setText(aqiCnGeolocalizedFeedResponse.getData().getCity().getName());
		} else {
			binding.measuringStationName.setText(notData);
		}
		/*
		if (geolocalizedFeedResponse.getData().getTime().getIso() != null) {
			// time : 2021-10-22T11:16:41+09:00
			ZonedDateTime syncDateTime = null;
			try {
				syncDateTime = ZonedDateTime.parse(geolocalizedFeedResponse.getData().getTime().getIso());
			} catch (Exception e) {

			}
			DateTimeFormatter syncDateTimeFormatter = DateTimeFormatter.ofPattern(clockUnit == ValueUnits.clock12 ? "M.d E a h:mm" : "M.d" +
							" E HH:mm",
					Locale.getDefault());
			binding.updatedTime.setText(syncDateTime.format(syncDateTimeFormatter));
		} else {
			binding.updatedTime.setText(notData);
		}

		 */

		AqiCnGeolocalizedFeedResponse.Data.IAqi iAqi = aqiCnGeolocalizedFeedResponse.getData().getIaqi();
		if (iAqi.getPm10() == null) {
			addGridItem(null, R.string.pm10_str, R.drawable.pm10);
		} else {
			Integer pm10 = (int) Double.parseDouble(iAqi.getPm10().getValue());
			addGridItem(pm10, R.string.pm10_str, R.drawable.pm10);
		}

		if (iAqi.getPm25() == null) {
			addGridItem(null, R.string.pm25_str, R.drawable.pm25);
		} else {
			Integer pm25 = (int) Double.parseDouble(iAqi.getPm25().getValue());
			addGridItem(pm25, R.string.pm25_str, R.drawable.pm25);
		}


		if (iAqi.getO3() == null) {
			addGridItem(null, R.string.o3_str, R.drawable.o3);
		} else {
			Integer o3 = (int) Double.parseDouble(iAqi.getO3().getValue());
			addGridItem(o3, R.string.o3_str, R.drawable.o3);
		}


		if (iAqi.getCo() == null) {
			addGridItem(null, R.string.co_str, R.drawable.co);
		} else {
			Integer co = (int) Double.parseDouble(iAqi.getCo().getValue());
			addGridItem(co, R.string.co_str, R.drawable.co);
		}

		if (iAqi.getSo2() == null) {
			addGridItem(null, R.string.so2_str, R.drawable.so2);
		} else {
			Integer so2 = (int) Double.parseDouble(iAqi.getSo2().getValue());
			addGridItem(so2, R.string.so2_str, R.drawable.so2);
		}

		if (iAqi.getNo2() == null) {
			addGridItem(null, R.string.no2_str, R.drawable.no2);
		} else {
			Integer no2 = (int) Double.parseDouble(iAqi.getNo2().getValue());
			addGridItem(no2, R.string.no2_str, R.drawable.no2);
		}

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M.d E", Locale.getDefault());
		LayoutInflater layoutInflater = LayoutInflater.from(getContext());

		List<AirQualityForecastObj> forecastObjList = AqicnResponseProcessor.getAirQualityForecastObjList(aqiCnGeolocalizedFeedResponse,
				zoneId);
		final int textColor = AppTheme.getColor(getContext(), R.attr.textColorInWeatherCard);

		View labelView = layoutInflater.inflate(R.layout.air_quality_simple_forecast_item, null);
		labelView.findViewById(R.id.date).setVisibility(View.INVISIBLE);
		((TextView) labelView.findViewById(R.id.pm10)).setText(getString(R.string.pm10_str));
		((TextView) labelView.findViewById(R.id.pm25)).setText(getString(R.string.pm25_str));
		((TextView) labelView.findViewById(R.id.o3)).setText(getString(R.string.o3_str));

		((TextView) labelView.findViewById(R.id.date)).setTextColor(textColor);
		((TextView) labelView.findViewById(R.id.pm10)).setTextColor(textColor);
		((TextView) labelView.findViewById(R.id.pm25)).setTextColor(textColor);
		((TextView) labelView.findViewById(R.id.o3)).setTextColor(textColor);
		binding.forecast.addView(labelView);

		for (AirQualityForecastObj forecastObj : forecastObjList) {

			View forecastItemView = layoutInflater.inflate(R.layout.air_quality_simple_forecast_item, null);
			((TextView) forecastItemView.findViewById(R.id.date)).setText(forecastObj.date.format(dateTimeFormatter));

			((TextView) forecastItemView.findViewById(R.id.pm10)).setText(
					forecastObj.pm10 == null ? notData : AqicnResponseProcessor.getGradeDescription(forecastObj.pm10));
			((TextView) forecastItemView.findViewById(R.id.pm10)).setTextColor(forecastObj.pm10 == null ? ContextCompat.getColor(getContext(),
					R.color.not_data_color) : AqicnResponseProcessor.getGradeColorId(forecastObj.pm10));

			((TextView) forecastItemView.findViewById(R.id.pm25)).setText(
					forecastObj.pm25 == null ? notData : AqicnResponseProcessor.getGradeDescription(forecastObj.pm25));
			((TextView) forecastItemView.findViewById(R.id.pm25)).setTextColor(forecastObj.pm25 == null ? ContextCompat.getColor(getContext(),
					R.color.not_data_color) : AqicnResponseProcessor.getGradeColorId(forecastObj.pm25));

			((TextView) forecastItemView.findViewById(R.id.o3)).setText(
					forecastObj.o3 == null ? notData : AqicnResponseProcessor.getGradeDescription(forecastObj.o3));
			((TextView) forecastItemView.findViewById(R.id.o3)).setTextColor(forecastObj.o3 == null ? ContextCompat.getColor(getContext(),
					R.color.not_data_color) : AqicnResponseProcessor.getGradeColorId(forecastObj.o3));

			((TextView) forecastItemView.findViewById(R.id.date)).setTextColor(textColor);

			binding.forecast.addView(forecastItemView);
		}
	}

	protected final View addGridItem(@Nullable Integer value, int labelDescriptionId, @NonNull Integer labelIconId) {
		View gridItem = getLayoutInflater().inflate(R.layout.air_quality_item, null);
		((ImageView) gridItem.findViewById(R.id.label_icon)).setImageResource(labelIconId);
		((TextView) gridItem.findViewById(R.id.label)).setText(labelDescriptionId);
		((TextView) gridItem.findViewById(R.id.label)).setTextColor(AppTheme.getTextColor(getContext(), FragmentType.Simple));
		((TextView) gridItem.findViewById(R.id.value_int)).setText(value == null ? "?" : value.toString());
		((TextView) gridItem.findViewById(R.id.value_int)).setTextColor(AppTheme.getTextColor(getContext(), FragmentType.Simple));
		((TextView) gridItem.findViewById(R.id.value_str)).setText(
				value == null ? getString(R.string.noData) : AqicnResponseProcessor.getGradeDescription(value));
		((TextView) gridItem.findViewById(R.id.value_str)).setTextColor(
				value == null ? ContextCompat.getColor(getContext(), R.color.not_data_color) : AqicnResponseProcessor.getGradeColorId(
						value));

		binding.grid.addView(gridItem);
		return gridItem;
	}
}