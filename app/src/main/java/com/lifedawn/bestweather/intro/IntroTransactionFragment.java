package com.lifedawn.bestweather.intro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.FragmentIntroTransactionBinding;

import org.jetbrains.annotations.NotNull;

public class IntroTransactionFragment extends Fragment {
	private FragmentIntroTransactionBinding binding;
	private OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
		@Override
		public void handleOnBackPressed() {
			if (!getChildFragmentManager().popBackStackImmediate()) {
				getActivity().finish();
			}
		}
	};
	
	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
	}
	
	@Nullable
	@org.jetbrains.annotations.Nullable
	@Override
	public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
			@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		binding = FragmentIntroTransactionBinding.inflate(inflater);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		IntroFragment introFragment = new IntroFragment();
		getChildFragmentManager().beginTransaction().add(binding.fragmentContainer.getId(), introFragment,
				getString(R.string.tag_intro_fragment)).commit();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		onBackPressedCallback.remove();
	}
}
