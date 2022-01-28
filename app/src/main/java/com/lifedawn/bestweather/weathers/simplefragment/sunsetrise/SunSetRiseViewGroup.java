package com.lifedawn.bestweather.weathers.simplefragment.sunsetrise;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.theme.AppTheme;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class SunSetRiseViewGroup extends FrameLayout {
	private DateTimeFormatter dateTimeFormatter;
	private Location location;
	private ZoneId zoneId;

	private SunSetRiseInfoView type1View;
	private SunSetRiseInfoView type2View;
	private SunSetRiseInfoView type3View;
	private TextView errorView;

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
	private OnSunRiseSetListener onSunRiseSetListener;


	public SunSetRiseViewGroup(Context context, Location location, ZoneId zoneId, OnSunRiseSetListener onSunRiseSetListener) {
		super(context);
		this.location = location;
		this.zoneId = zoneId;
		this.onSunRiseSetListener = onSunRiseSetListener;
		init(context);
	}

	private void init(Context context) {
		setWillNotDraw(false);
		type1BottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35f, getResources().getDisplayMetrics());
		type2BottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, getResources().getDisplayMetrics());
		lineMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, getResources().getDisplayMetrics());
		lineWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());
		circleRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, getResources().getDisplayMetrics());

		linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint.setColor(Color.GREEN);

		timeTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		timeTextPaint.setColor(AppTheme.getColor(context, R.attr.textColorInWeatherCard));
		timeTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, getResources().getDisplayMetrics()));
		timeTextPaint.setTextAlign(Paint.Align.CENTER);

		timeCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		timeCirclePaint.setColor(Color.WHITE);

		lineRect = new Rect();
		timeTextRect = new Rect();
		timeCirclePoint = new Point();
		currentTextRect = new Rect();

		current = context.getString(R.string.current);

		ValueUnits clockUnit =
				ValueUnits.enumOf(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_unit_clock),
						ValueUnits.clock12.name()));
		String dateTimeFormat = clockUnit == ValueUnits.clock12
				? context.getString(R.string.datetime_pattern_clock12) : context.getString(R.string.datetime_pattern_clock24);

		ZonedDateTime now = ZonedDateTime.now(zoneId);
		dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormat);
		String currentDateTime = now.format(dateTimeFormatter);
		timeTextPaint.getTextBounds(currentDateTime, 0, currentDateTime.length(), timeTextRect);

		type1PointOnLine = new Point();
		type2PointOnLine = new Point();

		setViews();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (type1View != null && type2View != null && type3View != null) {
			int widthSize = MeasureSpec.getSize(widthMeasureSpec);
			setMeasuredDimension(widthSize, type1View.getPixelHeight() + type1BottomMargin + type2View.getPixelHeight() + type2BottomMargin
					+ type3View.getPixelHeight());
		} else {
			setMeasuredDimension(widthMeasureSpec, (int) errorView.getTextSize() * 2);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (type1View != null && type2View != null && type3View != null) {
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
		} else {
			errorView.layout(0, 0, getWidth(), (int) errorView.getTextSize() * 2);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (type1View != null && type2View != null && type3View != null) {
			canvas.drawRect(lineRect.left, lineRect.top, lineRect.right, lineRect.bottom, linePaint);
			ZonedDateTime now = ZonedDateTime.now(zoneId);

			long millis = now.toInstant().toEpochMilli();

			long nowTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(millis);
			millis = type1View.getDateTime().toInstant().toEpochMilli();

			long type1Minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
			millis = type2View.getDateTime().toInstant().toEpochMilli();

			long type2Minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
			long diff = type2Minutes - type1Minutes;

			float heightPerMinute = (float) (type2PointOnLine.y - type1PointOnLine.y) / (float) diff;
			float currentTimeY = type1PointOnLine.y + (heightPerMinute * (nowTimeMinutes - type1Minutes));
			timeCirclePoint.x = type1PointOnLine.x;
			timeCirclePoint.y = (int) currentTimeY;
			canvas.drawCircle(timeCirclePoint.x, timeCirclePoint.y, circleRadius, timeCirclePaint);

			drawCurrent(now.format(dateTimeFormatter), canvas);
		}
	}

	private void drawCurrent(String dateTime, Canvas canvas) {
		String textOnCanvas = current + "\n" + dateTime;

		timeTextPaint.getTextBounds(textOnCanvas, 0, textOnCanvas.length(), timeTextRect);
		StaticLayout.Builder builder = StaticLayout.Builder.obtain(textOnCanvas, 0, textOnCanvas.length(), timeTextPaint,
				timeTextRect.width());
		StaticLayout sl = builder.build();
		canvas.save();

		int criteriaX = timeCirclePoint.x - lineMargin - timeTextRect.width() / 2 - lineWidth / 2;
		int criteriaY = timeCirclePoint.y;

		float textHeight = timeTextRect.height();
		float textYCoordinate = criteriaY + timeTextRect.exactCenterY() -
				((sl.getLineCount() * textHeight) / 2);

		canvas.translate(criteriaX, textYCoordinate);

		sl.draw(canvas);
		canvas.restore();
	}

	public void refresh() {
		if (type2View == null) {
			return;
		}
		if (type2View.getDateTime().isBefore(ZonedDateTime.now(zoneId))) {
			setViews();
			invalidate();
			requestLayout();
		} else {
			invalidate();
		}
	}


	public void setViews() {
		SunriseSunsetCalculator sunriseSunsetCalculator = new SunriseSunsetCalculator(location, TimeZone.getTimeZone(zoneId.getId()));

		final Calendar todayCalendar = Calendar.getInstance(TimeZone.getTimeZone(zoneId.getId()));
		final Calendar yesterdayCalendar = (Calendar) todayCalendar.clone();
		final Calendar tomorrowCalendar = (Calendar) todayCalendar.clone();

		yesterdayCalendar.add(Calendar.DATE, -1);
		tomorrowCalendar.add(Calendar.DATE, 1);

		Calendar todaySunRiseCalendar = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(todayCalendar);
		Calendar todaySunSetCalendar = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(todayCalendar);

		removeAllViews();
		type1View = null;
		type2View = null;
		type3View = null;
		errorView = null;

		if (todaySunRiseCalendar == null || todaySunSetCalendar == null) {
			onSunRiseSetListener.onCalcResult(false);

			errorView = new TextView(getContext());
			errorView.setText(R.string.failed_calculating_sun_rise_set);
			errorView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
			errorView.setTextColor(AppTheme.getColor(getContext(), R.attr.textColorInWeatherCard));
			errorView.setGravity(Gravity.CENTER);
			errorView.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			addView(errorView);
			return;
		}


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
		onSunRiseSetListener.onCalcResult(true);

		ZonedDateTime type1ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(type1Calendar.getTimeInMillis()),
				zoneId);
		ZonedDateTime type2ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(type2Calendar.getTimeInMillis()),
				zoneId);
		ZonedDateTime type3ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(type3Calendar.getTimeInMillis()),
				zoneId);

		type1View = new SunSetRiseInfoView(getContext(), type1ZonedDateTime, type1);
		type2View = new SunSetRiseInfoView(getContext(), type2ZonedDateTime, type2);
		type3View = new SunSetRiseInfoView(getContext(), type3ZonedDateTime, type3);

		addView(type1View);
		addView(type2View);
		addView(type3View);
	}

}
