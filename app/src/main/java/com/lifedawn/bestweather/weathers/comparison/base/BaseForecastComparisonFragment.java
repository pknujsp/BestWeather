package com.lifedawn.bestweather.weathers.comparison.base;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.databinding.BaseLayoutForecastComparisonBinding;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;
import com.lifedawn.bestweather.weathers.view.DateView;

import org.jetbrains.annotations.NotNull;

import java.util.TimeZone;

public class BaseForecastComparisonFragment extends Fragment implements IWeatherValues {
	protected BaseLayoutForecastComparisonBinding binding;
	protected DateView dateRow;
	protected SharedPreferences sharedPreferences;
	protected ValueUnits tempUnit;
	protected ValueUnits windUnit;
	protected ValueUnits visibilityUnit;
	protected ValueUnits clockUnit;

	protected Double latitude;
	protected Double longitude;
	protected String addressName;
	protected String countryCode;
	protected WeatherSourceType mainWeatherSourceType;
	protected TimeZone timeZone;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

		tempUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_temp), ValueUnits.celsius.name()));
		windUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_wind), ValueUnits.mPerSec.name()));
		visibilityUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_visibility), ValueUnits.km.name()));
		clockUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_clock), ValueUnits.clock24.name()));

		Bundle bundle = getArguments();
		latitude = bundle.getDouble(getString(R.string.bundle_key_latitude));
		longitude = bundle.getDouble(getString(R.string.bundle_key_longitude));
		addressName = bundle.getString(getString(R.string.bundle_key_address_name));
		countryCode = bundle.getString(getString(R.string.bundle_key_country_code));
		timeZone = (TimeZone) bundle.getSerializable(getString(R.string.bundle_key_timezone));
		mainWeatherSourceType = (WeatherSourceType) bundle.getSerializable(
				getString(R.string.bundle_key_main_weather_data_source));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = BaseLayoutForecastComparisonBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});

		binding.kmaLabelLayout.weatherDataSourceIconView.weatherDataSourceIcon.setText("K");
		binding.kmaLabelLayout.weatherDataSourceIconView.weatherDataSourceIcon.setBackgroundTintList(
				getContext().getColorStateList(R.color.kma_icon_color));

		binding.accuLabelLayout.weatherDataSourceIconView.weatherDataSourceIcon.setText("A");
		binding.accuLabelLayout.weatherDataSourceIconView.weatherDataSourceIcon.setBackgroundTintList(
				getContext().getColorStateList(R.color.accu_icon_color));

		binding.owmLabelLayout.weatherDataSourceIconView.weatherDataSourceIcon.setText("O");
		binding.owmLabelLayout.weatherDataSourceIconView.weatherDataSourceIcon.setBackgroundTintList(
				getContext().getColorStateList(R.color.owm_icon_color));

		final View.OnClickListener labelIconOnClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Toast.makeText(getContext(), view.getContentDescription(), Toast.LENGTH_SHORT).show();
			}
		};

		binding.kmaLabelLayout.weatherLabel.setOnClickListener(labelIconOnClickListener);
		binding.kmaLabelLayout.temperatureLabel.setOnClickListener(labelIconOnClickListener);
		binding.kmaLabelLayout.precipitationVolumeLabel.setOnClickListener(labelIconOnClickListener);
		binding.kmaLabelLayout.popLabel.setOnClickListener(labelIconOnClickListener);

		binding.accuLabelLayout.weatherLabel.setOnClickListener(labelIconOnClickListener);
		binding.accuLabelLayout.temperatureLabel.setOnClickListener(labelIconOnClickListener);
		binding.accuLabelLayout.precipitationVolumeLabel.setOnClickListener(labelIconOnClickListener);
		binding.accuLabelLayout.popLabel.setOnClickListener(labelIconOnClickListener);

		binding.owmLabelLayout.weatherLabel.setOnClickListener(labelIconOnClickListener);
		binding.owmLabelLayout.temperatureLabel.setOnClickListener(labelIconOnClickListener);
		binding.owmLabelLayout.precipitationVolumeLabel.setOnClickListener(labelIconOnClickListener);
		binding.owmLabelLayout.popLabel.setOnClickListener(labelIconOnClickListener);

		binding.scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
			@Override
			public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
				if (dateRow != null) {
					dateRow.reDraw(scrollX);
				}
			}
		});
	}

	@Override
	public void setValuesToViews() {

	}

}