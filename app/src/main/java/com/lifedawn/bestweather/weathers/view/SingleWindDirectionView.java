package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.lifedawn.bestweather.R;

import java.util.ArrayList;
import java.util.List;

public class SingleWindDirectionView extends View {
	private final FragmentType fragmentType;

	private final int viewWidth;
	private final int viewHeight;
	private final int columnWidth;
	private final int imgSize;

	private List<WindDirectionObj> windDirectionObjList = new ArrayList<>();
	private Rect imgRect = new Rect();

	public SingleWindDirectionView(Context context, FragmentType fragmentType, int viewWidth, int viewHeight, int columnWidth) {
		super(context);
		this.fragmentType = fragmentType;
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.columnWidth = columnWidth;

		int tempImgSize = viewHeight;
		imgSize = tempImgSize;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(viewWidth, viewHeight);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		imgRect.set((columnWidth - imgSize) / 2, 0, (columnWidth - imgSize) / 2 + imgSize, getHeight());

		for (WindDirectionObj windDirectionObj : windDirectionObjList) {
			windDirectionObj.img.setBounds(imgRect);
			windDirectionObj.img.draw(canvas);

			imgRect.offset(columnWidth, 0);
		}
	}


	public void setIcons(List<WindDirectionObj> windDirectionObjList) {
		this.windDirectionObjList = windDirectionObjList;
		for (WindDirectionObj windDirectionObj : windDirectionObjList) {
			windDirectionObj.img = ContextCompat.getDrawable(getContext(), R.drawable.temp_icon);
		}
	}


	public static class WindDirectionObj {
		final int directionDegree;
		Drawable img;

		public WindDirectionObj(int directionDegree) {
			this.directionDegree = directionDegree;
		}
	}
}
