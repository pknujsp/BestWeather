package com.lifedawn.bestweather.settings.fragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.interfaces.IAppbarTitle;
import com.lifedawn.bestweather.databinding.FragmentSettingsMainBinding;

import org.jetbrains.annotations.NotNull;

public class SettingsMainFragment extends Fragment implements IAppbarTitle {
	private FragmentSettingsMainBinding binding;

	private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
		@Override
		public void handleOnBackPressed() {
			if (!getChildFragmentManager().popBackStackImmediate()) {
				getParentFragmentManager().popBackStack();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentSettingsMainBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		SettingsFragment settingsFragment = new SettingsFragment(this);
		getChildFragmentManager().beginTransaction().add(binding.fragmentContainer.getId(), settingsFragment).commitNow();

		binding.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onBackPressedCallback.handleOnBackPressed();
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		onBackPressedCallback.remove();
	}

	@Override
	public void setAppbarTitle(String title) {
		binding.fragmentTitle.setText(title);
	}
}