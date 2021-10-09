package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.lifedawn.bestweather.R;

public final class WeatherCardView extends LinearLayout {
	private TextView forecastsNameTextView;
	private TextView compareForecastsBtn;
	private TextView detailBtn;

	private String forecastsName;
	private int visibilityCompBtn;
	private int visibilityDetailBtn;

	public WeatherCardView(Context context, AttributeSet attrs) {
		super(context, attrs, R.style.forecasts_card_background);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ForecastsCardView, 0, 0);
		init(a);
	}

	public WeatherCardView(Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, R.style.forecasts_card_background);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ForecastsCardView, R.style.forecasts_card_background, 0);
		init(a);
	}

	private void init(TypedArray a) {
		try {
			forecastsName = a.getString(R.styleable.ForecastsCardView_forecasts_name);
			visibilityCompBtn = a.getInt(R.styleable.ForecastsCardView_visibility_compare_btn, View.VISIBLE);
			visibilityDetailBtn = a.getInt(R.styleable.ForecastsCardView_visibility_detail, View.VISIBLE);
		} catch (Exception e) {

		} finally {
			a.recycle();
		}

		setOrientation(VERTICAL);

		forecastsNameTextView = new TextView(getContext());
		compareForecastsBtn = new TextView(getContext());
		detailBtn = new TextView(getContext());

		forecastsNameTextView.setText(forecastsName);
		forecastsNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f);
		forecastsNameTextView.setTextColor(Color.WHITE);
		forecastsNameTextView.setId(R.id.forecasts_name_text_view);

		compareForecastsBtn.setText(getContext().getString(R.string.compare));
		compareForecastsBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
		compareForecastsBtn.setTextColor(Color.WHITE);
		compareForecastsBtn.setId(R.id.compare_forecasts_btn);

		detailBtn.setText(getContext().getString(R.string.detail));
		detailBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
		detailBtn.setTextColor(Color.WHITE);
		detailBtn.setId(R.id.detail_forecasts_btn);

		RelativeLayout headerRelativeLayout = new RelativeLayout(getContext());
		final int leftRightPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, getResources().getDisplayMetrics());
		final int topPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, getResources().getDisplayMetrics());
		headerRelativeLayout.setPadding(leftRightPadding, topPadding, leftRightPadding, 0);

		addView(headerRelativeLayout);

		RelativeLayout.LayoutParams forecastsNameTextViewLayoutParams =
				new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		forecastsNameTextViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		forecastsNameTextViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		forecastsNameTextView.setLayoutParams(forecastsNameTextViewLayoutParams);

		RelativeLayout.LayoutParams compareForecastsBtnLayoutParams =
				new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		compareForecastsBtnLayoutParams.addRule(RelativeLayout.LEFT_OF, detailBtn.getId());
		compareForecastsBtnLayoutParams.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f,
				getResources().getDisplayMetrics());
		compareForecastsBtn.setLayoutParams(compareForecastsBtnLayoutParams);

		RelativeLayout.LayoutParams detailBtnLayoutParams =
				new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		detailBtnLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		detailBtnLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, forecastsNameTextView.getId());
		detailBtn.setLayoutParams(detailBtnLayoutParams);

		headerRelativeLayout.addView(forecastsNameTextView);
		headerRelativeLayout.addView(compareForecastsBtn);
		headerRelativeLayout.addView(detailBtn);

		compareForecastsBtn.setVisibility(visibilityCompBtn);
		detailBtn.setVisibility(visibilityDetailBtn);

		compareForecastsBtn.setClickable(true);
		detailBtn.setClickable(true);
	}

	public void setCompareForecastsOnClickListener(View.OnClickListener onClickListener) {
		compareForecastsBtn.setOnClickListener(onClickListener);
	}

	public void setDetailOnClickListener(View.OnClickListener onClickListener) {
		detailBtn.setOnClickListener(onClickListener);
	}
}
