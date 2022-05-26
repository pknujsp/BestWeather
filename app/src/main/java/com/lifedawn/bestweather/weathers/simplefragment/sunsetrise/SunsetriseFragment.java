package com.lifedawn.bestweather.weathers.simplefragment.sunsetrise;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.databinding.FragmentSunsetriseBinding;
import com.lifedawn.bestweather.weathers.WeatherFragment;
import com.lifedawn.bestweather.weathers.detailfragment.sunsetrise.DetailSunRiseSetFragment;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;
import com.luckycatlabs.sunrisesunset.dto.Location;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;

public class SunsetriseFragment extends Fragment implements IWeatherValues {
	private FragmentSunsetriseBinding binding;
	private SunSetRiseViewGroup sunSetRiseViewGroup;
	private Location location;
	private Double latitude;
	private Double longitude;
	private ZoneId zoneId;
	private Bundle bundle;
	private boolean registeredReceiver = false;
	private OnSunRiseSetListener onSunRiseSetListener;

	public void setOnSunRiseSetListener(OnSunRiseSetListener onSunRiseSetListener) {
		this.onSunRiseSetListener = onSunRiseSetListener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		bundle = savedInstanceState != null ? savedInstanceState : getArguments();
		latitude = bundle.getDouble(BundleKey.Latitude.name());
		longitude = bundle.getDouble(BundleKey.Longitude.name());
		zoneId = (ZoneId) bundle.getSerializable(BundleKey.TimeZone.name());

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

		binding.weatherCardViewHeader.detailForecast.setVisibility(View.VISIBLE);
		binding.weatherCardViewHeader.compareForecast.setVisibility(View.INVISIBLE);
		binding.weatherCardViewHeader.forecastName.setText(R.string.sun_set_rise);
		binding.weatherCardViewHeader.detailForecast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DetailSunRiseSetFragment detailSunRiseSetFragment = new DetailSunRiseSetFragment();
				detailSunRiseSetFragment.setArguments(bundle);
				String tag = DetailSunRiseSetFragment.class.getName();

				FragmentManager fragmentManager = getParentFragment().getParentFragmentManager();

				fragmentManager.beginTransaction().hide(
						fragmentManager.findFragmentByTag(WeatherFragment.class.getName())).add(R.id.fragment_container,
						detailSunRiseSetFragment, tag).addToBackStack(tag).commit();
			}
		});

		sunSetRiseViewGroup = new SunSetRiseViewGroup(getContext(), location, zoneId, new OnSunRiseSetListener() {
			@Override
			public void onCalcResult(boolean calcSuccessful, boolean night) {
				if (calcSuccessful) {
					if (!registeredReceiver) {
						registeredReceiver = true;

						//onSunRiseSetListener.onCalcResult(true, night);
						IntentFilter intentFilter = new IntentFilter();
						intentFilter.addAction(Intent.ACTION_TIME_TICK);
						requireActivity().registerReceiver(broadcastReceiver, intentFilter);
						binding.weatherCardViewHeader.detailForecast.setVisibility(View.VISIBLE);
					}
				} else {
					binding.weatherCardViewHeader.detailForecast.setVisibility(View.GONE);

					if (registeredReceiver) {
						registeredReceiver = false;
						requireActivity().unregisterReceiver(broadcastReceiver);
					} else {
						//onSunRiseSetListener.onCalcResult(false, false);
					}
				}
			}
		});
		binding.rootLayout.addView(sunSetRiseViewGroup, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (isAdded() && intent.getAction() != null) {
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
		if (registeredReceiver) {
			requireActivity().unregisterReceiver(broadcastReceiver);
		}
	}
}