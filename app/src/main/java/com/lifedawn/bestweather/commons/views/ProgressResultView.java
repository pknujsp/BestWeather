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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProgressResultView extends FrameLayout implements OnProgressViewListener, CheckSuccess {
	private ViewProgressResultBinding binding;
	private List<View> views = new ArrayList<>();
	private boolean succeed;
	private boolean btnEnabled;

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

	public void setContentView(View... contentView) {
		this.views.addAll(Arrays.asList(contentView));
		onSuccessful();
	}

	public void setBtnOnClickListener(View.OnClickListener onClickListener) {
		binding.btn.setOnClickListener(onClickListener);
		binding.btn.setVisibility(VISIBLE);
		btnEnabled = true;
	}

	@Override
	public void onSuccessful() {
		succeed = true;
		for (View v : views)
			v.setVisibility(View.VISIBLE);
		setVisibility(View.GONE);
	}

	@Override
	public void onFailed(@NonNull String text) {
		succeed = false;
		for (View v : views)
			v.setVisibility(View.VISIBLE);
		binding.status.setText(text);
		binding.status.setVisibility(VISIBLE);
		binding.progressbar.setVisibility(GONE);

		binding.btn.setVisibility(btnEnabled ? View.VISIBLE : View.GONE);
		setVisibility(View.VISIBLE);
	}

	@Override
	public void onStarted() {
		succeed = false;
		for (View v : views)
			v.setVisibility(View.GONE);

		binding.btn.setVisibility(View.GONE);
		binding.status.setVisibility(GONE);
		binding.progressbar.setVisibility(VISIBLE);

		setVisibility(View.VISIBLE);
	}


	@Override
	public boolean isSuccess() {
		return succeed;
	}

	public void setTextColor(int color) {
		binding.status.setTextColor(color);
	}
}