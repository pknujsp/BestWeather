package com.lifedawn.bestweather.weathers.simplefragment.sunsetrise;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
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
import android.widget.LinearLayout;
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
	private SunSetRiseView sunSetRiseView;
	private Location location;
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("M.d E H:mm", Locale.getDefault());

	public enum SunSetRiseType {
		RISE, SET
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getArguments();
		location = new Location(bundle.getDouble("latitude"), bundle.getDouble("longitude"));
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

		setValuesToViews();
	}


	@Override
	public void setValuesToViews() {
		int type1BottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30f, getResources().getDisplayMetrics());
		int type2BottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, getResources().getDisplayMetrics());

		SunriseSunsetCalculator sunriseSunsetCalculator = new SunriseSunsetCalculator(location, TimeZone.getDefault());

		final Calendar todayCalendar = Calendar.getInstance();
		final Calendar yesterdayCalendar = (Calendar) todayCalendar.clone();
		final Calendar tomorrowCalendar = (Calendar) todayCalendar.clone();

		yesterdayCalendar.add(Calendar.DATE, -1);
		tomorrowCalendar.add(Calendar.DATE, 1);

		Calendar todaySunRiseCalendar = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(todayCalendar);
		Calendar todaySunSetCalendar = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(todayCalendar);

		SunSetRiseInfoView type1View;
		SunSetRiseInfoView type2View;
		SunSetRiseInfoView type3View;

		if (todayCalendar.before(todaySunRiseCalendar)) {
			//일출 전 새벽 (0시 0분 <= 현재 시각 < 일출)
			//순서 : 전날 11.06 일몰 - 당일 11.07 일출 - 당일 11.07 일몰
			type1View = new SunSetRiseInfoView(getContext(),
					getContext().getString(R.string.sunset),
					sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(yesterdayCalendar),
					R.drawable.temp_icon);
			type2View = new SunSetRiseInfoView(getContext(), getContext().getString(R.string.sunrise),
					todaySunRiseCalendar, R.drawable.temp_icon);
			type3View = new SunSetRiseInfoView(getContext(), getContext().getString(R.string.sunset),
					todaySunSetCalendar, R.drawable.temp_icon);
		} else if (todayCalendar.compareTo(todaySunRiseCalendar) >= 0 && todayCalendar.compareTo(todaySunSetCalendar) <= 0) {
			//일출 후, 일몰 전 (일출 <= 현재 시각 <= 일몰)
			//순서 : 당일 11.07 일출 - 당일 11.07 일몰 - 다음날 11.08 일출
			type1View = new SunSetRiseInfoView(getContext(), getContext().getString(R.string.sunrise),
					todaySunRiseCalendar, R.drawable.temp_icon);
			type2View = new SunSetRiseInfoView(getContext(), getContext().getString(R.string.sunset)
					, todaySunSetCalendar, R.drawable.temp_icon);
			type3View = new SunSetRiseInfoView(getContext(), getContext().getString(R.string.sunrise)
					, sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(tomorrowCalendar), R.drawable.temp_icon);
		} else {
			//일몰 후 (일몰 < 현재 시각 < 24시 0분 )
			//순서 : 당일 11.07 일몰 - 다음날 11.08 일출 - 다음날 11.08 일몰
			type1View = new SunSetRiseInfoView(getContext(), getContext().getString(R.string.sunset)
					, todaySunSetCalendar, R.drawable.temp_icon);
			type2View = new SunSetRiseInfoView(getContext(), getContext().getString(R.string.sunrise)
					, sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(tomorrowCalendar), R.drawable.temp_icon);
			type3View = new SunSetRiseInfoView(getContext(), getContext().getString(R.string.sunset)
					, sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(tomorrowCalendar), R.drawable.temp_icon);
		}

		type1View.setId(R.id.sunSetRiseType1View);
		type2View.setId(R.id.sunSetRiseType2View);
		type3View.setId(R.id.sunSetRiseType3View);

		RelativeLayout.LayoutParams type1LayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams type2LayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams type3LayoutParams =
				new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);

		type1LayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		type1LayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		type1LayoutParams.bottomMargin = type1BottomMargin;

		type2LayoutParams.addRule(RelativeLayout.BELOW, R.id.sunSetRiseType1View);
		type2LayoutParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.sunSetRiseType1View);
		type2LayoutParams.bottomMargin = type2BottomMargin;

		type3LayoutParams.addRule(RelativeLayout.BELOW, R.id.sunSetRiseType2View);
		type3LayoutParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.sunSetRiseType1View);

		binding.sunSetRiseLayout.addView(type1View, type1LayoutParams);
		binding.sunSetRiseLayout.addView(type2View, type2LayoutParams);
		binding.sunSetRiseLayout.addView(type3View, type3LayoutParams);
	}


}