package com.lifedawn.bestweather.ui.weathers.detailfragment.sunsetrise;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.constants.BundleKey;
import com.lifedawn.bestweather.databinding.FragmentDetailSunRiseSetBinding;
import com.lifedawn.bestweather.data.MyApplication;
import com.lifedawn.bestweather.ui.weathers.dataprocessing.util.SunRiseSetUtil;
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
import java.util.concurrent.TimeUnit;


public class DetailSunRiseSetFragment extends Fragment {
	private FragmentDetailSunRiseSetBinding binding;
	private String addressName;
	private ZoneId zoneId;
	private Double latitude;
	private Double longitude;
	private Bundle bundle;

	private int minusWeeks = 1;
	private int plusWeeks = 20;

	private final ExecutorService executorService = MyApplication.getExecutorService();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		bundle = savedInstanceState != null ? savedInstanceState : getArguments();
		addressName = bundle.getString(BundleKey.AddressName.name());
		zoneId = (ZoneId) bundle.getSerializable(BundleKey.TimeZone.name());
		latitude = bundle.getDouble(BundleKey.Latitude.name());
		longitude = bundle.getDouble(BundleKey.Longitude.name());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentDetailSunRiseSetBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	@SuppressLint({"ClickableViewAccessibility", "MissingPermission"})
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) binding.toolbar.getRoot().getLayoutParams();
		layoutParams.topMargin = MyApplication.getStatusBarHeight();
		binding.toolbar.getRoot().setLayoutParams(layoutParams);

		binding.adViewBelowScrollView.loadAd(new AdRequest.Builder().build());
		binding.adViewBelowScrollView.setAdListener(new AdListener() {
			@Override
			public void onAdClosed() {
				super.onAdClosed();
				binding.adViewBelowScrollView.loadAd(new AdRequest.Builder().build());
			}
		});

		binding.toolbar.fragmentTitle.setText(R.string.detailSunRiseSet);
		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});

		binding.chart.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return false;
			}
		});

		binding.chart.setNoDataText(getString(R.string.sun_set_rise));
		binding.chart.setNoDataTextColor(Color.BLUE);
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
		xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
		xAxis.setDrawGridLines(false);
		xAxis.setDrawAxisLine(false);
		xAxis.setTextSize(13f);
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
					binding.chart.setNoDataText(getString(R.string.failed_calculating_sun_rise_set));
				} else {
					//차트 설정
					List<BarEntry> sunRiseSetTimeDataList = new ArrayList<>();
					List<String> dateListForAxis = new ArrayList<>();
					DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M.d E");
					Context context = requireContext().getApplicationContext();

					final int rightColor = ContextCompat.getColor(context, R.color.sunGradientStart);
					final int leftColor = ContextCompat.getColor(context, R.color.sunGradientEnd);
					final int todayColor = ContextCompat.getColor(context, android.R.color.holo_blue_bright);

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
					barDataSet.setValueTextSize(13f);
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
							binding.chart.zoom(0f, 7f, 0f, 0f);

							final float x = binding.chart.getLeft() + binding.chart.getWidth() / 2f;
							final float y = binding.chart.getTop() + binding.chart.getHeight() / 2f;
							/*
							4384
							4457
							4531
							4594
							*/

							Handler handler = new Handler();

							final long firstDownTime = SystemClock.uptimeMillis();
							final long firstEventTime = firstDownTime;

							binding.chart.dispatchTouchEvent(MotionEvent.obtain(firstDownTime,
									firstEventTime,
									MotionEvent.ACTION_DOWN, x, y, 0));

							handler.postDelayed(new Runnable() {
								@Override
								public void run() {
									binding.chart.dispatchTouchEvent(MotionEvent.obtain(firstDownTime,
											SystemClock.uptimeMillis(),
											MotionEvent.ACTION_UP, x, y, 0));

									handler.postDelayed(new Runnable() {
										@Override
										public void run() {
											final long secDownTime = SystemClock.uptimeMillis();
											final long secEventTime = secDownTime;

											binding.chart.dispatchTouchEvent(MotionEvent.obtain(secDownTime,
													secEventTime,
													MotionEvent.ACTION_DOWN, x, y, 0));

											handler.postDelayed(new Runnable() {
												@Override
												public void run() {
													binding.chart.dispatchTouchEvent(MotionEvent.obtain(secDownTime,
															SystemClock.uptimeMillis(),
															MotionEvent.ACTION_UP, x, y, 0));
												}
											}, 20);
										}
									}, 40);
								}
							}, 20);


						}
					}
				});
			}
		});
	}

	private List<SunRiseSetUtil.SunRiseSetObj> calcSunRiseSets(ZonedDateTime criteriaZonedDateTime) {
		ZoneOffset zoneOffset = criteriaZonedDateTime.getOffset();
		final int offset = zoneOffset.getTotalSeconds() * 1000;

		TimeZone realTimeZone = new SimpleTimeZone(offset, "");

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
				return list;
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