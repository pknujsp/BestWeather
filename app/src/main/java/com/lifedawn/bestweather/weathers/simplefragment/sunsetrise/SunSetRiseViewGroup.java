package com.lifedawn.bestweather.weathers.simplefragment.sunsetrise;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.SunSetRiseViewBinding;
import com.lifedawn.bestweather.theme.AppTheme;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class SunSetRiseViewGroup extends ViewGroup {
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("M.d E H:mm", Locale.getDefault());
	private Location location;


	public SunSetRiseViewGroup(Context context, Location location) {
		super(context);
		this.location = location;
		init(context);
	}

	private void init(Context context) {
		setWillNotDraw(false);
		//LayoutInflater.from(context).inflate(R.layout.sun_set_rise_view, this, false);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = 100;

		setMeasuredDimension(widthSize, heightSize);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

	}

	@Override
	protected void onDraw(Canvas canvas) {

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

		int type1Str;
		int type2Str;
		int type3Str;

		int type1Icon;
		int type2Icon;
		int type3Icon;

		if (todayCalendar.before(todaySunRiseCalendar)) {
			//일출 전 새벽 (0시 0분 <= 현재 시각 < 일출)
			//순서 : 전날 11.06 일몰 - 당일 11.07 일출 - 당일 11.07 일몰
			type1Calendar = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(yesterdayCalendar);
			type1Icon = R.drawable.temp_icon;
			type1Str = R.string.sunset;

			type2Calendar = todaySunRiseCalendar;
			type2Icon = R.drawable.temp_icon;
			type2Str = R.string.sunrise;

			type3Calendar = todaySunSetCalendar;
			type3Icon = R.drawable.temp_icon;
			type3Str = R.string.sunset;
		} else if (todayCalendar.compareTo(todaySunRiseCalendar) >= 0 && todayCalendar.compareTo(todaySunSetCalendar) <= 0) {
			//일출 후, 일몰 전 (일출 <= 현재 시각 <= 일몰)
			//순서 : 당일 11.07 일출 - 당일 11.07 일몰 - 다음날 11.08 일출
			type1Calendar = todaySunRiseCalendar;
			type1Icon = R.drawable.temp_icon;
			type1Str = R.string.sunrise;

			type2Calendar = todaySunSetCalendar;
			type2Icon = R.drawable.temp_icon;
			type2Str = R.string.sunset;

			type3Calendar = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(tomorrowCalendar);
			type3Icon = R.drawable.temp_icon;
			type3Str = R.string.sunrise;
		} else {
			//일몰 후 (일몰 < 현재 시각 < 24시 0분 )
			//순서 : 당일 11.07 일몰 - 다음날 11.08 일출 - 다음날 11.08 일몰
			type1Calendar = todaySunSetCalendar;
			type1Icon = R.drawable.temp_icon;
			type1Str = R.string.sunset;

			type2Calendar = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(tomorrowCalendar);
			type2Icon = R.drawable.temp_icon;
			type2Str = R.string.sunrise;

			type3Calendar = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(tomorrowCalendar);
			type3Icon = R.drawable.temp_icon;
			type3Str = R.string.sunset;
		}

		/*
		binding.sunSetRiseType1.sunSetRiseTypeIcon.setImageResource(type1Icon);
		binding.sunSetRiseType1.sunSetRiseTypeText.setText(type1Str);
		binding.sunSetRiseType1.sunSetRiseTime.setText(dateFormat.format(type1Calendar.getTime()));

		binding.sunSetRiseType2.sunSetRiseTypeIcon.setImageResource(type2Icon);
		binding.sunSetRiseType2.sunSetRiseTypeText.setText(type2Str);
		binding.sunSetRiseType2.sunSetRiseTime.setText(dateFormat.format(type2Calendar.getTime()));

		binding.sunSetRiseType3.sunSetRiseTypeIcon.setImageResource(type3Icon);
		binding.sunSetRiseType3.sunSetRiseTypeText.setText(type3Str);
		binding.sunSetRiseType3.sunSetRiseTime.setText(dateFormat.format(type3Calendar.getTime()));

		 */
	}
}
