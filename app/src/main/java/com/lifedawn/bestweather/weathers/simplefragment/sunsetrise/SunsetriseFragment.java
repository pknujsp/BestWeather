package com.lifedawn.bestweather.weathers.simplefragment.sunsetrise;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.databinding.FragmentSunsetriseBinding;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;
import com.luckycatlabs.sunrisesunset.dto.Location;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.util.TimeZone;

public class SunsetriseFragment extends Fragment implements IWeatherValues {
	private FragmentSunsetriseBinding binding;
	private SunSetRiseViewGroup sunSetRiseViewGroup;
	private Location location;
	private Double latitude;
	private Double longitude;
	private String addressName;
	private String countryCode;
	private WeatherSourceType mainWeatherSourceType;
	private ZoneId zoneId;

	public enum SunSetRiseType {
		RISE, SET
	}

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
		zoneId = (ZoneId) bundle.getSerializable(getString(R.string.bundle_key_timezone));

		location = new Location(latitude, longitude);
	}

	@Nullable
	@org.jetbrains.annotations.Nullable
	@Override
	public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
	                         @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		binding = FragmentSunsetriseBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.weatherCardViewHeader.detailForecast.setVisibility(View.GONE);
		binding.weatherCardViewHeader.compareForecast.setVisibility(View.GONE);
		binding.weatherCardViewHeader.forecastName.setText(R.string.sun_set_rise);

		sunSetRiseViewGroup = new SunSetRiseViewGroup(getContext(), location, zoneId);
		binding.rootLayout.addView(sunSetRiseViewGroup, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		setValuesToViews();

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_TIME_TICK);

		requireActivity().registerReceiver(broadcastReceiver, intentFilter);
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() != null) {
				if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
					sunSetRiseViewGroup.refresh();
				}
			}
		}
	};

	@Override
	public void setValuesToViews() {
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		requireActivity().unregisterReceiver(broadcastReceiver);
	}
}