package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;

import com.lifedawn.bestweather.commons.classes.ClockUtil;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.util.SunsetriseUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SingleWeatherIconView extends View {
	private final int viewWidth;
	private final int viewHeight;
	private final int columnWidth;
	private final int imgSize;

	private List<WeatherIconObj> weatherIconObjList = new ArrayList<>();
	private Rect imgRect = new Rect();

	public SingleWeatherIconView(Context context, int viewWidth, int viewHeight, int columnWidth) {
		super(context);
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


	/*
	public void setWeatherImgs(WeatherSourceType weatherSourceType, List<WeatherIconObj> weatherIconObjList,
	                           long firstFcstDateTime, long lastFcstDateTime, double latitude, double longitude) {
		this.weatherIconObjList = weatherIconObjList;
		List<SunsetriseUtil.SunSetRiseData> setRiseDataList = SunsetriseUtil.getSunsetRiseList(firstFcstDateTime, lastFcstDateTime,
				latitude, longitude);

		boolean day = false;
		for (WeatherIconObj weatherIconObj : weatherIconObjList) {
			for (SunsetriseUtil.SunSetRiseData sunSetRiseData : setRiseDataList) {
				if (ClockUtil.areSameDate(sunSetRiseData.getDate().getTime(), weatherIconObj.dateTime.getTime())) {
					day = weatherIconObj.dateTime.getTime() > sunSetRiseData.getSunrise().getTime() && weatherIconObj.dateTime.getTime() < sunSetRiseData.getSunset().getTime();
					// add drawable to list

					break;
				}
			}
		}
	}

	 */


	public static class WeatherIconObj {
		final String code;
		final LocalDateTime dateTime;
		Drawable img;

		public WeatherIconObj(String code, LocalDateTime dateTime) {
			this.code = code;
			this.dateTime = dateTime;
		}
	}
}
