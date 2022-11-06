package com.lifedawn.bestweather.weathers.simplefragment.sunsetrise;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.asynclayoutinflater.view.AsyncLayoutInflater;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.databinding.BaseLayoutSimpleCurrentConditionsBinding;
import com.lifedawn.bestweather.databinding.FragmentSunsetriseBinding;
import com.lifedawn.bestweather.databinding.LoadingViewAsyncBinding;
import com.lifedawn.bestweather.weathers.WeatherFragment;
import com.lifedawn.bestweather.weathers.detailfragment.sunsetrise.DetailSunRiseSetFragment;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherFragmentViewModel;
import com.luckycatlabs.sunrisesunset.dto.Location;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.util.Objects;

public class SunsetriseFragment extends Fragment implements IWeatherValues, AsyncLayoutInflater.OnInflateFinishedListener {
	private FragmentSunsetriseBinding binding;
	private LoadingViewAsyncBinding asyncBinding;
	private SunSetRiseViewGroup sunSetRiseViewGroup;
	private Location location;
	private Double latitude;
	private Double longitude;
	private ZoneId zoneId;
	private Bundle bundle;
	private boolean registeredReceiver = false;
	private OnSunRiseSetListener onSunRiseSetListener;
	private WeatherFragmentViewModel weatherFragmentViewModel;

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
		weatherFragmentViewModel = new ViewModelProvider(requireParentFragment()).get(WeatherFragmentViewModel.class);

	}

	@Nullable
	@org.jetbrains.annotations.Nullable
	@Override
	public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
	                         @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		asyncBinding = LoadingViewAsyncBinding.inflate(inflater, container, false);

		final AsyncLayoutInflater asyncLayoutInflater = new AsyncLayoutInflater(requireContext());
		asyncLayoutInflater.inflate(R.layout.fragment_sunsetrise, container, this);
		return asyncBinding.getRoot();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		asyncBinding = null;
		binding = null;
	}


	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
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
		if (registeredReceiver) {
			Objects.requireNonNull(requireActivity()).unregisterReceiver(broadcastReceiver);
		}
		super.onDestroy();
	}

	@Override
	public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
		binding = FragmentSunsetriseBinding.bind(view);
		asyncBinding.getRoot().addView(binding.getRoot());

		binding.weatherCardViewHeader.detailForecast.setVisibility(View.VISIBLE);
		binding.weatherCardViewHeader.compareForecast.setVisibility(View.INVISIBLE);
		binding.weatherCardViewHeader.forecastName.setText(R.string.sun_set_rise);
		binding.weatherCardViewHeader.detailForecast.setOnClickListener(v -> {
			DetailSunRiseSetFragment detailSunRiseSetFragment = new DetailSunRiseSetFragment();
			detailSunRiseSetFragment.setArguments(bundle);
			String tag = DetailSunRiseSetFragment.class.getName();

			FragmentManager fragmentManager = getParentFragment().getParentFragmentManager();

			fragmentManager.beginTransaction().hide(
					fragmentManager.findFragmentByTag(WeatherFragment.class.getName())).add(R.id.fragment_container,
					detailSunRiseSetFragment, tag).addToBackStack(tag).commit();
		});

		sunSetRiseViewGroup = new SunSetRiseViewGroup(getContext(), location, zoneId, (calcSuccessful, night) -> {
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
		});
		binding.rootLayout.addView(sunSetRiseViewGroup, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		asyncBinding.progressCircular.setVisibility(View.GONE);
		asyncBinding.progressCircular.pauseAnimation();
		weatherFragmentViewModel.onResumeWithAsync(this);
	}
}