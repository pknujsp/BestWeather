package com.lifedawn.bestweather.weathers.simplefragment.sunsetrise;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.FragmentSunsetriseBinding;
import com.lifedawn.bestweather.weathers.dataprocessing.util.SunsetriseUtil;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.TimeZone;

public class SunsetriseFragment extends Fragment implements IWeatherValues {
	private FragmentSunsetriseBinding binding;
	
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
	
	static class SunSetRiseView extends View {
		private final Paint timeLinePaint;
		private final TextPaint sunSetRiseTypePaint;
		private final TextPaint sunSetRiseTimePaint;
		private final TextPaint currentTimeLabelPaint;
		private final TextPaint currentTimeCirclePaint;
		private final TextPaint currentTimePaint;
		private final Drawable sunRiseImg;
		private final Drawable sunSetImg;
		private final Location location;
		
		public SunSetRiseView(Context context, Double latitude, Double longitude) {
			super(context);
			this.location = new Location(latitude, longitude);
			
			sunRiseImg = ContextCompat.getDrawable(context, R.drawable.temp_img);
			sunSetImg = ContextCompat.getDrawable(context, R.drawable.temp_img);
			
			timeLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			timeLinePaint.setColor(Color.WHITE);
			
			sunSetRiseTypePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			sunSetRiseTypePaint.setColor(Color.WHITE);
			
			sunSetRiseTimePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			sunSetRiseTimePaint.setColor(Color.WHITE);
			
			currentTimeLabelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			currentTimeLabelPaint.setColor(Color.WHITE);
			
			currentTimeCirclePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			currentTimeCirclePaint.setColor(Color.WHITE);
			
			currentTimePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			currentTimePaint.setColor(Color.WHITE);
			
			setWillNotDraw(false);
		}
		
		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
		
		@Override
		protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
			super.onLayout(changed, left, top, right, bottom);
		}
		
		@SuppressLint("DrawAllocation")
		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			
			SunriseSunsetCalculator sunriseSunsetCalculator = new SunriseSunsetCalculator(location, TimeZone.getDefault());
			
			final Calendar todayCalendar = Calendar.getInstance();
			final Calendar yesterdayCalendar = (Calendar) todayCalendar.clone();
			final Calendar tomorrowCalendar = (Calendar) todayCalendar.clone();
			
			yesterdayCalendar.add(Calendar.DATE, -1);
			tomorrowCalendar.add(Calendar.DATE, 1);
			
			Calendar todaySunRiseCalendar = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(todayCalendar);
			Calendar todaySunSetCalendar = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(todayCalendar);
			
			if (todayCalendar.before(todaySunRiseCalendar)) {
				//일출 전 새벽 (0시 0분 <= 현재 시각 < 일출)
				//순서 : 전날 11.06 일몰 - 당일 11.07 일출 - 당일 11.07 일몰
				
			} else if (todayCalendar.compareTo(todaySunRiseCalendar) >= 0 && todayCalendar.compareTo(todaySunSetCalendar) <= 0) {
				//일출 후, 일몰 전 (일출 <= 현재 시각 <= 일몰)
				//순서 : 당일 11.07 일출 - 당일 11.07 일몰 - 다음날 11.08 일출
				
			} else {
				//일몰 후 (일몰 < 현재 시각 < 24시 0분 )
				//순서 : 당일 11.07 일몰 - 다음날 11.08 일출 - 다음날 11.08 일몰
				
			}
		}
		
		private void drawCurrentTime(Canvas canvas) {
		
		}
	}
}