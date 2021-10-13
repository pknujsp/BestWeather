package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.view.View;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.ClockUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateView extends View {
	private final TextPaint dateTextPaint;
	private final int viewWidth;
	private final int viewHeight;
	private final int columnWidth;
	private final int textHeight;
	private final SimpleDateFormat MdE = new SimpleDateFormat("M/d E", Locale.getDefault());
	private List<DateValue> dateValueList;
	private int currentX;
	private int firstColX;
	
	public DateView(Context context, int viewWidth, int viewHeight, int columnWidth) {
		super(context);
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.columnWidth = columnWidth;
		
		dateTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		dateTextPaint.setTextAlign(Paint.Align.CENTER);
		dateTextPaint.setTextSize(context.getResources().getDimension(R.dimen.date_text_size_in_simple_forecast_view));
		
		Rect rect = new Rect();
		dateTextPaint.getTextBounds("0", 0, 1, rect);
		textHeight = rect.height();
		
		setWillNotDraw(false);
	}
	
	public void init(List<Long> dateTimeList) {
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(dateTimeList.get(0));
		date.add(Calendar.DATE, -15);
		long lastDate = date.getTimeInMillis();
		
		List<DateView.DateValue> dateValueList = new ArrayList<>();
		int beginX = 0;
		
		for (int col = 0; col < dateTimeList.size(); col++) {
			date.setTimeInMillis(dateTimeList.get(col));
			
			if (date.get(Calendar.HOUR_OF_DAY) == 0 || col == 0) {
				if (dateValueList.size() > 0) {
					dateValueList.get(dateValueList.size() - 1).endX = columnWidth * (col - 1) + columnWidth / 2;
				}
				beginX = columnWidth * col + columnWidth / 2;
				dateValueList.add(new DateView.DateValue(beginX, date.getTime()));
			}
			
			if (!ClockUtil.areSameDate(lastDate, date.getTimeInMillis())) {
				lastDate = date.getTimeInMillis();
			}
		}
		dateValueList.get(dateValueList.size() - 1).endX = columnWidth * (dateTimeList.size() - 1) + columnWidth / 2;
		
		this.dateValueList = dateValueList;
		this.firstColX = dateValueList.get(0).beginX;
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