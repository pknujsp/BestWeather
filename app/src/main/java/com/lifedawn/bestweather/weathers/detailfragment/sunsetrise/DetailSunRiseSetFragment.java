package com.lifedawn.bestweather.weathers.detailfragment.sunsetrise;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.databinding.FragmentDetailSunRiseSetBinding;
import com.lifedawn.bestweather.weathers.dataprocessing.util.SunRiseSetUtil;
import com.lifedawn.bestweather.weathers.view.DateView;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
		binding.sunRiseSetInfoLayout.setVisibility(View.GONE);

		binding.lineChart.setNoDataText(getString(R.string.failed_calculating_sun_rise_set));
		binding.lineChart.getAxisLeft().setAxisMinimum(0f);
		//1439 == 23:59
		binding.lineChart.getAxisLeft().setAxisMaximum(1439f);
		binding.lineChart.getDescription().setText(getString(R.string.date));
		binding.lineChart.getDescription().setTextSize(15f);

		binding.lineChart.setScaleYEnabled(false);
		binding.lineChart.getAxisRight().setEnabled(false);
		binding.lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
		binding.lineChart.setDrawBorders(true);
		binding.lineChart.setPinchZoom(false);

		binding.lineChart.getAxisLeft().setLabelCount(24);
		binding.lineChart.getAxisLeft().setTextSize(14f);
		binding.lineChart.getAxisLeft().setGridLineWidth(1.5f);
		binding.lineChart.getXAxis().setLabelCount(8);
		binding.lineChart.getXAxis().setTextSize(14f);
		binding.lineChart.getXAxis().setGridLineWidth(1.5f);

		binding.lineChart.setBackgroundColor(Color.WHITE);
		binding.lineChart.setGridBackgroundColor(ContextCompat.getColor(getContext(), R.color.dayColor));
		binding.lineChart.setDrawGridBackground(true);
		binding.lineChart.getLegend().setEnabled(false);
		binding.lineChart.getXAxis().setGranularityEnabled(true);
		binding.lineChart.getXAxis().setGranularity(1f);

		binding.lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
			@Override
			public void onValueSelected(Entry e, Highlight h) {
				SunRiseSetUtil.SunRiseSetObj sunRiseSetObj = (SunRiseSetUtil.SunRiseSetObj) e.getData();
				binding.date.setText(sunRiseSetObj.getZonedDateTime().format(dateFormatterInInfo));

				LocalTime localTime = LocalTime.of(sunRiseSetObj.getSunrise().get(Calendar.HOUR_OF_DAY),
						sunRiseSetObj.getSunrise().get(Calendar.MINUTE));

				binding.sunRiseTime.setText(localTime.format(timeFormatterInInfo));

				localTime = LocalTime.of(sunRiseSetObj.getSunset().get(Calendar.HOUR_OF_DAY),
						sunRiseSetObj.getSunset().get(Calendar.MINUTE));
				binding.sunSetTime.setText(localTime.format(timeFormatterInInfo));

				binding.sunRiseSetInfoLayout.setVisibility(View.VISIBLE);
			}

			@Override
			public void onNothingSelected() {
				binding.sunRiseSetInfoLayout.setVisibility(View.GONE);
			}
		});

		binding.goToToday.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				float x = Math.abs((float) (minusWeeks * 7));
				binding.lineChart.moveViewToX(x - 1);
				binding.lineChart.highlightValue(new Highlight(x, 0f, 0), true);
			}
		});

		// 기준 날짜 1주일 전 - 기준 날짜 - 기준 날짜 3달 후
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				List<SunRiseSetUtil.SunRiseSetObj> sunRiseSetObjList = calcSunRiseSets(ZonedDateTime.now(zoneId));

				if (sunRiseSetObjList.isEmpty()) {
					//error
					binding.lineChart.clearValues();
				} else {
					//차트 설정
					List<Entry> sunRiseTimeDataList = new ArrayList<>();
					List<Entry> sunSetTimeDataList = new ArrayList<>();
					List<String> dateListForAxis = new ArrayList<>();
					DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M.d E");

					int index = 0;
					int minutes;

					for (SunRiseSetUtil.SunRiseSetObj sunRiseSetObj : sunRiseSetObjList) {
						dateListForAxis.add(sunRiseSetObj.getZonedDateTime().format(dateFormatter));

						minutes = 60 * sunRiseSetObj.getSunrise().get(Calendar.HOUR_OF_DAY) +
								sunRiseSetObj.getSunrise().get(Calendar.MINUTE);
						sunRiseTimeDataList.add(new Entry(index, minutes, sunRiseSetObj));

						minutes = 60 * sunRiseSetObj.getSunset().get(Calendar.HOUR_OF_DAY) +
								sunRiseSetObj.getSunset().get(Calendar.MINUTE);
						sunSetTimeDataList.add(new Entry(index, minutes, sunRiseSetObj));
						index++;
					}

					LineDataSet sunRiseLineDataSet = new LineDataSet(sunRiseTimeDataList, "sunRise");
					LineDataSet sunSetLineDataSet = new LineDataSet(sunSetTimeDataList, "sunSet");
					sunRiseLineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
					sunSetLineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

					sunSetLineDataSet.setColor(Color.BLUE);
					sunRiseLineDataSet.setColor(Color.BLUE);

					sunSetLineDataSet.setDrawHorizontalHighlightIndicator(false);
					sunRiseLineDataSet.setDrawHorizontalHighlightIndicator(false);

					sunSetLineDataSet.setHighlightLineWidth(3f);
					sunRiseLineDataSet.setHighlightLineWidth(3f);

					sunSetLineDataSet.setHighLightColor(Color.GRAY);
					sunRiseLineDataSet.setHighLightColor(Color.GRAY);

					sunSetLineDataSet.setDrawCircleHole(false);
					sunRiseLineDataSet.setDrawCircleHole(false);

					sunSetLineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
					sunRiseLineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

					sunSetLineDataSet.setDrawCircles(false);
					sunRiseLineDataSet.setDrawCircles(false);

					sunSetLineDataSet.setDrawValues(false);
					sunRiseLineDataSet.setDrawValues(false);

					sunSetLineDataSet.setLineWidth(5f);
					sunRiseLineDataSet.setLineWidth(5f);

					sunSetLineDataSet.setFillColor(Color.WHITE);
					sunRiseLineDataSet.setFillColor(Color.WHITE);

					sunSetLineDataSet.setDrawFilled(true);
					sunRiseLineDataSet.setDrawFilled(true);

					sunSetLineDataSet.setFillAlpha(255);
					sunRiseLineDataSet.setFillAlpha(255);

					sunSetLineDataSet.setFillFormatter(new IFillFormatter() {
						@Override
						public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
							return binding.lineChart.getAxisLeft().getAxisMaximum();
						}
					});
					sunRiseLineDataSet.setFillFormatter(new IFillFormatter() {
						@Override
						public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
							return binding.lineChart.getAxisLeft().getAxisMinimum();
						}
					});

					List<ILineDataSet> dataSets = new ArrayList<>();
					dataSets.add(sunSetLineDataSet);
					dataSets.add(sunRiseLineDataSet);

					LineData lineData = new LineData(dataSets);
					lineData.setValueTextColor(Color.BLUE);

					binding.lineChart.setData(lineData);
					binding.lineChart.getXAxis().setValueFormatter(new XAxisDateFormatter(dateListForAxis));
					binding.lineChart.getAxisLeft().setValueFormatter(new YAxisTimeFormatter());
					binding.lineChart.getXAxis().setLabelRotationAngle(320f);
				}
				MainThreadWorker.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						binding.lineChart.invalidate();
						if (sunRiseSetObjList.size() > 0) {
							binding.goToToday.callOnClick();
						}
						binding.lineChart.zoom(1.2f, 0f, 1f, 1f);
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

	public class XAxisDateFormatter extends ValueFormatter {
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

	public class YAxisTimeFormatter extends ValueFormatter {
		final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
		LocalTime localTime = LocalTime.of(0, 0);

		@Override
		public String getAxisLabel(float value, AxisBase axis) {
			return localTime.plusMinutes((long) value).format(timeFormatter);
		}
	}
}