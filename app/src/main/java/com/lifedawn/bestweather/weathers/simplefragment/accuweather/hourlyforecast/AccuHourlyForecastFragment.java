package com.lifedawn.bestweather.weathers.simplefragment.accuweather.hourlyforecast;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.ClockUtil;
import com.lifedawn.bestweather.retrofit.responses.accuweather.twelvehoursofhourlyforecasts.TwelveHoursOfHourlyForecastsResponse;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleHourlyForecastFragment;
import com.lifedawn.bestweather.weathers.view.DateView;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class AccuHourlyForecastFragment extends BaseSimpleHourlyForecastFragment {
	private TwelveHoursOfHourlyForecastsResponse twelveHoursOfHourlyForecastsResponse;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	public AccuHourlyForecastFragment setTwelveHoursOfHourlyForecastsResponse(TwelveHoursOfHourlyForecastsResponse twelveHoursOfHourlyForecastsResponse) {
		this.twelveHoursOfHourlyForecastsResponse = twelveHoursOfHourlyForecastsResponse;
		return this;
	}

	@Override
	public void setValuesToViews() {
		//날짜, 시각, 날씨, 기온, 강수확률, 강설확률, 강설량
		Context context = getContext();

		final int DATE_ROW_HEIGHT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f,
				getResources().getDisplayMetrics());
		final int CLOCK_ROW_HEIGHT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 34f,
				getResources().getDisplayMetrics());
		final int PROBABILITY_OF_PRECIPITATION_ROW_HEIGHT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 34f,
				getResources().getDisplayMetrics());
		final int MARGIN = (int) context.getResources().getDimension(R.dimen.row_top_bottom_margin_in_forecast_scroll_view);
		List<TwelveHoursOfHourlyForecastsResponse.Item> items = twelveHoursOfHourlyForecastsResponse.getItems();

		final int COLUMN_COUNT = items.size();
		final int COLUMN_WIDTH = (int) context.getResources().getDimension(R.dimen.column_width_in_forecast_scroll_view);
		final int VIEW_WIDTH = COLUMN_COUNT * COLUMN_WIDTH;

		//label column 설정
		final int LABEL_VIEW_WIDTH = (int) context.getResources().getDimension(R.dimen.label_view_width_in_forecast_scroll_view);

		addLabelView(R.drawable.temp_img, getString(R.string.date), LABEL_VIEW_WIDTH, DATE_ROW_HEIGHT, MARGIN);
		addLabelView(R.drawable.temp_img, getString(R.string.clock), LABEL_VIEW_WIDTH, DATE_ROW_HEIGHT, MARGIN);
		addLabelView(R.drawable.temp_img, getString(R.string.probability_of_precipitation), LABEL_VIEW_WIDTH,
				PROBABILITY_OF_PRECIPITATION_ROW_HEIGHT, MARGIN);

		dateRow = null;
		LinearLayout clockRow = addValuesView(VIEW_WIDTH, CLOCK_ROW_HEIGHT, MARGIN);
		LinearLayout probabilityOfPrecipitationRow = addValuesView(VIEW_WIDTH, PROBABILITY_OF_PRECIPITATION_ROW_HEIGHT, MARGIN);

		//시각 --------------------------------------------------------------------------
		Calendar date = Calendar.getInstance();
		vilageFcstXMap.clear();
		date.setTimeInMillis(Long.parseLong(items.get(0).getEpochDateTime()));
		date.add(Calendar.DATE, -10);
		long lastDate = date.getTimeInMillis();

		List<DateValue> dateValueList = new ArrayList<>();
		int beginX = 0;

		SimpleDateFormat MdE = new SimpleDateFormat("MdE", Locale.getDefault());

		for (int col = 0; col < COLUMN_COUNT; col++) {
			TextView textView = new TextView(context);
			date.setTimeInMillis(Long.parseLong(items.get(col).getEpochDateTime()));

			if (date.get(Calendar.HOUR_OF_DAY) == 0 || col == 0) {
				if (dateValueList.size() > 0) {
					dateValueList.get(dateValueList.size() - 1).endX = COLUMN_WIDTH * (col - 1) + COLUMN_WIDTH / 2;
				}

				beginX = COLUMN_WIDTH * col + COLUMN_WIDTH / 2;
				dateValueList.add(new DateValue(beginX, MdE.format(date.getTime())));
			}

			setValueTextView(textView, Integer.toString(date.get(Calendar.HOUR_OF_DAY)));

			if (!ClockUtil.areSameDate(lastDate, date.getTimeInMillis())) {
				vilageFcstXMap.put(COLUMN_WIDTH * col, date.getTime());
				lastDate = date.getTimeInMillis();
			}

			LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(COLUMN_WIDTH, CLOCK_ROW_HEIGHT);
			textParams.gravity = Gravity.CENTER;
			clockRow.addView(textView, textParams);
		}

		dateValueList.get(dateValueList.size() - 1).endX = COLUMN_WIDTH * (COLUMN_COUNT - 1) + COLUMN_WIDTH / 2;

		dateRow = new DateView(getContext(), dateValueList);
		dateRow.measure(VIEW_WIDTH, DATE_ROW_HEIGHT);

		//강수확률 ------------------------------------------------------------------------------
		for (int col = 0; col < COLUMN_COUNT; col++) {
			TextView textView = new TextView(context);
			setValueTextView(textView, items.get(col).getPrecipitationProbability());

			LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(COLUMN_WIDTH, PROBABILITY_OF_PRECIPITATION_ROW_HEIGHT);
			textParams.gravity = Gravity.CENTER;
			probabilityOfPrecipitationRow.addView(textView, textParams);
		}

		binding.forecastView.addView(dateRow, new LinearLayout.LayoutParams(VIEW_WIDTH, DATE_ROW_HEIGHT));
		binding.forecastView.addView(clockRow);
		binding.forecastView.addView(probabilityOfPrecipitationRow);
	}

}