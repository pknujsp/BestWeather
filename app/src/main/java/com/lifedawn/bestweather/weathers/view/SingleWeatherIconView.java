package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;

import java.util.ArrayList;
import java.util.List;

public class SingleWeatherIconView extends View {
	private final FragmentType fragmentType;
	
	private final int viewWidth;
	private final int viewHeight;
	private final int columnWidth;
	private final int imgSize;
	
	private List<WeatherIconObj> weatherIconObjList = new ArrayList<>();
	private Rect imgRect = new Rect();
	
	public SingleWeatherIconView(Context context, FragmentType fragmentType, int viewWidth, int viewHeight, int columnWidth) {
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
		
		for (WeatherIconObj weatherIconObj : weatherIconObjList) {
			weatherIconObj.img.setBounds(imgRect);
			weatherIconObj.img.draw(canvas);
			
			imgRect.offset(columnWidth, 0);
		}
	}
	
	
	public void setWeatherImgs(List<WeatherIconObj> weatherIconObjList) {
		this.weatherIconObjList = weatherIconObjList;
	}
	
	
	public static class WeatherIconObj {
		Drawable img;
		
		public WeatherIconObj(Drawable drawable) {
			img = drawable;
		}
	}
}
