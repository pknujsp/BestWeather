package com.lifedawn.bestweather.weathers.simplefragment.sunsetrise;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.theme.AppTheme;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SunSetRiseInfoView extends View {
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("M.d E H:mm", Locale.getDefault());

	private Paint imgPaint;
	private TextPaint labelPaint;
	private TextPaint timePaint;
	private String label;
	private String time;
	private Drawable img;

	private Rect imgRect;
	private Rect labelRect;
	private Rect timeRect;

	public int measuredWidth;
	public int measuredHeight;

	private int padding;
	private Calendar calendar;

	public SunSetRiseInfoView(Context context, String label, Calendar calendar, int imgId) {
		super(context);
		init(context, label, calendar, imgId);
	}

	private void init(Context context, String label, Calendar calendar, int imgId) {
		this.label = label;
		this.time = dateFormat.format(calendar.getTime());
		this.img = ContextCompat.getDrawable(context, imgId);

		imgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		labelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		labelPaint.setColor(AppTheme.getColor(context, R.attr.textColorInWeatherCard));
		labelPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, getResources().getDisplayMetrics()));

		labelRect = new Rect();
		labelPaint.getTextBounds(label, 0, label.length(), labelRect);

		timePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		timePaint.setColor(AppTheme.getColor(context, R.attr.textColorInWeatherCard));
		timePaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15f, getResources().getDisplayMetrics()));

		timeRect = new Rect();
		timePaint.getTextBounds(time, 0, time.length(), timeRect);

		final int imgSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28f, getResources().getDisplayMetrics());
		final int labelLeftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f,
				context.getResources().getDisplayMetrics());
		final int timeTopMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f,
				context.getResources().getDisplayMetrics());

		imgRect = new Rect(0, 0, imgSize, imgSize);
		labelRect.offsetTo(imgRect.right + labelLeftMargin, imgRect.centerY());
		timeRect.offsetTo(imgRect.left, imgRect.bottom + timeTopMargin + timeRect.height() / 2);

		imgRect.offset(padding, padding);
		labelRect.offset(padding, padding);
		timeRect.offset(padding, padding);
		img.setBounds(imgRect);

		padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, getResources().getDisplayMetrics());

		setPadding(padding, padding, padding, padding);

		measuredWidth = Math.max(labelRect.right
				+ padding * 2, timeRect.right + padding);
		measuredHeight = timeRect.bottom + padding;

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(measuredWidth, measuredHeight);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		img.draw(canvas);
		canvas.drawText(label, labelRect.left, labelRect.top + labelRect.height() / 2f, labelPaint);
		canvas.drawText(time, timeRect.left, timeRect.top + timeRect.height() / 2f, timePaint);
	}

	public Calendar getCalendar() {
		return calendar;
	}

	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}
}
