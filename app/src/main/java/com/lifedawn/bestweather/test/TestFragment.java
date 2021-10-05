package com.lifedawn.bestweather.test;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.FragmentTestBinding;
import com.lifedawn.bestweather.weathers.dataprocessing.MainProcessing;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class TestFragment extends Fragment {
	private FragmentTestBinding binding;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentTestBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.download.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// 내외동 좌표:35.235421,128.868227
				Set<MainProcessing.WeatherSourceType> weatherSourceTypeSet = new HashSet<>();
				weatherSourceTypeSet.add(MainProcessing.WeatherSourceType.MET_NORWAY);
				MainProcessing.downloadWeatherData(getActivity().getApplicationContext(), "35.235421", "128.868227", weatherSourceTypeSet);
			}
		});
	}
}