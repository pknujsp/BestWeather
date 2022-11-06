package com.lifedawn.bestweather.weathers.simplefragment.base;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.asynclayoutinflater.view.AsyncLayoutInflater;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.databinding.BaseLayoutSimpleCurrentConditionsBinding;
import com.lifedawn.bestweather.databinding.LoadingViewAsyncBinding;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.weathers.WeatherFragment;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class BaseSimpleCurrentConditionsFragment extends Fragment implements IWeatherValues, WeatherFragment.ITextColor, AsyncLayoutInflater.OnInflateFinishedListener {
	protected BaseLayoutSimpleCurrentConditionsBinding binding;
	protected LoadingViewAsyncBinding asyncBinding;
	protected ValueUnits tempUnit;
	protected Double latitude;
	protected Double longitude;
	protected String addressName;
	protected String countryCode;
	protected WeatherProviderType mainWeatherProviderType;
	protected ZoneId zoneId;
	protected ZoneOffset zoneOffset;
	protected Bundle bundle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bundle = savedInstanceState != null ? savedInstanceState : getArguments();

		tempUnit = MyApplication.VALUE_UNIT_OBJ.getTempUnit();

		latitude = bundle.getDouble(BundleKey.Latitude.name());
		longitude = bundle.getDouble(BundleKey.Longitude.name());
		addressName = bundle.getString(BundleKey.AddressName.name());
		countryCode = bundle.getString(BundleKey.CountryCode.name());
		mainWeatherProviderType = (WeatherProviderType) bundle.getSerializable(
				BundleKey.WeatherProvider.name());
		zoneId = (ZoneId) bundle.getSerializable(BundleKey.TimeZone.name());

		zoneOffset = ZonedDateTime.now(zoneId).getOffset();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		asyncBinding = LoadingViewAsyncBinding.inflate(inflater, container, false);

		final AsyncLayoutInflater asyncLayoutInflater = new AsyncLayoutInflater(requireContext());
		asyncLayoutInflater.inflate(R.layout.base_layout_simple_current_conditions, container, this::onInflateFinished);
		return asyncBinding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}


	@Override
	public void setValuesToViews() {
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
		asyncBinding = null;
	}

	@Override
	public void changeColor(int color) {
		binding.sky.setTextColor(color);
		binding.precipitation.setTextColor(color);
		binding.humidity.setTextColor(color);
		binding.windDirection.setTextColor(color);
		binding.wind.setTextColor(color);
		binding.airQualityLabel.setTextColor(color);
		binding.airQuality.setTextColor(color);
		binding.temperature.setTextColor(color);
		binding.tempUnit.setTextColor(color);
		binding.feelsLikeTempLabel.setTextColor(color);
		binding.feelsLikeTemp.setTextColor(color);
		binding.feelsLikeTempUnit.setTextColor(color);
		binding.tempDescription.setTextColor(color);
		binding.windDirectionArrow.setImageTintList(ColorStateList.valueOf(color));
	}

	@Override
	public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
		binding = BaseLayoutSimpleCurrentConditionsBinding.bind(view);
		asyncBinding.getRoot().addView(binding.getRoot());
		asyncBinding.progressCircular.setVisibility(View.GONE);
		asyncBinding.progressCircular.pauseAnimation();
	}
}