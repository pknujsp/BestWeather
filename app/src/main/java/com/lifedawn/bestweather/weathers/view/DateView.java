package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;

import java.util.List;

public class DateView extends View {
	private final List<DateValue> dateValueList;
	private final TextPaint dateTextPaint;
	private final int textHeight;
	private final int firstColX;

	private int currentX;

	public DateView(Context context, List<DateValue> dateValueList) {
		super(context);

		dateTextPaint = new TextPaint();
		dateTextPaint.setTextAlign(Paint.Align.CENTER);
		dateTextPaint.setColor(Color.DKGRAY);
		dateTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14f, getResources().getDisplayMetrics()));

		Rect rect = new Rect();
		dateTextPaint.getTextBounds("0", 0, 1, rect);
		textHeight = rect.height();

		this.dateValueList = dateValueList;
		this.firstColX = dateValueList.get(0).beginX;
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
		final int y = getHeight() / 2 + textHeight / 2;

		for (DateValue dateValue : dateValueList) {
			if (currentX >= dateValue.beginX - firstColX && currentX < dateValue.endX - firstColX) {
				dateValue.lastX = currentX + firstColX;
			} else if (currentX < dateValue.beginX) {
				dateValue.lastX = dateValue.beginX;
			}
			canvas.drawText(dateValue.date, dateValue.lastX, y, dateTextPaint);
		}
	}

	public void reDraw(int newX) {
		this.currentX = newX;
		invalidate();
	}


	public static class DateValue {
		final int beginX;
		final String date;
		int endX;
		int lastX;

		public DateValue(int beginX, String date) {
			this.beginX = beginX;
			this.date = date;
			this.lastX = beginX;
		}
	}
}