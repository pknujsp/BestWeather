package com.lifedawn.bestweather.commons.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.bestweather.databinding.ViewProgressResultBinding;

import org.jetbrains.annotations.NotNull;

public class ProgressResultView extends FrameLayout implements OnProgressViewListener, CheckSuccess {
	private ViewProgressResultBinding binding;
	private View contentView;
	private boolean succeed;

	public ProgressResultView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ProgressResultView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();

	}

	public ProgressResultView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();

	}

	public ProgressResultView(@NonNull @NotNull Context context) {
		super(context);
		init();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}


	private void init() {
		binding = ViewProgressResultBinding.inflate(LayoutInflater.from(getContext()), this, true);
	}

	public void setContentView(View contentView) {
		this.contentView = contentView;
		onSuccessful();
	}

	@Override
	public void onSuccessful() {
		succeed = true;
		contentView.setVisibility(View.VISIBLE);
		setVisibility(View.GONE);
	}

	@Override
	public void onFailed(@NonNull String text) {
		succeed = false;
		contentView.setVisibility(View.GONE);
		binding.status.setText(text);
		setVisibility(View.VISIBLE);
		binding.status.setVisibility(VISIBLE);
		binding.progressbar.setVisibility(GONE);
	}

	@Override
	public void onStarted() {
		succeed = false;
		contentView.setVisibility(View.GONE);
		setVisibility(View.VISIBLE);
		binding.status.setVisibility(GONE);
		binding.progressbar.setVisibility(VISIBLE);
	}


	@Override
	public boolean isSuccess() {
		return succeed;
	}

	public void setTextColor(int color) {
		binding.status.setTextColor(color);
	}
}