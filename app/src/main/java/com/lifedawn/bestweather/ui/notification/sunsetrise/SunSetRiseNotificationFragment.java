package com.lifedawn.bestweather.ui.notification.sunsetrise;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.FragmentSunSetRiseNotificationBinding;
import com.lifedawn.bestweather.data.MyApplication;


public class SunSetRiseNotificationFragment extends Fragment {
	private FragmentSunSetRiseNotificationBinding binding;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentSunSetRiseNotificationBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) binding.toolbar.getRoot().getLayoutParams();
		layoutParams.topMargin = MyApplication.getStatusBarHeight();
		binding.toolbar.getRoot().setLayoutParams(layoutParams);
		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});
		binding.toolbar.fragmentTitle.setText(R.string.sun_set_rise_notification);

		binding.sunRiseSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			boolean init = true;

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!init) {
					SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext().getApplicationContext());
					sharedPreferences.edit().putBoolean(getString(R.string.pref_key_sun_rise_notification), isChecked).commit();
				} else {
					init = false;
				}
			}
		});

		binding.sunSetSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			boolean init = true;

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!init) {
					SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext().getApplicationContext());
					sharedPreferences.edit().putBoolean(getString(R.string.pref_key_sun_set_notification), isChecked).commit();
				} else {
					init = false;
				}
			}
		});

		binding.sunriseTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showTimePicker(new OnTimeListener() {
					@Override
					public void onResult(long value) {

					}
				});
			}
		});

		binding.sunsetTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showTimePicker(new OnTimeListener() {
					@Override
					public void onResult(long value) {

					}
				});
			}
		});
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	private void showTimePicker(OnTimeListener onTimeListener) {

	}

	public interface OnTimeListener {
		void onResult(long value);
	}
}