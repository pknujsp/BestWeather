package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.lifedawn.bestweather.R;

import java.util.List;

public class WindDirectionView extends View {
	private final int viewWidth;
	private final int viewHeight;
	private final int columnWidth;
	private final Drawable directionImg;
	private final int imgSize;
	private List<Integer> directionValueList;
	
	public WindDirectionView(Context context, int viewWidth, int viewHeight, int columnWidth) {
		super(context);
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.columnWidth = columnWidth;
		
		imgSize = (int) getResources().getDimension(R.dimen.wind_direction_img_size_in_simple_forecast_view);
		directionImg = ContextCompat.getDrawable(getContext(), R.drawable.temp_icon);
		
		setWillNotDraw(false);
	}
	
	public WindDirectionView setDirectionValueList(List<Integer> directionValueList) {
		this.directionValueList = directionValueList;
		return this;
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
		super.onDraw(canvas);
		
		final int leftInColumn = (columnWidth - imgSize) / 2;
		int left = 0;
		int right = 0;
		final int top = (getHeight() - imgSize) / 2;
		final int bottom = top + imgSize;
		
		int column = 0;
		for (Integer direction : directionValueList) {
			left = leftInColumn + (columnWidth * column++);
			right = left + imgSize;
			
			directionImg.setBounds(left, top, right, bottom);
			canvas.rotate(direction);
			directionImg.draw(canvas);
		}
	}
	
}
