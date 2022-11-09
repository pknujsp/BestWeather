package com.lifedawn.bestweather.weathers.simplefragment.sunsetrise;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.SunRiseSetType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.theme.AppTheme;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

public class SunSetRiseInfoView extends View {
	private Drawable typeIcon;
	private ZonedDateTime dateTime;
	private String type;
	private DateTimeFormatter dateTimeFormatter;

	private TextPaint typeTextPaint;
	private TextPaint timeTextPaint;
	private Rect iconRect;
	private Rect typeTextRect;
	private Rect timeTextRect;

	private int pixelWidth;
	private int pixelHeight;


	public SunSetRiseInfoView(Context context, ZonedDateTime dateTime, SunRiseSetType sunSetRiseType) {
		super(context);
		init(dateTime, sunSetRiseType);
	}

	private void init(ZonedDateTime dateTime, SunRiseSetType sunRiseSetType) {
		this.dateTime = dateTime;
		this.typeIcon = ContextCompat.getDrawable(getContext(), sunRiseSetType == SunRiseSetType.RISE ?
				R.drawable.sunrise : R.drawable.sunset);
		this.type = getContext().getString(sunRiseSetType == SunRiseSetType.RISE ? R.string.sunrise : R.string.sunset);

		typeTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		typeTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15f, getResources().getDisplayMetrics()));
		typeTextPaint.setColor(Color.WHITE);

		timeTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		timeTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, getResources().getDisplayMetrics()));
		timeTextPaint.setColor(Color.WHITE);

		typeTextRect = new Rect();
		timeTextRect = new Rect();

		typeTextPaint.getTextBounds(type, 0, type.length(), typeTextRect);

		ValueUnits clockUnit =
				ValueUnits.valueOf(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getContext().getString(R.string.pref_key_unit_clock),
						ValueUnits.clock12.name()));

		dateTimeFormatter = DateTimeFormatter.ofPattern(clockUnit == ValueUnits.clock12
				? getContext().getString(R.string.datetime_pattern_clock12) : getContext().getString(R.string.datetime_pattern_clock24));

		String dateTimeStr = this.dateTime.format(dateTimeFormatter);
		timeTextPaint.getTextBounds(dateTimeStr, 0, dateTimeStr.length(), timeTextRect);

		int typeTextLeftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, getResources().getDisplayMetrics());
		int timeTextTopMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());

		final int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());

		final int iconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 42f, getResources().getDisplayMetrics());
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
		canvas.drawText(dateTime.format(dateTimeFormatter), timeTextRect.left, timeTextRect.top, timeTextPaint);
	}

	public int getPixelWidth() {
		return pixelWidth;
	}

	public int getPixelHeight() {
		return pixelHeight;
	}

	public ZonedDateTime getDateTime() {
		return dateTime;
	}
}
