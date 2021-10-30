package com.lifedawn.bestweather.weathers.simplefragment.aqicn;

import android.content.Context;
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
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.databinding.FragmentAirQualitySimpleBinding;
import com.lifedawn.bestweather.retrofit.responses.aqicn.GeolocalizedFeedResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.util.LocationDistance;
import com.lifedawn.bestweather.weathers.detailfragment.aqicn.DetailAirQualityFragment;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;

import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class SimpleAirQualityFragment extends Fragment implements IWeatherValues {
	private FragmentAirQualitySimpleBinding binding;
	private GeolocalizedFeedResponse geolocalizedFeedResponse;
	private Double latitude;
	private Double longitude;
	private String addressName;
	private String countryCode;
	private WeatherSourceType mainWeatherSourceType;
	private TimeZone timeZone;
	private ValueUnits clockUnit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		latitude = bundle.getDouble(getString(R.string.bundle_key_latitude));
		longitude = bundle.getDouble(getString(R.string.bundle_key_longitude));
		addressName = bundle.getString(getString(R.string.bundle_key_address_name));
		countryCode = bundle.getString(getString(R.string.bundle_key_country_code));
		mainWeatherSourceType = (WeatherSourceType) bundle.getSerializable(
				getString(R.string.bundle_key_main_weather_data_source));
		timeZone = (TimeZone) bundle.getSerializable(getString(R.string.bundle_key_timezone));

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

		binding.weatherCardViewHeader.forecastName.setText(R.string.air_quality);
		binding.weatherCardViewHeader.compareForecast.setVisibility(View.GONE);
		binding.weatherCardViewHeader.detailForecast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				DetailAirQualityFragment detailAirQualityFragment = new DetailAirQualityFragment();
				detailAirQualityFragment.setResponse(geolocalizedFeedResponse);

				Bundle bundle = new Bundle();
				bundle.putSerializable(getString(R.string.bundle_key_timezone), timeZone);
				detailAirQualityFragment.setArguments(bundle);

				String tag = getString(R.string.tag_detail_air_quality_fragment);
				FragmentManager fragmentManager = getParentFragment().getParentFragment().getParentFragmentManager();
				fragmentManager.beginTransaction().hide(
						fragmentManager.findFragmentByTag(getString(R.string.tag_weather_main_fragment))).add(R.id.fragment_container,
						detailAirQualityFragment, tag).addToBackStack(tag).commit();
			}
		});

		setValuesToViews();
	}

	public SimpleAirQualityFragment setGeolocalizedFeedResponse(GeolocalizedFeedResponse geolocalizedFeedResponse) {
		this.geolocalizedFeedResponse = geolocalizedFeedResponse;
		return this;
	}

	@Override
	public void setValuesToViews() {
		Context context = getContext();

		//측정소와의 거리 계산 후 50km이상의 거리에 있으면 표시보류
		final Double distance = LocationDistance.distance(latitude, longitude,
				Double.parseDouble(geolocalizedFeedResponse.getData().getCity().getGeo().get(0)),
				Double.parseDouble(geolocalizedFeedResponse.getData().getCity().getGeo().get(1)),
				LocationDistance.Unit.KM);

		String notData = getString(R.string.not_data);

		if (distance > 50.0) {
			String msg = getString(R.string.the_measuring_station_is_very_far_away) + "\n"
					+ String.format("%.2f", distance) + getString(R.string.km);
			binding.message.setText(msg);
			binding.message.setVisibility(View.VISIBLE);
		} else {
			binding.message.setVisibility(View.GONE);
		}
		binding.distanceToMeasuringStation.setText(String.format("%.2f", distance) + getString(R.string.km));

		if (geolocalizedFeedResponse.getData().getCity().getName() != null) {
			binding.measuringStationName.setText(geolocalizedFeedResponse.getData().getCity().getName());
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

		GeolocalizedFeedResponse.Data.IAqi iAqi = geolocalizedFeedResponse.getData().getIaqi();
		if (iAqi.getPm10() == null) {
			addGridItem(null, R.string.pm10_str, R.drawable.temp_icon);
		} else {
			Integer pm10 = (int) Double.parseDouble(iAqi.getPm10().getValue());
			addGridItem(pm10, R.string.pm10_str, R.drawable.temp_icon);
		}

		if (iAqi.getPm25() == null) {
			addGridItem(null, R.string.pm25_str, R.drawable.temp_icon);
		} else {
			Integer pm25 = (int) Double.parseDouble(iAqi.getPm25().getValue());
			addGridItem(pm25, R.string.pm25_str, R.drawable.temp_icon);
		}


		if (iAqi.getO3() == null) {
			addGridItem(null, R.string.o3_str, R.drawable.temp_icon);
		} else {
			Integer o3 = (int) Double.parseDouble(iAqi.getO3().getValue());
			addGridItem(o3, R.string.o3_str, R.drawable.temp_icon);
		}


		if (iAqi.getCo() == null) {
			addGridItem(null, R.string.co_str, R.drawable.temp_icon);
		} else {
			Integer co = (int) Double.parseDouble(iAqi.getCo().getValue());
			addGridItem(co, R.string.co_str, R.drawable.temp_icon);
		}

		if (iAqi.getSo2() == null) {
			addGridItem(null, R.string.so2_str, R.drawable.temp_icon);
		} else {
			Integer so2 = (int) Double.parseDouble(iAqi.getSo2().getValue());
			addGridItem(so2, R.string.so2_str, R.drawable.temp_icon);
		}

		if (iAqi.getNo2() == null) {
			addGridItem(null, R.string.no2_str, R.drawable.temp_icon);
		} else {
			Integer no2 = (int) Double.parseDouble(iAqi.getNo2().getValue());
			addGridItem(no2, R.string.no2_str, R.drawable.temp_icon);
		}

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M.d E", Locale.getDefault());
		LayoutInflater layoutInflater = LayoutInflater.from(getContext());

		View labelView = layoutInflater.inflate(R.layout.air_quality_simple_forecast_item, null);
		((TextView) labelView.findViewById(R.id.date)).setText(null);
		((TextView) labelView.findViewById(R.id.pm10)).setText(getString(R.string.pm10_str));
		((TextView) labelView.findViewById(R.id.pm25)).setText(getString(R.string.pm25_str));
		((TextView) labelView.findViewById(R.id.o3)).setText(getString(R.string.o3_str));

		List<AirQualityForecastObj> forecastObjList = AqicnResponseProcessor.getAirQualityForecastObjList(geolocalizedFeedResponse, timeZone);
		for (AirQualityForecastObj forecastObj : forecastObjList) {
			View forecastItemView = layoutInflater.inflate(R.layout.air_quality_simple_forecast_item, null);
			((TextView) forecastItemView.findViewById(R.id.date)).setText(forecastObj.date.format(dateTimeFormatter));

			((TextView) forecastItemView.findViewById(R.id.pm10)).setText(forecastObj.pm10 == null ? notData :
					AqicnResponseProcessor.getGradeDescription(forecastObj.pm10));
			((TextView) forecastItemView.findViewById(R.id.pm10)).setTextColor(forecastObj.pm10 == null ?
					ContextCompat.getColor(context, R.color.not_data_color)
					: AqicnResponseProcessor.getGradeColorId(forecastObj.pm10));

			((TextView) forecastItemView.findViewById(R.id.pm25)).setText(forecastObj.pm25 == null ? notData :
					AqicnResponseProcessor.getGradeDescription(forecastObj.pm25));
			((TextView) forecastItemView.findViewById(R.id.pm25)).setTextColor(forecastObj.pm25 == null ?
					ContextCompat.getColor(context, R.color.not_data_color)
					: AqicnResponseProcessor.getGradeColorId(forecastObj.pm25));

			((TextView) forecastItemView.findViewById(R.id.o3)).setText(forecastObj.o3 == null ? notData :
					AqicnResponseProcessor.getGradeDescription(forecastObj.o3));
			((TextView) forecastItemView.findViewById(R.id.o3)).setTextColor(forecastObj.o3 == null ?
					ContextCompat.getColor(context, R.color.not_data_color)
					: AqicnResponseProcessor.getGradeColorId(forecastObj.o3));

			binding.forecast.addView(forecastItemView);
		}
	}

	protected final View addGridItem(@Nullable Integer value, int labelDescriptionId, @NonNull Integer labelIconId) {
		View gridItem = getLayoutInflater().inflate(R.layout.air_quality_item, null);
		((ImageView) gridItem.findViewById(R.id.label_icon)).setImageResource(labelIconId);
		((TextView) gridItem.findViewById(R.id.label)).setText(labelDescriptionId);
		((TextView) gridItem.findViewById(R.id.value_int)).setText(value == null ? "?" : value.toString());
		((TextView) gridItem.findViewById(R.id.value_str)).setText(value == null ? getString(R.string.not_data) :
				AqicnResponseProcessor.getGradeDescription(value));
		((TextView) gridItem.findViewById(R.id.value_str)).setTextColor(value == null ? ContextCompat.getColor(getContext(), R.color.not_data_color)
				: AqicnResponseProcessor.getGradeColorId(value));

		binding.grid.addView(gridItem);
		return gridItem;
	}
}