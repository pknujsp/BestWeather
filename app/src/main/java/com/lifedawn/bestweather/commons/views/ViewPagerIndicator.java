package com.lifedawn.bestweather.commons.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerIndicator extends LinearLayout {
	private List<ImageView> dotList = new ArrayList<>();

	private final float PADDING = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());

	private final Drawable unselectedDrawable;
	private final Drawable selectedDrawable;


	public ViewPagerIndicator(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		TypedArray tr = context.obtainStyledAttributes(attrs, R.styleable.ViewPagerIndicator, 0, 0);

		try {
			unselectedDrawable = tr.getDrawable(R.styleable.ViewPagerIndicator_IndicatorUnselectedDotDrawable);
			selectedDrawable = tr.getDrawable(R.styleable.ViewPagerIndicator_IndicatorSelectedDotDrawable);
		} finally {
			tr.recycle();
		}
	}

	public void createDot(int position, int length) {
		removeAllViews();

		for (int i = 0; i < length; i++) {
			ImageView imageView = new ImageView(getContext());
			imageView.setPadding((int) PADDING, 0, (int) PADDING, 0);
			dotList.add(imageView);

			addView(dotList.get(i));
		}

		selectDot(position);
	}

	public void selectDot(int position) {
		for (int i = 0; i < dotList.size(); i++) {
			if (i == position) {
				dotList.get(i).setImageDrawable(selectedDrawable);
			} else {
				dotList.get(i).setImageDrawable(unselectedDrawable);
			}
		}
	}

}