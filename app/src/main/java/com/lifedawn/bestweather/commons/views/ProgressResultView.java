package com.lifedawn.bestweather.commons.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.theme.AppTheme;

public class ProgressResultView extends LinearLayout implements OnProgressViewListener, CheckSuccess {
	private TextView statusTextView;
	private View contentView;
	private boolean succeed;

	public ProgressResultView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}


	private void init() {
		setOrientation(VERTICAL);
		int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, getResources().getDisplayMetrics());
		setPadding(padding, padding, padding, padding);
		setBackgroundResource(R.drawable.progressview_background);
		setGravity(Gravity.CENTER);

		statusTextView = new TextView(getContext());
		statusTextView.setId(R.id.progress_status_textview);
		statusTextView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
		statusTextView.setText(null);
		statusTextView.setTextColor(AppTheme.getColor(getContext(), R.attr.textColorInProgressView));
		statusTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);

		LinearLayout.LayoutParams statusTextViewLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		statusTextView.setLayoutParams(statusTextViewLayoutParams);

		addView(statusTextView);
	}

	public void setContentView(View contentView) {
		this.contentView = contentView;
		onSuccessfulProcessingData();
	}

	@Override
	public void onSuccessfulProcessingData() {
		succeed = true;
		contentView.setVisibility(View.VISIBLE);
		setVisibility(View.GONE);
	}

	@Override
	public void onFailedProcessingData(@NonNull String text) {
		succeed = false;
		contentView.setVisibility(View.GONE);
		setVisibility(View.VISIBLE);

		statusTextView.setText(text);
	}

	@Override
	public void onStartedProcessingData() {
		succeed = false;
		contentView.setVisibility(View.GONE);
		setVisibility(View.GONE);
	}


	@Override
	public boolean isSuccess() {
		return succeed;
	}
}