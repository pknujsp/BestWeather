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

public class SunSetRiseView extends RelativeLayout {
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("M.d E hh:mm", Locale.getDefault());
	private int type1BottomMargin;
	private int type2BottomMargin;
	private int lineMargin;
	private int lineWidth;
	private int pointYOfType2OnLine;
	private Rect lineRect = new Rect();
	private Paint linePaint;
	private TextPaint currentTimePaint;
	private String currentStr;

	public SunSetRiseView(Context context) {
		super(context);
		init();
	}

	public SunSetRiseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SunSetRiseView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		setWillNotDraw(false);

		type1BottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, getResources().getDisplayMetrics());
		type2BottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f, getResources().getDisplayMetrics());
		lineMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());
		lineWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, getResources().getDisplayMetrics());

		linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint.setColor(Color.GREEN);

		currentTimePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		currentTimePaint.setTextAlign(Paint.Align.CENTER);
		currentTimePaint.setColor(Color.BLUE);
		currentTimePaint.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13f, getResources().getDisplayMetrics()));

		currentStr = getContext().getString(R.string.current) + "\n";
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		View type1View = findViewById(R.id.sunSetRiseType1View);
		View type2View = findViewById(R.id.sunSetRiseType2View);
		View type3View = findViewById(R.id.sunSetRiseType3View);

		Rect type1Rect = new Rect(type1View.getLeft(), type1View.getTop(), type1View.getRight(), type1View.getBottom());
		Rect type2Rect = new Rect(type2View.getLeft(), type2View.getTop(), type2View.getRight(), type2View.getBottom());
		Rect type3Rect = new Rect(type3View.getLeft(), type3View.getTop(), type3View.getRight(), type3View.getBottom());

		lineRect.left = type1Rect.left - lineMargin - lineWidth;
		lineRect.right = lineRect.left + lineWidth;
		lineRect.top = type1Rect.centerY();
		lineRect.bottom = type3Rect.centerY();
		pointYOfType2OnLine = type2Rect.centerY();

	}

	public void setViews(Location location) {
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

		RelativeLayout.LayoutParams type1LayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
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

		requestLayout();
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		//line그리기
		canvas.drawLine(lineRect.left, lineRect.right, lineRect.top, lineRect.bottom, linePaint);
	}

	@Override
	protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
		return super.drawChild(canvas, child, drawingTime);
	}

	public static class SunSetRiseInfoView extends View {
		private final TextPaint typePaint;
		private final TextPaint timePaint;
		private final Rect imgRect;
		private Drawable img;

		private final int typeTextHeight;
		private final int timeTextHeight;
		private final int typeLeftMargin;
		private final int timeTopMargin;
		private final int imgSize;

		private Calendar sunSetRiseCalendar;
		private String sunSetRiseStr;

		private final SimpleDateFormat dateFormat = new SimpleDateFormat("M.d E hh:mm", Locale.getDefault());

		public SunSetRiseInfoView(Context context) {
			super(context);
			Rect rect = new Rect();

			String tempStr = "0";

			typePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			typePaint.setColor(AppTheme.getColor(context, R.attr.textColorInWeatherCard));
			typePaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13f, getResources().getDisplayMetrics()));
			typePaint.getTextBounds(tempStr, 0, 1, rect);
			typeTextHeight = rect.height();

			timePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			timePaint.setColor(AppTheme.getColor(context, R.attr.textColorInWeatherCard));
			timePaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, getResources().getDisplayMetrics()));
			timePaint.getTextBounds(tempStr, 0, 1, rect);
			timeTextHeight = rect.height();

			typeLeftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, context.getResources().getDisplayMetrics());
			timeTopMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, context.getResources().getDisplayMetrics());
			imgSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, context.getResources().getDisplayMetrics());
			this.imgRect = new Rect(0, 0, imgSize, imgSize);

			setWillNotDraw(false);
		}

		public void setViews(Calendar sunSetRiseCalendar, String sunSetRiseStr) {
			this.sunSetRiseCalendar = sunSetRiseCalendar;
			this.sunSetRiseStr = sunSetRiseStr;
			//이미지 설정
			img = ContextCompat.getDrawable(getContext(), R.drawable.temp_icon);
			img.setBounds(imgRect);
		}


		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}

		@Override
		protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		}

		@Override
		protected void onDraw(Canvas canvas) {
			img.draw(canvas);

			canvas.drawText(sunSetRiseStr, imgRect.right + typeLeftMargin, imgRect.bottom - typeTextHeight / 2f, typePaint);
			canvas.drawText(dateFormat.format(sunSetRiseCalendar.getTime()), imgRect.left,
					imgRect.bottom + timeTopMargin + timeTextHeight / 2f, timePaint);
		}

	}
}
