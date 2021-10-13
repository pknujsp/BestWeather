package com.lifedawn.bestweather.weathers.simplefragment.base;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.BaseLayoutSimpleForecastBinding;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;
import com.lifedawn.bestweather.weathers.view.DateView;

import org.jetbrains.annotations.NotNull;

public class BaseSimpleForecastFragment extends Fragment implements IWeatherValues {
	protected BaseLayoutSimpleForecastBinding binding;
	protected DateView dateRow;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = BaseLayoutSimpleForecastBinding.inflate(inflater);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		binding.scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
			@Override
			public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
				if (dateRow != null) {
					dateRow.reDraw(scrollX);
				}
			}
		});
	}
	
	@Override
	public void setValuesToViews() {
	
	}
	
	protected ImageView addLabelView(int labelImgId, String labelDescription, int viewWidth, int viewHeight, int margin) {
		ImageView labelView = new ImageView(getContext());
		labelView.setImageDrawable(ContextCompat.getDrawable(getContext(), labelImgId));
		labelView.setClickable(true);
		labelView.setScaleType(ImageView.ScaleType.CENTER);
		labelView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getContext(), labelDescription, Toast.LENGTH_SHORT).show();
			}
		});
		
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(viewWidth, viewHeight);
		layoutParams.gravity = Gravity.CENTER;
		layoutParams.topMargin = margin;
		layoutParams.bottomMargin = margin;
		labelView.setLayoutParams(layoutParams);
		
		binding.labels.addView(labelView);
		return labelView;
	}
}