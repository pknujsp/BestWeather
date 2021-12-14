package com.lifedawn.bestweather.weathers.detailfragment.sunsetrise;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.model.GradientColor;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.databinding.FragmentDetailSunRiseSetBinding;
import com.lifedawn.bestweather.weathers.dataprocessing.util.SunRiseSetUtil;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class DetailSunRiseSetFragment extends Fragment {
	private FragmentDetailSunRiseSetBinding binding;
	private String addressName;
	private ZoneId zoneId;
	private Double latitude;
	private Double longitude;

	private int minusWeeks = 1;
	private int plusWeeks = 16;

	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private final DateTimeFormatter dateFormatterInInfo = DateTimeFormatter.ofPattern("yyyy.M.d EEE");
	private final DateTimeFormatter timeFormatterInInfo = DateTimeFormatter.ofPattern("a hh:mm");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getArguments();
		addressName = bundle.getString(BundleKey.AddressName.name());
		zoneId = (ZoneId) bundle.getSerializable(BundleKey.TimeZone.name());
		latitude = bundle.getDouble(BundleKey.Latitude.name());
		longitude = bundle.getDouble(BundleKey.Longitude.name());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentDetailSunRiseSetBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.toolbar.fragmentTitle.setText(R.string.detailSunRiseSet);
		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});

		binding.chart.setDrawGridBackground(false);
		binding.chart.getDescription().setEnabled(false);
		binding.chart.setScaleXEnabled(false);
		binding.chart.setDrawValueAboveBar(true);
		binding.chart.setHighlightFullBarEnabled(false);
		binding.chart.setExtraOffsets(0, 10, 0, 20);

		binding.chart.getAxisLeft().setEnabled(false);
		binding.chart.getAxisRight().setAxisMaximum(720f);
		binding.chart.getAxisRight().setAxisMinimum(-720f);
		binding.chart.getAxisRight().setDrawGridLines(false);
		binding.chart.getAxisRight().setDrawZeroLine(true);
		binding.chart.getAxisRight().setTextSize(12f);
		binding.chart.getAxisRight().setYOffset(-3f);
		binding.chart.getAxisRight().setLabelCount(4);
		binding.chart.getAxisRight().setValueFormatter(new TimeFormatter());

		XAxis xAxis = binding.chart.getXAxis();
		xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
		xAxis.setDrawGridLines(false);
		xAxis.setDrawAxisLine(false);
		xAxis.setTextSize(12f);
		xAxis.setCenterAxisLabels(false);
		xAxis.setLabelCount(12);
		xAxis.setGranularity(1f);
		xAxis.setGranularityEnabled(true);

		binding.chart.getLegend().setEnabled(false);

		// 기준 날짜 1주일 전 - 기준 날짜 - 기준 날짜 3달 후
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				List<SunRiseSetUtil.SunRiseSetObj> sunRiseSetObjList = calcSunRiseSets(ZonedDateTime.now(zoneId));

				if (sunRiseSetObjList.isEmpty()) {
					//error
					binding.chart.clearValues();
				} else {
					//차트 설정
					List<BarEntry> sunRiseSetTimeDataList = new ArrayList<>();
					List<String> dateListForAxis = new ArrayList<>();
					DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M.d E");

					final int rightColor = ContextCompat.getColor(getContext(), R.color.sunGradientStart);
					final int leftColor = ContextCompat.getColor(getContext(), R.color.sunGradientEnd);
					final int todayColor = ContextCompat.getColor(getContext(), android.R.color.holo_blue_bright);

					int index = 0;
					int leftMinutes;
					int rightMinutes;

					final int todayIndex = Math.abs(minusWeeks * 7);

					int[] barColors = new int[sunRiseSetObjList.size() * 2];
					int colorIndex = 0;

					for (SunRiseSetUtil.SunRiseSetObj sunRiseSetObj : sunRiseSetObjList) {
						if (index == todayIndex) {
							barColors[colorIndex + 1] = todayColor;
							barColors[colorIndex] = todayColor;
						} else {
							barColors[colorIndex + 1] = rightColor;
							barColors[colorIndex] = leftColor;
						}
						colorIndex = colorIndex + 2;

						dateListForAxis.add(sunRiseSetObj.getZonedDateTime().format(dateFormatter));

						leftMinutes = -720 + 60 * sunRiseSetObj.getSunrise().get(Calendar.HOUR_OF_DAY) +
								sunRiseSetObj.getSunrise().get(Calendar.MINUTE);
						rightMinutes = 60 * sunRiseSetObj.getSunset().get(Calendar.HOUR_OF_DAY) +
								sunRiseSetObj.getSunset().get(Calendar.MINUTE) - 720;

						sunRiseSetTimeDataList.add(new BarEntry(index, new float[]{leftMinutes, rightMinutes}, sunRiseSetObj));
						index++;
					}

					BarDataSet barDataSet = new BarDataSet(sunRiseSetTimeDataList, "sunRiseSet");
					barDataSet.setDrawIcons(false);
					barDataSet.setValueFormatter(new TimeFormatter());
					barDataSet.setValueTextSize(12f);
					barDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);

					barDataSet.setColors(barColors);

					BarData barData = new BarData(barDataSet);
					barData.setHighlightEnabled(false);

					binding.chart.setData(barData);
					binding.chart.getXAxis().setValueFormatter(new XAxisDateFormatter(dateListForAxis));
				}
				MainThreadWorker.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						binding.chart.invalidate();

						if (sunRiseSetObjList.size() > 0) {
							binding.chart.zoom(0f, 8f, 0f, 0f);
						}
					}
				});
			}
		});
	}

	private List<SunRiseSetUtil.SunRiseSetObj> calcSunRiseSets(ZonedDateTime criteriaZonedDateTime) {
		//TimeZone localTimeZone = TimeZone.getDefault();
		//final int localOffset = localTimeZone.getRawOffset();
		ZoneOffset zoneOffset = criteriaZonedDateTime.getOffset();
		final int offset = zoneOffset.getTotalSeconds() * 1000;

		TimeZone realTimeZone = new SimpleTimeZone(offset, "");
		/*
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			realTimeZone = TimeZone.getTimeZone(zoneId);
		} else {
			realTimeZone = TimeZone.getTimeZone(zoneId.toString());
		}

		 */
		ZonedDateTime criteria = ZonedDateTime.of(criteriaZonedDateTime.toLocalDateTime(), zoneId);

		ZonedDateTime beginUtc0ZonedDateTime = criteria.minusWeeks(minusWeeks);
		ZonedDateTime beginRealZonedDateTime = criteria.minusWeeks(minusWeeks);
		ZonedDateTime endUtc0ZonedDateTime = criteria.plusWeeks(plusWeeks);

		beginUtc0ZonedDateTime = beginUtc0ZonedDateTime.withZoneSameLocal(ZoneOffset.UTC);
		endUtc0ZonedDateTime = endUtc0ZonedDateTime.withZoneSameLocal(ZoneOffset.UTC);

		long beginDay;
		final long endDay = TimeUnit.MILLISECONDS.toDays(endUtc0ZonedDateTime.toInstant().toEpochMilli());

		List<SunRiseSetUtil.SunRiseSetObj> list = new ArrayList<>();
		SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(new Location(latitude, longitude), realTimeZone);
		Calendar calendar = Calendar.getInstance(realTimeZone);

		do {
			calendar.setTimeInMillis(beginRealZonedDateTime.toInstant().toEpochMilli());

			SunRiseSetUtil.SunRiseSetObj sunRiseSetObj =
					new SunRiseSetUtil.SunRiseSetObj(beginRealZonedDateTime, calculator.getOfficialSunriseCalendarForDate(calendar),
							calculator.getOfficialSunsetCalendarForDate(calendar));
			if (sunRiseSetObj.getSunrise() == null || sunRiseSetObj.getSunset() == null) {
				// 일출/일몰 계산 오류 발생 하면 리스트 비우고 반환
				list.clear();
				break;
			}
			list.add(sunRiseSetObj);

			beginUtc0ZonedDateTime = beginUtc0ZonedDateTime.plusDays(1);
			beginRealZonedDateTime = beginRealZonedDateTime.plusDays(1);
			beginDay = TimeUnit.MILLISECONDS.toDays(beginUtc0ZonedDateTime.toInstant().toEpochMilli());
		} while (beginDay <= endDay);


		return list;
	}

	private static class XAxisDateFormatter extends ValueFormatter {
		final List<String> dateList;
		int val;

		public XAxisDateFormatter(List<String> dateList) {
			this.dateList = dateList;
		}

		@Override
		public String getAxisLabel(float value, AxisBase axis) {
			val = (int) value;
			return (dateList.size() > val && val >= 0) ? dateList.get(val) : "";
		}
	}

	private static class TimeFormatter extends ValueFormatter {
		final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
		LocalTime localTime = LocalTime.of(12, 0);

		@Override
		public String getAxisLabel(float value, AxisBase axis) {
			return convert(value);
		}

		@Override
		public String getFormattedValue(float value) {
			return convert(value);
		}

		private String convert(float value) {
			return localTime.plusMinutes((long) value).format(timeFormatter);
			//			return localTime.plusMinutes((long) value).format(timeFormatter);
		}
	}

}