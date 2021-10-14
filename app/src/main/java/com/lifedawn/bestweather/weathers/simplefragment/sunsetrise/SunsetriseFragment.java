package com.lifedawn.bestweather.weathers.simplefragment.sunsetrise;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.FragmentSunsetriseBinding;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class SunsetriseFragment extends Fragment implements IWeatherValues {
	private FragmentSunsetriseBinding binding;
	private Drawable sunRiseImg;
	private Drawable sunSetImg;
	private Location location;

	public enum SunSetRiseType {
		RISE, SET
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@org.jetbrains.annotations.Nullable
	@Override
	public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
	                         @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		binding = FragmentSunsetriseBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.weatherCardViewHeader.detailForecast.setVisibility(View.GONE);
		binding.weatherCardViewHeader.compareForecast.setVisibility(View.GONE);
		binding.weatherCardViewHeader.forecastName.setText(R.string.sun_set_rise);
	}

	@Override
	public void setValuesToViews() {

	}

	public static class SunSetRiseView extends RelativeLayout {
		private final SimpleDateFormat dateFormat = new SimpleDateFormat("M.d E hh:mm", Locale.getDefault());
		private int type1BottomMargin;
		private int type2BottomMargin;

		public SunSetRiseView(Context context) {
			super(context);
			init();
		}

		public SunSetRiseView(Context context, AttributeSet attrs) {
			super(context, attrs);
			init();
		}

		private void init() {
			setWillNotDraw(false);

			type1BottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, getResources().getDisplayMetrics());
			type2BottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f, getResources().getDisplayMetrics());
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}

		@Override
		protected void onLayout(boolean changed, int l, int t, int r, int b) {

		}

		public void setViews(Location location) {
			removeAllViews();
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
				type1View.setViews(sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(yesterdayCalendar), SunSetRiseType.SET,
						getContext().getString(R.string.sunset));
				type2View.setViews(todaySunRiseCalendar, SunSetRiseType.RISE,
						getContext().getString(R.string.sunrise));
				type3View.setViews(todaySunSetCalendar, SunSetRiseType.SET,
						getContext().getString(R.string.sunset));
			} else if (todayCalendar.compareTo(todaySunRiseCalendar) >= 0 && todayCalendar.compareTo(todaySunSetCalendar) <= 0) {
				//일출 후, 일몰 전 (일출 <= 현재 시각 <= 일몰)
				//순서 : 당일 11.07 일출 - 당일 11.07 일몰 - 다음날 11.08 일출
				type1View.setViews(todaySunRiseCalendar, SunSetRiseType.RISE,
						getContext().getString(R.string.sunrise));
				type2View.setViews(todaySunSetCalendar, SunSetRiseType.SET,
						getContext().getString(R.string.sunset));
				type3View.setViews(sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(tomorrowCalendar), SunSetRiseType.RISE,
						getContext().getString(R.string.sunrise));
			} else {
				//일몰 후 (일몰 < 현재 시각 < 24시 0분 )
				//순서 : 당일 11.07 일몰 - 다음날 11.08 일출 - 다음날 11.08 일몰
				type1View.setViews(todaySunSetCalendar, SunSetRiseType.SET,
						getContext().getString(R.string.sunset));
				type2View.setViews(sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(tomorrowCalendar), SunSetRiseType.RISE,
						getContext().getString(R.string.sunrise));
				type3View.setViews(sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(tomorrowCalendar), SunSetRiseType.SET,
						getContext().getString(R.string.sunset));
			}

			RelativeLayout.LayoutParams type1LayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			type1LayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			type1LayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
			addView(type1View, type1LayoutParams);

			RelativeLayout.LayoutParams type2LayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			type1LayoutParams.addRule(RelativeLayout.BELOW, R.id.sunSetRiseType1View);
			type1LayoutParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.sunSetRiseType1View);
			type1LayoutParams.bottomMargin = type1BottomMargin;
			addView(type2View, type2LayoutParams);

			RelativeLayout.LayoutParams type3LayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			type1LayoutParams.addRule(RelativeLayout.BELOW, R.id.sunSetRiseType1View);
			type1LayoutParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.sunSetRiseType1View);
			addView(type3View, type3LayoutParams);

			invalidate();
			requestLayout();
		}

		@Override
		protected void onDraw(Canvas canvas) {
			//line그리기
		}
	}

	public static class SunSetRiseInfoView extends View {
		private final TextPaint typePaint;
		private final TextPaint timePaint;
		private Drawable img;

		private final int typeTextHeight;
		private final int timeTextHeight;
		private final int typeLeftMargin;
		private final int timeTopMargin;
		private final int imgSize;

		private Calendar sunSetRiseCalendar;
		private String sunSetRiseStr;
		private SunSetRiseType sunSetRiseType;

		private final SimpleDateFormat dateFormat = new SimpleDateFormat("M.d E hh:mm", Locale.getDefault());

		public SunSetRiseInfoView(Context context) {
			super(context);
			Rect rect = new Rect();

			typePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			typePaint.setColor(Color.WHITE);
			typePaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 13f, getResources().getDisplayMetrics()));
			typePaint.getTextBounds("0", 0, 1, rect);
			typeTextHeight = rect.height();

			timePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			timePaint.setColor(Color.WHITE);
			timePaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f,
					getResources().getDisplayMetrics()));
			timePaint.getTextBounds("0", 0, 1, rect);
			timeTextHeight = rect.height();

			typeLeftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, context.getResources().getDisplayMetrics());
			timeTopMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, context.getResources().getDisplayMetrics());
			imgSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, context.getResources().getDisplayMetrics());

			setWillNotDraw(false);
		}

		public void setViews(Calendar sunSetRiseCalendar, SunSetRiseType sunSetRiseType, String sunSetRiseStr) {
			this.sunSetRiseCalendar = sunSetRiseCalendar;
			this.sunSetRiseType = sunSetRiseType;
			this.sunSetRiseStr = sunSetRiseStr;

			//이미지 설정
			img = ContextCompat.getDrawable(getContext(), R.drawable.temp_icon);
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
			Rect imgRect = new Rect(0, 0, imgSize, imgSize);
			img.setBounds(imgRect);

			img.draw(canvas);
			canvas.drawText(sunSetRiseStr, imgRect.right + typeLeftMargin, imgRect.bottom - typeTextHeight / 2f, typePaint);
			canvas.drawText(dateFormat.format(sunSetRiseCalendar.getTime()), imgRect.left, imgRect.bottom + timeTopMargin + typeTextHeight / 2f,
					timePaint);
		}

	}
}