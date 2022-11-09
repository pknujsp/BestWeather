package com.lifedawn.bestweather.weathers.detailfragment.base.dialogfragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.asynclayoutinflater.view.AsyncLayoutInflater;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.commons.views.ProgressDialog;
import com.lifedawn.bestweather.databinding.DialogFragmentDetailForecastBinding;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public abstract class BaseDetailDialogFragment extends DialogFragment {
	protected DialogFragmentDetailForecastBinding binding;
	protected CompositePageTransformer compositePageTransformer;
	protected int firstSelectedPosition;
	protected Bundle bundle;

	@Override
	public void onAttach(@NonNull @NotNull Context context) {
		super.onAttach(context);
	}

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		bundle = savedInstanceState != null ? savedInstanceState : getArguments();
		firstSelectedPosition = bundle.getInt("FirstSelectedPosition");
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		Dialog dialog = new Dialog(requireContext().getApplicationContext(), R.style.DialogTransparent);
		return dialog;
	}

	@Nullable
	@org.jetbrains.annotations.Nullable
	@Override
	public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		binding = DialogFragmentDetailForecastBinding.inflate(inflater);

		final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, getResources().getDisplayMetrics());

		binding.detailForecastViewPager.setOffscreenPageLimit(2);
		binding.detailForecastViewPager.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);

		compositePageTransformer = new CompositePageTransformer();
		compositePageTransformer.addTransformer(new MarginPageTransformer(margin));
		compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
			@Override
			public void transformPage(@NonNull View page, float position) {
				float r = 1 - Math.abs(position);
				page.setScaleY(0.8f + r * 0.2f);
			}
		});
		binding.detailForecastViewPager.setPageTransformer(compositePageTransformer);
		binding.detailForecastViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position);
			}
		});

		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	protected void setTabCustomView() {
		TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(binding.tabLayout, binding.detailForecastViewPager,
				new TabLayoutMediator.TabConfigurationStrategy() {
					@Override
					public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
					}
				}
		);
		tabLayoutMediator.attach();
	}

	@Override
	public void onStart() {
		super.onStart();
		getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
