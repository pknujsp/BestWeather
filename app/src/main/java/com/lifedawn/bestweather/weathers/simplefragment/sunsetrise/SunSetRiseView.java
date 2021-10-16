package com.lifedawn.bestweather.weathers.simplefragment.sunsetrise;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.theme.AppTheme;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class SunSetRiseView extends ViewGroup {
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("M.d E H:mm", Locale.getDefault());
	private Location location;
	private int type1BottomMargin;
	private int type2BottomMargin;

	SunSetRiseInfoView type1View;
	SunSetRiseInfoView type2View;
	SunSetRiseInfoView type3View;

	public SunSetRiseView(Context context, Location location) {
		super(context);
		init(context, location);
	}

	public SunSetRiseView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SunSetRiseView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public SunSetRiseView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	private void init(Context context, Location location) {
		this.location = location;
		setWillNotDraw(false);
		setSunSetRiseViews();
	}

	private void setSunSetRiseViews() {

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, type1View.measuredWidth + type1BottomMargin + type2View.measuredHeight
				+ type3View.measuredHeight);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childCount = getChildCount();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
}

/*
	SunriseSunsetCalculator sunriseSunsetCalculator = new SunriseSunsetCalculator(location, TimeZone.getDefault());

		final Calendar todayCalendar = Calendar.getInstance();
		final Calendar yesterdayCalendar = (Calendar) todayCalendar.clone();
		final Calendar tomorrowCalendar = (Calendar) todayCalendar.clone();

		yesterdayCalendar.add(Calendar.DATE, -1);
		tomorrowCalendar.add(Calendar.DATE, 1);

		Calendar todaySunRiseCalendar = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(todayCalendar);
		Calendar todaySunSetCalendar = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(todayCalendar);

		SunSetRiseInfoView type1View = new SunSetRiseInfoView(getContext());
		SunSetRiseInfoView type2View = new SunSetRiseInfoView(getContext());
		SunSetRiseInfoView type3View = new SunSetRiseInfoView(getContext());
		type1View.setId(R.id.sunSetRiseType1View);
		type2View.setId(R.id.sunSetRiseType2View);
		type3View.setId(R.id.sunSetRiseType3View);

		if (todayCalendar.before(todaySunRiseCalendar)) {
			//일출 전 새벽 (0시 0분 <= 현재 시각 < 일출)
			//순서 : 전날 11.06 일몰 - 당일 11.07 일출 - 당일 11.07 일몰
			type1View.setViews(sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(yesterdayCalendar),
					getContext().getString(R.string.sunset));
			type2View.setViews(todaySunRiseCalendar, getContext().getString(R.string.sunrise));
			type3View.setViews(todaySunSetCalendar, getContext().getString(R.string.sunset));
		} else if (todayCalendar.compareTo(todaySunRiseCalendar) >= 0 && todayCalendar.compareTo(todaySunSetCalendar) <= 0) {
			//일출 후, 일몰 전 (일출 <= 현재 시각 <= 일몰)
			//순서 : 당일 11.07 일출 - 당일 11.07 일몰 - 다음날 11.08 일출
			type1View.setViews(todaySunRiseCalendar, getContext().getString(R.string.sunrise));
			type2View.setViews(todaySunSetCalendar, getContext().getString(R.string.sunset));
			type3View.setViews(sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(tomorrowCalendar),
					getContext().getString(R.string.sunrise));
		} else {
			//일몰 후 (일몰 < 현재 시각 < 24시 0분 )
			//순서 : 당일 11.07 일몰 - 다음날 11.08 일출 - 다음날 11.08 일몰
			type1View.setViews(todaySunSetCalendar, getContext().getString(R.string.sunset));
			type2View.setViews(sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(tomorrowCalendar),
					getContext().getString(R.string.sunrise));
			type3View.setViews(sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(tomorrowCalendar),
					getContext().getString(R.string.sunset));
		}

		RelativeLayout.LayoutParams type1LayoutParams = new RelativeLayout.LayoutParams(100,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		type1LayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		type1LayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		type1LayoutParams.bottomMargin = type1BottomMargin;
		addView(type1View, type1LayoutParams);

		RelativeLayout.LayoutParams type2LayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		type1LayoutParams.addRule(RelativeLayout.BELOW, R.id.sunSetRiseType1View);
		type1LayoutParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.sunSetRiseType1View);
		type1LayoutParams.bottomMargin = type2BottomMargin;
		addView(type2View, type2LayoutParams);

		RelativeLayout.LayoutParams type3LayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		type1LayoutParams.addRule(RelativeLayout.BELOW, R.id.sunSetRiseType2View);
		type1LayoutParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.sunSetRiseType1View);
		addView(type3View, type3LayoutParams);
 */