package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.lifedawn.bestweather.commons.classes.ClockUtil;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.util.SunsetriseUtil;

import java.util.ArrayList;
import java.util.List;

public class WeatherIconView extends View {
	private List<Drawable> weatherImgList = new ArrayList<>();

	public WeatherIconView(Context context) {
		super(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawImages(canvas);
	}

	private void drawImages(Canvas canvas) {
		final int COLUMN_WIDTH = getWidth() / weatherImgList.size();
		final int RADIUS = COLUMN_WIDTH / 2;
		final int TOP = 0;
		final int BOTTOM = getHeight();
		final int LEFT = COLUMN_WIDTH / 2 - RADIUS;
		final int RIGHT = COLUMN_WIDTH / 2 + RADIUS;
		final Rect RECT = new Rect(LEFT, TOP, RIGHT, BOTTOM);

		for (Drawable image : weatherImgList) {
			image.setBounds(RECT);
			image.draw(canvas);

			RECT.offset(COLUMN_WIDTH, 0);
		}
	}

	public void setWeatherImgs(MainProcessing.WeatherSourceType weatherSourceType, List<WeatherIconObj> weatherIconObjs,
	                           long firstFcstDateTime,
	                           long lastFcstDateTime, double latitude, double longitude) {
		List<SunsetriseUtil.SunSetRiseData> setRiseDataList = SunsetriseUtil.getSunsetRiseList(firstFcstDateTime, lastFcstDateTime,
				latitude, longitude);

		boolean day = false;
		for (WeatherIconObj weatherIconObj : weatherIconObjs) {
			for (SunsetriseUtil.SunSetRiseData sunSetRiseData : setRiseDataList) {
				if (ClockUtil.areSameDate(sunSetRiseData.getDate().getTime(), weatherIconObj.fcstDateTime)) {
					day = weatherIconObj.fcstDateTime > sunSetRiseData.getSunrise().getTime() && weatherIconObj.fcstDateTime < sunSetRiseData.getSunset().getTime();
					// add drawable to list

					break;
				}
			}
		}
	}


	public static class WeatherIconObj {
		final String code;
		final long fcstDateTime;

		public WeatherIconObj(String code, long fcstDateTime) {
			this.code = code;
			this.fcstDateTime = fcstDateTime;
		}
	}
}
