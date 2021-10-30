package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.theme.AppTheme;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;

import java.util.ArrayList;
import java.util.List;

public class DoubleWeatherIconView extends View {
	private final int viewWidth;
	private final int viewHeight;
	private final int columnWidth;
	private final int imgSize;
	private final int margin;
	private final int dividerWidth;

	private Rect leftImgRect = new Rect();
	private Rect rightImgRect = new Rect();
	private Rect dividerRect = new Rect();
	private Paint dividerPaint;

	private List<WeatherIconObj> weatherIconObjList = new ArrayList<>();

	public DoubleWeatherIconView(Context context, int viewWidth, int viewHeight, int columnWidth) {
		super(context);
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

		dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dividerPaint.setColor(AppTheme.getColor(context, R.attr.lineColor));
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
		leftImgRect.set(margin, getHeight() / 2 - imgSize / 2, margin + imgSize, getHeight() / 2 + imgSize / 2);
		rightImgRect.set(leftImgRect.right + margin * 2, leftImgRect.top, leftImgRect.right + margin * 2 + imgSize, leftImgRect.bottom);
		dividerRect.set(leftImgRect.right + margin - dividerWidth / 2, leftImgRect.top, leftImgRect.right + margin + dividerWidth / 2
				, leftImgRect.bottom);

		for (WeatherIconObj weatherIconObj : weatherIconObjList) {
			weatherIconObj.leftImg.setBounds(leftImgRect);
			weatherIconObj.rightImg.setBounds(rightImgRect);
			weatherIconObj.leftImg.draw(canvas);
			weatherIconObj.rightImg.draw(canvas);
			canvas.drawRect(dividerRect, dividerPaint);

			leftImgRect.offset(columnWidth, 0);
			rightImgRect.offset(columnWidth, 0);
			dividerRect.offset(columnWidth, 0);
		}
	}


	public void setIcons(WeatherSourceType weatherSourceType, List<WeatherIconObj> weatherIconObjList) {
		this.weatherIconObjList = weatherIconObjList;
		for (WeatherIconObj weatherIconObj : weatherIconObjList) {
			weatherIconObj.leftImg = ContextCompat.getDrawable(getContext(), R.drawable.temp_icon);
			weatherIconObj.rightImg = ContextCompat.getDrawable(getContext(), R.drawable.temp_icon);
		}
	}


	public static class WeatherIconObj {
		String leftCode;
		String rightCode;
		Drawable leftImg;
		Drawable rightImg;

		public WeatherIconObj(String leftCode, String rightCode) {
			this.leftCode = leftCode;
			this.rightCode = rightCode;
		}
	}
}
