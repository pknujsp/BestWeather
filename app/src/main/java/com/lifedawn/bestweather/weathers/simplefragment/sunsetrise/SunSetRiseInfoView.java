package com.lifedawn.bestweather.weathers.simplefragment.sunsetrise;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
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
	private Drawable typeIcon;
	private Calendar calendar;
	private String type;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("M.d E H:mm", Locale.getDefault());

	private TextPaint typeTextPaint;
	private TextPaint timeTextPaint;
	private Rect iconRect;
	private Rect typeTextRect;
	private Rect timeTextRect;

	private int pixelWidth;
	private int pixelHeight;

	public SunSetRiseInfoView(Context context, Calendar calendar, SunsetriseFragment.SunSetRiseType sunSetRiseType) {
		super(context);
		init(context, calendar, sunSetRiseType);
	}

	private void init(Context context, Calendar calendar, SunsetriseFragment.SunSetRiseType sunSetRiseType) {
		this.calendar = calendar;
		this.typeIcon = ContextCompat.getDrawable(getContext(), sunSetRiseType == SunsetriseFragment.SunSetRiseType.RISE ?
				R.drawable.temp_icon : R.drawable.temp_icon);
		this.type = getContext().getString(sunSetRiseType == SunsetriseFragment.SunSetRiseType.RISE ? R.string.sunrise : R.string.sunset);

		typeTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		typeTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15f, getResources().getDisplayMetrics()));
		typeTextPaint.setColor(AppTheme.getColor(context, R.attr.textColorInWeatherCard));

		timeTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		timeTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, getResources().getDisplayMetrics()));
		timeTextPaint.setColor(AppTheme.getColor(context, R.attr.textColorInWeatherCard));

		typeTextRect = new Rect();
		timeTextRect = new Rect();

		typeTextPaint.getTextBounds(type, 0, type.length(), typeTextRect);
		String dateTime = dateFormat.format(calendar.getTime());
		timeTextPaint.getTextBounds(dateTime, 0, dateTime.length(), timeTextRect);

		int typeTextLeftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, getResources().getDisplayMetrics());
		int timeTextTopMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, getResources().getDisplayMetrics());

		final int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());

		final int iconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, getResources().getDisplayMetrics());
		iconRect = new Rect(padding, padding, padding + iconSize, padding + iconSize);
		typeTextRect.offsetTo(iconRect.right + typeTextLeftMargin, iconRect.centerY() + typeTextRect.height() / 2);
		timeTextRect.offsetTo(iconRect.left, iconRect.bottom + timeTextTopMargin + timeTextRect.height() / 2);

		typeIcon.setBounds(iconRect);

		pixelWidth = Math.max(typeTextRect.right, timeTextRect.right) + padding;
		pixelHeight = timeTextRect.bottom + padding;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(pixelWidth, pixelHeight);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		typeIcon.draw(canvas);
		canvas.drawText(type, typeTextRect.left, typeTextRect.top, typeTextPaint);
		canvas.drawText(dateFormat.format(calendar.getTime()), timeTextRect.left, timeTextRect.top, timeTextPaint);
	}

	public int getPixelWidth() {
		return pixelWidth;
	}

	public int getPixelHeight() {
		return pixelHeight;
	}

	public Calendar getCalendar() {
		return calendar;
	}
}
