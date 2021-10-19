package com.lifedawn.bestweather.weathers.comparison.base;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.databinding.BaseLayoutForecastComparisonBinding;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;
import com.lifedawn.bestweather.weathers.view.DateView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BaseForecastComparisonFragment extends Fragment implements IWeatherValues {
	protected BaseLayoutForecastComparisonBinding binding;
	protected DateView dateRow;
	protected SharedPreferences sharedPreferences;
	protected ValueUnits tempUnit;
	protected ValueUnits windUnit;
	protected ValueUnits visibilityUnit;
	protected ValueUnits clockUnit;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		tempUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_temp), ValueUnits.celsius.name()));
		windUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_temp), ValueUnits.mPerSec.name()));
		visibilityUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_temp), ValueUnits.km.name()));
		clockUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_temp), ValueUnits.clock24.name()));
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = BaseLayoutForecastComparisonBinding.inflate(inflater);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.customProgressView.setContentView(binding.rootScrollView);
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
	
	
	protected View addWeatherDataSourceIconView(MainProcessing.WeatherSourceType weatherSourceType, int topMargin, int bottomMargin,
			int leftRightMargin) {
		TextView view = (TextView) getLayoutInflater().inflate(R.layout.weather_data_source_icon_view, null);
		switch (weatherSourceType) {
			case KMA:
				view.setText("K");
				view.setBackgroundTintList(getContext().getColorStateList(R.color.kma_icon_color));
				break;
			case ACCU_WEATHER:
				view.setText("A");
				view.setBackgroundTintList(getContext().getColorStateList(R.color.accu_icon_color));
				break;
			case OPEN_WEATHER_MAP:
				view.setText("O");
				view.setBackgroundTintList(getContext().getColorStateList(R.color.owm_icon_color));
				break;
		}
		
		int iconSize = (int) getResources().getDimension(R.dimen.label_icon_size);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(iconSize, iconSize);
		layoutParams.topMargin = topMargin;
		layoutParams.bottomMargin = bottomMargin;
		layoutParams.leftMargin = leftRightMargin;
		layoutParams.rightMargin = leftRightMargin;
		
		binding.labels.addView(view, layoutParams);
		return view;
	}
	
	protected ImageView addLabelView(int labelImgId, String labelDescription, int topMargin, int bottomMargin, int leftRightMargin) {
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
		
		int iconSize = (int) getResources().getDimension(R.dimen.label_icon_size);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(iconSize, iconSize);
		layoutParams.gravity = Gravity.CENTER;
		layoutParams.topMargin = topMargin;
		layoutParams.bottomMargin = bottomMargin;
		layoutParams.leftMargin = leftRightMargin;
		layoutParams.rightMargin = leftRightMargin;
		labelView.setLayoutParams(layoutParams);
		
		binding.labels.addView(labelView);
		return labelView;
	}
	
}