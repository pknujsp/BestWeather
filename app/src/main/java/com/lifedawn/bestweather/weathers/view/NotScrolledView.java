package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.ViewNotScrolledBinding;

import java.util.Arrays;
import java.util.Collections;

public class NotScrolledView extends FrameLayout implements ICleaner {
	private ViewNotScrolledBinding binding;

	public NotScrolledView(Context context) {
		super(context);
		init(null);
	}

	public NotScrolledView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public NotScrolledView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(attrs);
	}

	public NotScrolledView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(attrs);
	}

	private void init(@Nullable AttributeSet attrs) {
		int iconVisibility = View.GONE;
		String text = "ss";
		int iconSrc = R.drawable.temp_icon;

		TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.NotScrolledView, 0, 0);
		try {
			iconVisibility = typedArray.getInt(R.styleable.NotScrolledView_iconVisibility, iconVisibility);
			text = typedArray.getString(R.styleable.NotScrolledView_android_text);
			iconSrc = typedArray.getResourceId(R.styleable.NotScrolledView_iconSrc, iconSrc);
		} catch (Exception e) {

		} finally {
			typedArray.recycle();
		}

		binding = ViewNotScrolledBinding.inflate(LayoutInflater.from(getContext()), this, true);
		binding.img.setVisibility(iconVisibility);
		binding.img.setImageResource(iconSrc);
		binding.text.setText(text);
		binding.text.setTextColor(Color.GRAY);
	}

	public void setImg(int id) {
		binding.img.setImageResource(id);
		binding.img.setVisibility(VISIBLE);
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

	@Override
	public void clear() {
		binding.img.setImageDrawable(null);
		removeAllViewsInLayout();
		binding = null;
	}
}
