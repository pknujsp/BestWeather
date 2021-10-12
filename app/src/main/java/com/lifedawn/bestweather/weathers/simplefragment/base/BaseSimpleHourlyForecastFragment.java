package com.lifedawn.bestweather.weathers.simplefragment.base;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.ClockUtil;
import com.lifedawn.bestweather.databinding.BaseLayoutSimpleHourlyForecastBinding;
import com.lifedawn.bestweather.retrofit.responses.accuweather.twelvehoursofhourlyforecasts.TwelveHoursOfHourlyForecastsResponse;
import com.lifedawn.bestweather.weathers.simplefragment.accuweather.hourlyforecast.AccuHourlyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;
import com.lifedawn.bestweather.weathers.view.DateView;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BaseSimpleHourlyForecastFragment extends Fragment implements IWeatherValues {
	protected BaseLayoutSimpleHourlyForecastBinding binding;
	protected DateView dateRow;
	protected Map<Integer, Date> dateXMap = new HashMap<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = BaseLayoutSimpleHourlyForecastBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.weatherCardViewHeader.forecastName.setText(R.string.hourly_forecast);
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

	protected LinearLayout addValuesView(int layoutWidth, int layoutHeight, int margin) {
		LinearLayout newLayout = new LinearLayout(getContext());
		newLayout.setOrientation(LinearLayout.HORIZONTAL);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(layoutWidth, layoutHeight);
		layoutParams.topMargin = margin;
		layoutParams.bottomMargin = margin;
		newLayout.setLayoutParams(layoutParams);

		return newLayout;
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

	protected void setValueTextView(TextView textView, String value) {
		textView.setTextColor(Color.BLACK);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
		textView.setGravity(Gravity.CENTER);
		textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
		textView.setText(value == null ? "-" : value);
	}

}