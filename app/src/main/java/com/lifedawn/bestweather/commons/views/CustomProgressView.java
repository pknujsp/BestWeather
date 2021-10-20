package com.lifedawn.bestweather.commons.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.theme.AppTheme;

public class CustomProgressView extends LinearLayout implements OnProgressViewListener, CheckSuccess {
	private TextView progressStatusTextView;
	private CircularProgressIndicator progressView;
	private View contentView;
	private boolean succeed;
	
	
	public CustomProgressView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	
	private void init() {
		setOrientation(VERTICAL);
		int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, getResources().getDisplayMetrics());
		setPadding(padding, padding, padding, padding);
		setBackgroundResource(R.drawable.progressview_background);
		setGravity(Gravity.CENTER);
		
		progressStatusTextView = new TextView(getContext());
		progressStatusTextView.setId(R.id.progress_status_textview);
		progressStatusTextView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
		progressStatusTextView.setText(null);
		progressStatusTextView.setTextColor(AppTheme.getColor(getContext(), R.attr.textColorInProgressView));
		progressStatusTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
		
		progressView = new CircularProgressIndicator(getContext());
		progressView.setIndicatorColor(ContextCompat.getColor(getContext(), R.color.design_default_color_primary));
		progressView.setIndeterminate(true);
		
		LinearLayout.LayoutParams statusTextViewLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		
		LinearLayout.LayoutParams progressViewLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		progressViewLayoutParams.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f,
				getResources().getDisplayMetrics());
		
		progressStatusTextView.setLayoutParams(statusTextViewLayoutParams);
		progressView.setLayoutParams(progressViewLayoutParams);
		
		addView(progressStatusTextView);
		addView(progressView);
	}
	
	public void setContentView(View contentView) {
		this.contentView = contentView;
		onSuccessfulProcessingData();
	}
	
	@Override
	public void onSuccessfulProcessingData() {
		succeed = true;
		progressStatusTextView.setVisibility(View.GONE);
		progressView.setVisibility(View.GONE);
		contentView.setVisibility(View.VISIBLE);
		setVisibility(View.GONE);
	}
	
	@Override
	public void onFailedProcessingData(@NonNull String text) {
		succeed = false;
		progressStatusTextView.setVisibility(View.VISIBLE);
		progressView.setVisibility(View.GONE);
		contentView.setVisibility(View.GONE);
		setVisibility(View.VISIBLE);
		
		progressStatusTextView.setText(text);
	}
	
	@Override
	public void onStartedProcessingData(String statusText) {
		succeed = false;
		progressView.setVisibility(View.VISIBLE);
		progressStatusTextView.setVisibility(View.VISIBLE);
		progressStatusTextView.setText(statusText);
		contentView.setVisibility(View.GONE);
		setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onStartedProcessingData() {
		succeed = false;
		progressView.setVisibility(View.VISIBLE);
		progressStatusTextView.setVisibility(View.GONE);
		progressStatusTextView.setText("");
		contentView.setVisibility(View.GONE);
		setVisibility(View.VISIBLE);
	}
	
	@Override
	public boolean isSuccess() {
		return succeed;
	}
}