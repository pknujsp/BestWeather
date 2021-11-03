package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.theme.AppTheme;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;

import java.util.ArrayList;
import java.util.List;

public class DoubleWeatherIconView extends View {
	private final FragmentType fragmentType;
	
	private final int viewWidth;
	private final int viewHeight;
	private final int columnWidth;
	private final int imgSize;
	private final int singleImgSize;
	private final int margin;
	private final int dividerWidth;
	
	private Rect leftImgRect = new Rect();
	private Rect rightImgRect = new Rect();
	private Rect singleImgRect = new Rect();
	private Rect dividerRect = new Rect();
	private Paint dividerPaint;
	
	private List<WeatherIconObj> weatherIconObjList = new ArrayList<>();
	
	public DoubleWeatherIconView(Context context, FragmentType fragmentType, int viewWidth, int viewHeight, int columnWidth) {
		super(context);
		this.fragmentType = fragmentType;
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.columnWidth = columnWidth;
		this.dividerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, getResources().getDisplayMetrics());
		
		int tempMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, getResources().getDisplayMetrics());
		
		singleImgSize = viewHeight - tempMargin * 2;
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
		switch (fragmentType) {
			case Simple:
				dividerPaint.setColor(Color.WHITE);
				break;
			default:
				dividerPaint.setColor(Color.BLACK);
				break;
		}
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
		singleImgRect.set(columnWidth / 2 - singleImgSize / 2, margin, columnWidth / 2 + singleImgSize / 2, getHeight() - margin);
		leftImgRect.set(margin, getHeight() / 2 - imgSize / 2, margin + imgSize, getHeight() / 2 + imgSize / 2);
		rightImgRect.set(leftImgRect.right + margin * 2, leftImgRect.top, leftImgRect.right + margin * 2 + imgSize, leftImgRect.bottom);
		dividerRect.set(leftImgRect.right + margin - dividerWidth / 2, leftImgRect.top, leftImgRect.right + margin + dividerWidth / 2,
				leftImgRect.bottom);
		
		for (WeatherIconObj weatherIconObj : weatherIconObjList) {
			if (weatherIconObj.isDouble) {
				weatherIconObj.leftImg.setBounds(leftImgRect);
				weatherIconObj.rightImg.setBounds(rightImgRect);
				weatherIconObj.leftImg.draw(canvas);
				weatherIconObj.rightImg.draw(canvas);
				canvas.drawRect(dividerRect, dividerPaint);
			} else {
				weatherIconObj.singleImg.setBounds(singleImgRect);
				weatherIconObj.singleImg.draw(canvas);
			}
			singleImgRect.offset(columnWidth, 0);
			leftImgRect.offset(columnWidth, 0);
			rightImgRect.offset(columnWidth, 0);
			dividerRect.offset(columnWidth, 0);
		}
	}
	
	
	public void setIcons(List<WeatherIconObj> weatherIconObjList) {
		this.weatherIconObjList = weatherIconObjList;
	}
	
	
	public static class WeatherIconObj {
		final boolean isDouble;
		Drawable leftImg;
		Drawable rightImg;
		Drawable singleImg;
		
		public WeatherIconObj(Drawable leftDrawable, Drawable rightDrawable) {
			this.leftImg = leftDrawable;
			this.rightImg = rightDrawable;
			this.isDouble = true;
		}
		
		public WeatherIconObj(Drawable drawable) {
			this.singleImg = drawable;
			this.isDouble = false;
		}
	}
}
