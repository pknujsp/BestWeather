package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.weathers.FragmentType;

import java.util.List;

public class SingleWindDirectionView extends LinearLayout implements ICleaner {
	private final FragmentType fragmentType;
	private final int viewWidth;
	private final int viewHeight;
	private final int columnWidth;
	private final int iconSize;

	private List<Integer> windDirectionObjList;


	public SingleWindDirectionView(Context context, FragmentType fragmentType, int viewWidth, int viewHeight, int columnWidth) {
		super(context);
		this.fragmentType = fragmentType;
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.columnWidth = columnWidth;

		int tempImgSize = viewHeight;
		iconSize = tempImgSize;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}


	public void setWindDirectionObjList(List<Integer> windDirectionObjList) {
		this.windDirectionObjList = windDirectionObjList;
		LinearLayout.LayoutParams layoutParams = new LayoutParams(columnWidth, viewHeight);
		layoutParams.gravity = Gravity.CENTER;
		Context context = getContext();
		Drawable drawable = ContextCompat.getDrawable(context, R.drawable.arrow);
		int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics());

		for (Integer degree : windDirectionObjList) {
			ImageView directionView = new ImageView(context);
			directionView.setImageDrawable(drawable);
			directionView.setPadding(padding, padding, padding, padding);
			directionView.setRotation(degree + 180);
			addView(directionView, layoutParams);
		}
	}


	@Override
	public void clear() {
		if (windDirectionObjList != null)
			windDirectionObjList.clear();
	}
}
