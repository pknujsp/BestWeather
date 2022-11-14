package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.weathers.FragmentType;

import java.util.ArrayList;
import java.util.List;

public class DoubleWindDirectionView extends LinearLayout implements ICleaner {
	private final FragmentType fragmentType;

	private final int viewWidth;
	private final int viewHeight;
	private final int columnWidth;
	private final int imgSize;
	private final int margin;
	private final int dividerWidth;

	private List<WindDirectionObj> windDirectionObjList = new ArrayList<>();

	public DoubleWindDirectionView(Context context, FragmentType fragmentType, int viewWidth, int viewHeight, int columnWidth) {
		super(context);
		this.fragmentType = fragmentType;

		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.columnWidth = columnWidth;
		this.dividerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, getResources().getDisplayMetrics());

		int tempMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, getResources().getDisplayMetrics());
		int tempImgSize = columnWidth / 2 - tempMargin * 2;

		if (tempImgSize > viewHeight) {
			tempImgSize = tempImgSize - (tempImgSize - viewHeight);
			if (columnWidth / 2 - tempImgSize > 0) {
				tempMargin = (columnWidth / 2 - tempImgSize) / 2;
			} else {
				tempMargin = 0;
			}
		}
		imgSize = tempImgSize;
		margin = tempMargin;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}


	public void setIcons(List<WindDirectionObj> windDirectionObjList) {
		this.windDirectionObjList = windDirectionObjList;
		LinearLayout.LayoutParams directionContainerViewGroupLayoutParams = new LayoutParams(columnWidth, viewHeight);
		directionContainerViewGroupLayoutParams.gravity = Gravity.CENTER;

		LinearLayout.LayoutParams directionViewLayoutParams = new LayoutParams(imgSize, imgSize);
		directionViewLayoutParams.gravity = Gravity.CENTER;

		LinearLayout.LayoutParams dividerLayoutParams = new LayoutParams(dividerWidth, ViewGroup.LayoutParams.MATCH_PARENT);
		dividerLayoutParams.gravity = Gravity.CENTER;
		int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
		dividerLayoutParams.leftMargin = margin;
		dividerLayoutParams.rightMargin = margin;

		Context context = getContext();
		Drawable drawable = ContextCompat.getDrawable(context, R.drawable.arrow);
		int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());

		for (WindDirectionObj windDirectionObj : windDirectionObjList) {
			ImageView leftDirectionView = new ImageView(context);
			leftDirectionView.setImageDrawable(drawable);
			leftDirectionView.setPadding(padding, padding, padding, padding);
			leftDirectionView.setRotation(windDirectionObj.leftDirectionDegree + 180);
			leftDirectionView.setScaleType(ImageView.ScaleType.FIT_CENTER);

			ImageView rightDirectionView = new ImageView(context);
			rightDirectionView.setImageDrawable(drawable);
			rightDirectionView.setPadding(padding, padding, padding, padding);
			rightDirectionView.setRotation(windDirectionObj.rightDirectionDegree + 180);
			rightDirectionView.setScaleType(ImageView.ScaleType.FIT_CENTER);

			View dividerView = new View(context);
			dividerView.setBackgroundColor(Color.GRAY);

			LinearLayout container = new LinearLayout(context);
			container.setPadding(padding, padding, padding, padding);

			container.addView(leftDirectionView, directionViewLayoutParams);
			container.addView(dividerView, dividerLayoutParams);
			container.addView(rightDirectionView, directionViewLayoutParams);

			addView(container, directionContainerViewGroupLayoutParams);
		}
	}

	@Override
	public void clear() {
		windDirectionObjList.clear();
	}


	public static class WindDirectionObj {
		final int leftDirectionDegree;
		final int rightDirectionDegree;

		public WindDirectionObj(Integer leftCode, Integer rightCode) {
			this.leftDirectionDegree = leftCode;
			this.rightDirectionDegree = rightCode;
		}
	}
}
