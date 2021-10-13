package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.view.View;

import com.lifedawn.bestweather.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateView extends View {
	private final TextPaint dateTextPaint;
	private final int viewWidth;
	private final int viewHeight;
	private final int textHeight;
	private final SimpleDateFormat MdE = new SimpleDateFormat("MdE", Locale.getDefault());
	private List<DateValue> dateValueList;
	private int currentX;
	private int firstColX;
	
	public DateView(Context context, int viewWidth, int viewHeight) {
		super(context);
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		
		dateTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		dateTextPaint.setTextAlign(Paint.Align.CENTER);
		dateTextPaint.setTextSize(context.getResources().getDimension(R.dimen.date_text_size_in_simple_forecast_view));
		
		Rect rect = new Rect();
		dateTextPaint.getTextBounds("0", 0, 1, rect);
		textHeight = rect.height();
		
		setWillNotDraw(false);
	}
	
	public DateView setDateValueList(List<DateValue> dateValueList) {
		this.dateValueList = dateValueList;
		this.firstColX = dateValueList.get(0).beginX;
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
		
		dateTextPaint.setColor(Color.WHITE);
		final int y = getHeight() / 2 + textHeight / 2;
		
		for (DateValue dateValue : dateValueList) {
			if (currentX >= dateValue.beginX - firstColX && currentX < dateValue.endX - firstColX) {
				dateValue.lastX = currentX + firstColX;
			} else if (currentX < dateValue.beginX) {
				dateValue.lastX = dateValue.beginX;
			}
			canvas.drawText(MdE.format(dateValue.date), dateValue.lastX, y, dateTextPaint);
		}
	}
	
	public void reDraw(int newX) {
		this.currentX = newX;
		invalidate();
	}
	
	
	public static class DateValue {
		public final int beginX;
		public final Date date;
		public int endX;
		public int lastX;
		
		public DateValue(int beginX, Date date) {
			this.beginX = beginX;
			this.date = date;
			this.lastX = beginX;
		}
	}
}