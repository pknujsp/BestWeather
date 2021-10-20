package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.view.View;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.theme.AppTheme;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClockView extends View {
	private final int viewWidth;
	private final int viewHeight;
	private final int columnWidth;
	private final int clockTextHeight;
	private final TextPaint clockPaint;
	private final SimpleDateFormat hFormat = new SimpleDateFormat("H", Locale.getDefault());
	
	
	private List<Date> clockList;
	
	public ClockView(Context context, int viewWidth, int viewHeight, int columnWidth) {
		super(context);
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.columnWidth = columnWidth;
		clockPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		clockPaint.setTextAlign(Paint.Align.CENTER);
		clockPaint.setTextSize(context.getResources().getDimension(R.dimen.clockValueTextSizeInSCD));
		clockPaint.setColor(AppTheme.getColor(context, R.attr.textColorInWeatherCard));
		
		Rect rect = new Rect();
		clockPaint.getTextBounds("0", 0, 1, rect);
		clockTextHeight = rect.height();
		setWillNotDraw(false);
		
	}
	
	public ClockView setClockList(List<Date> clockList) {
		this.clockList = clockList;
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
		
		float x = 0f;
		final float columnCenterX = columnWidth / 2f;
		final float y = getHeight() / 2f + clockTextHeight / 2f;
		
		int column = 0;
		for (Date clock : clockList) {
			x = columnCenterX + (columnWidth * column++);
			canvas.drawText(hFormat.format(clock), x, y, clockPaint);
		}
	}
}
