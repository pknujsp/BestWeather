package com.lifedawn.bestweather.weathers.simplefragment.sunsetrise;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.ViewGroup;

import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.theme.AppTheme;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class SunSetRiseViewGroup extends ViewGroup {
	private SimpleDateFormat dateFormat;
	private Location location;

	private SunSetRiseInfoView type1View;
	private SunSetRiseInfoView type2View;
	private SunSetRiseInfoView type3View;

	private int type1BottomMargin;
	private int type2BottomMargin;
	private int lineMargin;
	private int lineWidth;
	private int circleRadius;

	private Paint linePaint;
	private TextPaint timeTextPaint;
	private Rect lineRect;
	private Rect timeTextRect;
	private Rect currentTextRect;
	private Point timeCirclePoint;
	private Paint timeCirclePaint;

	private Point type1PointOnLine;
	private Point type2PointOnLine;

	private String current;

	public SunSetRiseViewGroup(Context context, Location location) {
		super(context);
		this.location = location;
		init(context);
	}

	private void init(Context context) {
		setWillNotDraw(false);
		type1BottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35f, getResources().getDisplayMetrics());
		type2BottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, getResources().getDisplayMetrics());
		lineMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, getResources().getDisplayMetrics());
		lineWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());
		circleRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7f, getResources().getDisplayMetrics());

		linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint.setColor(Color.GREEN);

		timeTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		timeTextPaint.setColor(AppTheme.getColor(context, R.attr.textColor));
		timeTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, getResources().getDisplayMetrics()));
		timeTextPaint.setTextAlign(Paint.Align.RIGHT);

		timeCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		timeCirclePaint.setColor(Color.WHITE);

		lineRect = new Rect();
		timeTextRect = new Rect();
		timeCirclePoint = new Point();
		currentTextRect = new Rect();

		Calendar calendar = Calendar.getInstance();
		current = context.getString(R.string.current);

		ValueUnits clockUnit =
				ValueUnits.enumOf(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_unit_clock),
						ValueUnits.clock12.name()));
		dateFormat = new SimpleDateFormat(clockUnit == ValueUnits.clock12
				? "M.d E a h:mm" : "M.d E HH:mm", Locale.getDefault());

		String currentDateTime = dateFormat.format(calendar.getTime());
		timeTextPaint.getTextBounds(currentDateTime, 0, currentDateTime.length(), timeTextRect);

		type1PointOnLine = new Point();
		type2PointOnLine = new Point();

		setViews();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		setMeasuredDimension(widthSize, type1View.getPixelHeight() + type1BottomMargin + type2View.getPixelHeight() + type2BottomMargin
				+ type3View.getPixelHeight());
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		type1View.measure(type1View.getPixelWidth(), type1View.getPixelHeight());
		type2View.measure(type2View.getPixelWidth(), type2View.getPixelHeight());
		type3View.measure(type3View.getPixelWidth(), type3View.getPixelHeight());

		int centerX = right / 2;
		int childLeft = centerX;
		int childRight = childLeft + type1View.getPixelWidth();
		int childTop = 0;
		int childBottom = type1View.getPixelHeight();

		int lineTop = childTop + type1View.getPixelHeight() / 2;
		type1PointOnLine.y = lineTop;

		type1View.layout(childLeft, childTop, childRight, childBottom);

		childRight = childLeft + type2View.getPixelWidth();
		childTop = childBottom + type1BottomMargin;
		childBottom = childTop + type2View.getPixelHeight();

		type2PointOnLine.y = childTop + type2View.getPixelHeight() / 2;
		type2View.layout(childLeft, childTop, childRight, childBottom);

		childRight = childLeft + type3View.getPixelWidth();
		childTop = childBottom + type2BottomMargin;
		childBottom = childTop + type3View.getPixelHeight();

		int lineBottom = childTop + type3View.getPixelHeight() / 2;

		type3View.layout(childLeft, childTop, childRight, childBottom);

		type1PointOnLine.x = childLeft - lineMargin - lineWidth / 2;
		type2PointOnLine.x = type1PointOnLine.x;
		lineRect.set(childLeft - lineMargin - lineWidth, lineTop, childLeft - lineMargin, lineBottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawRect(lineRect.left, lineRect.top, lineRect.right, lineRect.bottom, linePaint);

		Calendar calendar = Calendar.getInstance();
		long currentTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(calendar.getTimeInMillis());
		long type1Minutes = TimeUnit.MILLISECONDS.toMinutes(type1View.getCalendar().getTimeInMillis());
		long type2Minutes = TimeUnit.MILLISECONDS.toMinutes(type2View.getCalendar().getTimeInMillis());

		long diff = type2Minutes - type1Minutes;

		float heightPerMinute = (float) (type2PointOnLine.y - type1PointOnLine.y) / (float) diff;
		float currentTimeY = type1PointOnLine.y + (heightPerMinute * (currentTimeMinutes - type1Minutes));
		timeCirclePoint.x = type1PointOnLine.x;
		timeCirclePoint.y = (int) currentTimeY;
		canvas.drawCircle(timeCirclePoint.x, timeCirclePoint.y, circleRadius, timeCirclePaint);

		timeTextRect.offsetTo(timeCirclePoint.x - lineMargin - lineWidth / 2, timeCirclePoint.y);
		timeTextPaint.setTextAlign(Paint.Align.RIGHT);
		String currentDateTime = dateFormat.format(calendar.getTime());
		canvas.drawText(currentDateTime, timeTextRect.left, timeTextRect.top + timeTextPaint.descent() - timeTextPaint.ascent(),
				timeTextPaint);

		timeTextPaint.getTextBounds(currentDateTime, 0, currentDateTime.length(), currentTextRect);

		timeTextPaint.setTextAlign(Paint.Align.CENTER);

		canvas.drawText(current, timeTextRect.left - currentTextRect.width() / 2f, timeTextRect.top,
				timeTextPaint);
	}

	public void refresh() {
		Calendar calendar = Calendar.getInstance();
		if (type2View.getCalendar().before(calendar)) {
			setViews();
			invalidate();
			requestLayout();
		} else {
			invalidate();
		}
	}


	public void setViews() {
		SunriseSunsetCalculator sunriseSunsetCalculator = new SunriseSunsetCalculator(location, TimeZone.getDefault());

		final Calendar todayCalendar = Calendar.getInstance();
		final Calendar yesterdayCalendar = (Calendar) todayCalendar.clone();
		final Calendar tomorrowCalendar = (Calendar) todayCalendar.clone();

		yesterdayCalendar.add(Calendar.DATE, -1);
		tomorrowCalendar.add(Calendar.DATE, 1);

		Calendar todaySunRiseCalendar = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(todayCalendar);
		Calendar todaySunSetCalendar = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(todayCalendar);

		Calendar type1Calendar;
		Calendar type2Calendar;
		Calendar type3Calendar;

		SunsetriseFragment.SunSetRiseType type1;
		SunsetriseFragment.SunSetRiseType type2;
		SunsetriseFragment.SunSetRiseType type3;

		if (todayCalendar.before(todaySunRiseCalendar)) {
			//일출 전 새벽 (0시 0분 <= 현재 시각 < 일출)
			//순서 : 전날 11.06 일몰 - 당일 11.07 일출 - 당일 11.07 일몰
			type1Calendar = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(yesterdayCalendar);
			type1 = SunsetriseFragment.SunSetRiseType.SET;

			type2Calendar = todaySunRiseCalendar;
			type2 = SunsetriseFragment.SunSetRiseType.RISE;

			type3Calendar = todaySunSetCalendar;
			type3 = SunsetriseFragment.SunSetRiseType.SET;
		} else if (todayCalendar.compareTo(todaySunRiseCalendar) >= 0 && todayCalendar.compareTo(todaySunSetCalendar) <= 0) {
			//일출 후, 일몰 전 (일출 <= 현재 시각 <= 일몰)
			//순서 : 당일 11.07 일출 - 당일 11.07 일몰 - 다음날 11.08 일출
			type1Calendar = todaySunRiseCalendar;
			type1 = SunsetriseFragment.SunSetRiseType.RISE;

			type2Calendar = todaySunSetCalendar;
			type2 = SunsetriseFragment.SunSetRiseType.SET;

			type3Calendar = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(tomorrowCalendar);
			type3 = SunsetriseFragment.SunSetRiseType.RISE;
		} else {
			//일몰 후 (일몰 < 현재 시각 < 24시 0분 )
			//순서 : 당일 11.07 일몰 - 다음날 11.08 일출 - 다음날 11.08 일몰
			type1Calendar = todaySunSetCalendar;
			type1 = SunsetriseFragment.SunSetRiseType.SET;

			type2Calendar = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(tomorrowCalendar);
			type2 = SunsetriseFragment.SunSetRiseType.RISE;

			type3Calendar = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(tomorrowCalendar);
			type3 = SunsetriseFragment.SunSetRiseType.SET;
		}

		type1View = new SunSetRiseInfoView(getContext(), type1Calendar, type1);
		type2View = new SunSetRiseInfoView(getContext(), type2Calendar, type2);
		type3View = new SunSetRiseInfoView(getContext(), type3Calendar, type3);

		removeAllViews();

		addView(type1View);
		addView(type2View);
		addView(type3View);
	}

}
