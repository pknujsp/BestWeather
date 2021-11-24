package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.databinding.DataBindingUtil;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.ViewNotScrolledBinding;

public class NotScrolledView extends FrameLayout {
	private ViewNotScrolledBinding binding;

	public NotScrolledView(Context context) {
		super(context);
		init();
	}

	public NotScrolledView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public NotScrolledView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public NotScrolledView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	private void init() {
		binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.view_not_scrolled, this, true);
	}

	public void setImg(int id) {
		binding.img.setImageResource(id);
	}

	public void setText(String text) {
		binding.text.setText(text);
	}


	public void reDraw(int newX) {
		binding.getRoot().setX(newX);
		invalidate();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}
}
