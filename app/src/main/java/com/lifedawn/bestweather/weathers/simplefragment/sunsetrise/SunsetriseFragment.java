package com.lifedawn.bestweather.weathers.simplefragment.sunsetrise;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.FragmentSunsetriseBinding;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class SunsetriseFragment extends Fragment implements IWeatherValues {
	private FragmentSunsetriseBinding binding;
	private SunSetRiseViewGroup sunSetRiseViewGroup;
	private Location location;
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("M.d E H:mm", Locale.getDefault());

	public enum SunSetRiseType {
		RISE, SET
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getArguments();
		location = new Location(bundle.getDouble("latitude"), bundle.getDouble("longitude"));
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

		sunSetRiseViewGroup = new SunSetRiseViewGroup(getContext(), location);
		binding.rootLayout.addView(sunSetRiseViewGroup, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		setValuesToViews();
	}


	@Override
	public void setValuesToViews() {
	}


}