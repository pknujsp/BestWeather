package com.lifedawn.bestweather.ui.weathers.detailfragment.aqicn;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.constants.BundleKey;
import com.lifedawn.bestweather.commons.constants.ValueUnits;
import com.lifedawn.bestweather.databinding.FragmentAirQualityDetailBinding;
import com.lifedawn.bestweather.data.MyApplication;
import com.lifedawn.bestweather.data.remote.retrofit.responses.aqicn.AqiCnGeolocalizedFeedResponse;
import com.lifedawn.bestweather.ui.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.ui.weathers.dataprocessing.util.LocationDistance;
import com.lifedawn.bestweather.ui.weathers.simplefragment.aqicn.AirQualityForecastObj;
import com.lifedawn.bestweather.ui.weathers.simplefragment.interfaces.IWeatherValues;
import com.lifedawn.bestweather.ui.weathers.view.TextsView;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetailAirQualityFragment extends Fragment implements IWeatherValues {
	private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("E");

	private static AqiCnGeolocalizedFeedResponse response;
	private FragmentAirQualityDetailBinding binding;
	private ValueUnits clockUnit;
	private ZoneId zoneId;

	private Double latitude;
	private Double longitude;
	private Bundle bundle;

	private int pm10LineColor;
	private int pm25LineColor;
	private int o3LineColor;

	public static void setResponse(AqiCnGeolocalizedFeedResponse response) {
		DetailAirQualityFragment.response = response;
	}

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		bundle = savedInstanceState != null ? savedInstanceState : getArguments();
		zoneId = (ZoneId) bundle.getSerializable(BundleKey.TimeZone.name());
		latitude = bundle.getDouble(BundleKey.Latitude.name());
		longitude = bundle.getDouble(BundleKey.Longitude.name());

		clockUnit = MyApplication.VALUE_UNIT_OBJ.getClockUnit();
		Context context = requireContext().getApplicationContext();

		pm10LineColor = ContextCompat.getColor(context, R.color.pm10LineColor);
		pm25LineColor = ContextCompat.getColor(context, R.color.pm25LineColor);
		o3LineColor = ContextCompat.getColor(context, R.color.o3LineColor);
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	@Nullable
	@org.jetbrains.annotations.Nullable
	@Override
	public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
	                         @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		binding = FragmentAirQualityDetailBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@SuppressLint("MissingPermission")
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) binding.toolbar.getRoot().getLayoutParams();
		layoutParams.topMargin = MyApplication.getStatusBarHeight();
		binding.toolbar.getRoot().setLayoutParams(layoutParams);
		binding.toolbar.fragmentTitle.setText(R.string.detail_air_quality);

		binding.adViewBelowGrid.loadAd(new AdRequest.Builder().build());
		binding.adViewBelowGrid.setAdListener(new AdListener() {
			@Override
			public void onAdClosed() {
				super.onAdClosed();
				binding.adViewBelowGrid.loadAd(new AdRequest.Builder().build());
			}
		});

		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});

		binding.horizontalScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
			@Override
			public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
				binding.pm10NoScrollView.reDraw(scrollX);
				binding.pm25NoScrollView.reDraw(scrollX);
				binding.o3NoScrollView.reDraw(scrollX);
			}
		});

		AdRequest adRequest = new AdRequest.Builder().build();
		AdLoader adLoader = new AdLoader.Builder(requireContext().getApplicationContext(), getString(R.string.NATIVE_ADVANCE_unitId))
				.forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
					@Override
					public void onNativeAdLoaded(NativeAd nativeAd) {
						NativeTemplateStyle styles = new
								NativeTemplateStyle.Builder().withMainBackgroundColor(new ColorDrawable(Color.WHITE)).build();
						TemplateView template = binding.adView;
						template.setStyles(styles);
						template.setNativeAd(nativeAd);

						if (nativeAd.isCustomMuteThisAdEnabled()) {

						}
					}
				}).withNativeAdOptions(new NativeAdOptions.Builder().setRequestCustomMuteThisAd(true).build())
				.build();
		adLoader.loadAd(adRequest);

		initBarChart(binding.pm10Chart);
		initBarChart(binding.pm25Chart);
		initBarChart(binding.o3Chart);

		setAirPollutionMaterialsInfo();
		setAqiGradeInfo();
		setValuesToViews();
	}

	@Override
	public void setValuesToViews() {
		final AqiCnGeolocalizedFeedResponse.Data.IAqi iAqi = response.getData().getIaqi();
		MyApplication.getExecutorService().execute(new Runnable() {
			@Override
			public void run() {
				AirQualityForecastObj current = new AirQualityForecastObj(null);
				if (iAqi.getPm10() != null) {
					current.pm10 = (int) Double.parseDouble(iAqi.getPm10().getValue());
				}
				if (iAqi.getPm25() != null) {
					current.pm25 = (int) Double.parseDouble(iAqi.getPm25().getValue());
				}
				if (iAqi.getO3() != null) {
					current.o3 = (int) Double.parseDouble(iAqi.getO3().getValue());
				}

				List<AirQualityForecastObj> airQualityForecastObjList = new ArrayList<>();
				airQualityForecastObjList.add(current);
				airQualityForecastObjList.addAll(AqicnResponseProcessor.getAirQualityForecastObjList(response, zoneId));

				setDataForBarChart(airQualityForecastObjList);
			}
		});

		int overallGrade = 0;
		try {
			overallGrade = (int) Double.parseDouble(response.getData().getAqi() == null ? "-1" : response.getData().getAqi());
		} catch (NumberFormatException e) {
			overallGrade = -1;
		}

		final String currentOverallDescription =
				AqicnResponseProcessor.getGradeDescription(overallGrade);
		final int currentOverallColor = AqicnResponseProcessor.getGradeColorId(overallGrade);

		binding.currentAirquality.setText(overallGrade == -1 ? getString(R.string.noData) : currentOverallDescription);
		binding.currentAirquality.setTextColor(currentOverallColor);

		String notData = getString(R.string.noData);
		if (iAqi.getCo() == null) {
			addGridItem(null, R.string.co_str, R.drawable.co);
		} else {
			Integer co = (int) Double.parseDouble(iAqi.getCo().getValue());
			addGridItem(co, R.string.co_str, R.drawable.co);
		}

		if (iAqi.getSo2() == null) {
			addGridItem(null, R.string.so2_str, R.drawable.so2);
		} else {
			Integer so2 = (int) Double.parseDouble(iAqi.getSo2().getValue());
			addGridItem(so2, R.string.so2_str, R.drawable.so2);
		}

		if (iAqi.getNo2() == null) {
			addGridItem(null, R.string.no2_str, R.drawable.no2);
		} else {
			Integer no2 = (int) Double.parseDouble(iAqi.getNo2().getValue());
			addGridItem(no2, R.string.no2_str, R.drawable.no2);
		}

		if (response.getData().getCity().getName() != null) {
			binding.measuringStationName.setText(response.getData().getCity().getName());
		} else {
			binding.measuringStationName.setText(notData);
		}
		if (response.getData().getTime().getIso() != null) {
			// time : 2021-10-22T11:16:41+09:00
			ZonedDateTime syncDateTime = null;
			try {
				syncDateTime = ZonedDateTime.parse(response.getData().getTime().getIso());
			} catch (Exception e) {

			}
			DateTimeFormatter syncDateTimeFormatter = DateTimeFormatter.ofPattern(clockUnit == ValueUnits.clock12 ?
							getString(R.string.datetime_pattern_clock12) : getString(R.string.datetime_pattern_clock24),
					Locale.getDefault());
			binding.updatedTime.setText(syncDateTime.format(syncDateTimeFormatter));
		} else {
			binding.updatedTime.setText(notData);
		}

		final Double distance = LocationDistance.distance(latitude, longitude,
				Double.parseDouble(response.getData().getCity().getGeo().get(0)),
				Double.parseDouble(response.getData().getCity().getGeo().get(1)), LocationDistance.Unit.KM);
		binding.distanceToMeasuringStation.setText(String.format("%.2f km", distance));
	}

	private void syncCharts(Chart mainChart, BarChart... otherCharts) {
		Matrix mainMatrix;
		float[] mainVals = new float[9];
		Matrix otherMatrix;
		float[] otherVals = new float[9];
		mainMatrix = mainChart.getViewPortHandler().getMatrixTouch();
		mainMatrix.getValues(mainVals);

		for (BarChart chart : otherCharts) {

			otherMatrix = chart.getViewPortHandler().getMatrixTouch();
			otherMatrix.getValues(otherVals);
			otherVals[Matrix.MSCALE_X] = mainVals[Matrix.MSCALE_X];
			otherVals[Matrix.MTRANS_X] = mainVals[Matrix.MTRANS_X];
			otherVals[Matrix.MSKEW_X] = mainVals[Matrix.MSKEW_X];
			otherMatrix.setValues(otherVals);
			chart.getViewPortHandler().refresh(otherMatrix, chart, true);

		}
	}

	private void setBarChartSize(BarChart barChart) {
		float originalTop = barChart.getViewPortHandler().offsetTop();
		float originalBottom = barChart.getViewPortHandler().offsetBottom();

		barChart.setViewPortOffsets(0, originalTop, 0, originalBottom + 10);
		barChart.getContentRect().left = 0;
		barChart.getContentRect().right = barChart.getWidth();
	}

	private void initBarChart(BarChart barChart) {
		barChart.setNoDataText(getString(R.string.noData));
		barChart.setNoDataTextColor(Color.BLUE);
		barChart.getAxisRight().setEnabled(false);

		barChart.setDrawBorders(false);
		barChart.setPinchZoom(false);
		barChart.getDescription().setEnabled(false);

		barChart.setTouchEnabled(false);
		barChart.setScaleEnabled(false);

		barChart.setBackgroundColor(Color.WHITE);
		barChart.setDrawGridBackground(false);
		barChart.getXAxis().setDrawGridLines(false);
		barChart.getXAxis().setGranularityEnabled(true);
		barChart.getXAxis().setGranularity(1f);
		barChart.getXAxis().setTextSize(13f);
		barChart.getXAxis().setCenterAxisLabels(false);
		barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
		barChart.getXAxis().setDrawLabels(true);

		barChart.getAxisLeft().setSpaceBottom(15);
		barChart.getAxisLeft().setSpaceTop(15);
		barChart.getAxisLeft().setDrawGridLines(false);
		barChart.getAxisLeft().setAxisMinimum(0);
		barChart.getAxisLeft().setTextSize(13f);
		barChart.getAxisLeft().setDrawLabels(false);
		barChart.getAxisLeft().setDrawAxisLine(false);

		barChart.setDrawValueAboveBar(true);
		barChart.getLegend().setEnabled(false);
		barChart.fitScreen();
	}


	private void initLineChart(LineChart lineChart) {
		lineChart.getAxisLeft().setTextSize(13f);
		lineChart.getAxisLeft().setAxisMinimum(-1f);
		lineChart.getAxisLeft().setSpaceBottom(15);
		lineChart.getAxisLeft().setSpaceTop(15);
		lineChart.getAxisLeft().setDrawGridLines(false);
		lineChart.getAxisLeft().setDrawGridLines(false);

		lineChart.setBackgroundColor(Color.WHITE);
		lineChart.setDrawGridBackground(false);
		lineChart.getXAxis().setDrawGridLines(false);
		lineChart.getXAxis().setGranularityEnabled(true);
		lineChart.getXAxis().setGranularity(1f);
		lineChart.getXAxis().setCenterAxisLabels(false);
		lineChart.getXAxis().setTextSize(14f);

		lineChart.getAxisRight().setEnabled(false);

		lineChart.setExtraTopOffset(10f);
		lineChart.setTouchEnabled(false);
		lineChart.setScaleYEnabled(false);
		lineChart.setScaleXEnabled(false);
		lineChart.getLegend().setTextSize(14f);
		lineChart.getLegend().setFormSize(12f);
		lineChart.setNoDataText(getString(R.string.noData));

		lineChart.getDescription().setEnabled(false);
		lineChart.setDrawBorders(true);
		lineChart.setPinchZoom(false);

		lineChart.fitScreen();
	}


	private void setDataForBarChart(List<AirQualityForecastObj> airQualityForecastObjList) {
		List<BarEntry> pm10EntryList = new ArrayList<>();
		List<BarEntry> pm25EntryList = new ArrayList<>();
		List<BarEntry> o3EntryList = new ArrayList<>();

		List<Integer> pm10ColorList = new ArrayList<>();
		List<Integer> pm25ColorList = new ArrayList<>();
		List<Integer> o3ColorList = new ArrayList<>();

		List<String> pm10GradeDescriptionList = new ArrayList<>();
		List<String> pm25GradeDescriptionList = new ArrayList<>();
		List<String> o3GradeDescriptionList = new ArrayList<>();

		BottomXAxisLabelFormatter pm10BottomXAxisLabelFormatter = new BottomXAxisLabelFormatter(pm10GradeDescriptionList);
		BottomXAxisLabelFormatter pm25BottomXAxisLabelFormatter = new BottomXAxisLabelFormatter(pm25GradeDescriptionList);
		BottomXAxisLabelFormatter o3BottomXAxisLabelFormatter = new BottomXAxisLabelFormatter(o3GradeDescriptionList);

		List<String> dateList = new ArrayList<>();

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M.d\nE");

		for (int i = 0; i < airQualityForecastObjList.size(); i++) {
			AirQualityForecastObj airQualityForecastObj = airQualityForecastObjList.get(i);
			dateList.add(airQualityForecastObj.date == null ? getString(R.string.current) :
					airQualityForecastObj.date.format(dateTimeFormatter));

			int pm10 = airQualityForecastObj.pm10 == null ? 0 : airQualityForecastObj.pm10;
			int pm25 = airQualityForecastObj.pm25 == null ? 0 : airQualityForecastObj.pm25;
			int o3 = airQualityForecastObj.o3 == null ? 0 : airQualityForecastObj.o3;

			pm10EntryList.add(new BarEntry(i, pm10, airQualityForecastObj));
			pm25EntryList.add(new BarEntry(i, pm25, airQualityForecastObj));
			o3EntryList.add(new BarEntry(i, o3, airQualityForecastObj));

			pm10ColorList.add(AqicnResponseProcessor.getGradeColorId(pm10));
			pm25ColorList.add(AqicnResponseProcessor.getGradeColorId(pm25));
			o3ColorList.add(AqicnResponseProcessor.getGradeColorId(o3));

			if (airQualityForecastObj.pm10 == null) {
				pm10 = -1;
			}
			if (airQualityForecastObj.pm25 == null) {
				pm25 = -1;
			}
			if (airQualityForecastObj.o3 == null) {
				o3 = -1;
			}

			pm10GradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(pm10));
			pm25GradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(pm25));
			o3GradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(o3));
		}

		BarDataSet pm10BarDataSet = new BarDataSet(pm10EntryList, "pm10");
		BarDataSet pm25BarDataSet = new BarDataSet(pm25EntryList, "pm25");
		BarDataSet o3BarDataSet = new BarDataSet(o3EntryList, "o3");

		pm10BarDataSet.setColors(pm10ColorList);
		pm25BarDataSet.setColors(pm25ColorList);
		o3BarDataSet.setColors(o3ColorList);

		List<IBarDataSet> pm10DataSet = new ArrayList<>();
		pm10DataSet.add(pm10BarDataSet);

		List<IBarDataSet> pm25DataSet = new ArrayList<>();
		pm25DataSet.add(pm25BarDataSet);

		List<IBarDataSet> o3DataSet = new ArrayList<>();
		o3DataSet.add(o3BarDataSet);

		BarData pm10BarData = new BarData(pm10DataSet);
		BarData pm25BarData = new BarData(pm25DataSet);
		BarData o3BarData = new BarData(o3DataSet);

		pm10BarData.setHighlightEnabled(false);
		pm25BarData.setHighlightEnabled(false);
		o3BarData.setHighlightEnabled(false);

		pm10BarData.setValueTextSize(15f);
		pm25BarData.setValueTextSize(15f);
		o3BarData.setValueTextSize(15f);

		pm10BarData.setValueFormatter(new GradeValueFormatter());
		pm25BarData.setValueFormatter(new GradeValueFormatter());
		o3BarData.setValueFormatter(new GradeValueFormatter());

		binding.pm10Chart.setData(pm10BarData);
		binding.pm10Chart.getXAxis().setValueFormatter(pm10BottomXAxisLabelFormatter);

		binding.pm25Chart.setData(pm25BarData);
		binding.pm25Chart.getXAxis().setValueFormatter(pm25BottomXAxisLabelFormatter);

		binding.o3Chart.setData(o3BarData);
		binding.o3Chart.getXAxis().setValueFormatter(o3BottomXAxisLabelFormatter);

		MainThreadWorker.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final int cellWidth = (int) getResources().getDimension(R.dimen.minColumnWidthInAirQualityBarView);
				final int viewWidth = cellWidth * dateList.size();
				TextsView dateView = new TextsView(requireContext().getApplicationContext(), viewWidth, cellWidth, dateList);
				dateView.setValueTextSize(14);
				dateView.setValueTextColor(Color.BLACK);

				binding.dateRow.removeAllViews();
				binding.dateRow.addView(dateView);

				setBarChartSize(binding.pm10Chart);
				setBarChartSize(binding.pm25Chart);
				setBarChartSize(binding.o3Chart);

				binding.pm10Chart.invalidate();
				binding.pm25Chart.invalidate();
				binding.o3Chart.invalidate();
			}
		});
	}


	private void setDataForLineChart(List<AirQualityForecastObj> airQualityForecastObjList) {
		List<Entry> pm10EntryList = new ArrayList<>();
		List<Entry> pm25EntryList = new ArrayList<>();
		List<Entry> o3EntryList = new ArrayList<>();

		List<Integer> pm10ColorList = new ArrayList<>();
		List<Integer> pm25ColorList = new ArrayList<>();
		List<Integer> o3ColorList = new ArrayList<>();

		List<String> pm10GradeDescriptionList = new ArrayList<>();
		List<String> pm25GradeDescriptionList = new ArrayList<>();
		List<String> o3GradeDescriptionList = new ArrayList<>();
		ArrayMap<Integer, String> gradeDescriptionMap = new ArrayMap<>();

		List<String> dateList = new ArrayList<>();

		String pm10Description = null;
		String pm25Description = null;
		String o3Description = null;

		BottomXAxisLabelFormatter bottomXAxisLabelFormatter = new BottomXAxisLabelFormatter(dateList);

		for (int i = 0; i < airQualityForecastObjList.size(); i++) {
			AirQualityForecastObj airQualityForecastObj = airQualityForecastObjList.get(i);
			dateList.add(airQualityForecastObj.date == null ? getString(R.string.current) :
					airQualityForecastObj.date.format(DATE_FORMATTER));

			int pm10 = airQualityForecastObj.pm10 == null ? -1 : airQualityForecastObj.pm10;
			int pm25 = airQualityForecastObj.pm25 == null ? -1 : airQualityForecastObj.pm25;
			int o3 = airQualityForecastObj.o3 == null ? -1 : airQualityForecastObj.o3;

			pm10EntryList.add(new Entry(i, pm10, airQualityForecastObj));
			pm25EntryList.add(new Entry(i, pm25, airQualityForecastObj));
			o3EntryList.add(new Entry(i, o3, airQualityForecastObj));

			pm10ColorList.add(AqicnResponseProcessor.getGradeColorId(pm10));
			pm25ColorList.add(AqicnResponseProcessor.getGradeColorId(pm25));
			o3ColorList.add(AqicnResponseProcessor.getGradeColorId(o3));

			pm10Description = AqicnResponseProcessor.getGradeDescription(pm10);
			pm25Description = AqicnResponseProcessor.getGradeDescription(pm25);
			o3Description = AqicnResponseProcessor.getGradeDescription(o3);

			pm10GradeDescriptionList.add(pm10Description);
			pm25GradeDescriptionList.add(pm25Description);
			o3GradeDescriptionList.add(o3Description);

			gradeDescriptionMap.put(pm10, pm10Description);
			gradeDescriptionMap.put(pm25, pm25Description);
			gradeDescriptionMap.put(o3, o3Description);
		}

		LineDataSet pm10LineDataSet = new LineDataSet(pm10EntryList, "pm10");
		LineDataSet pm25LineDataSet = new LineDataSet(pm25EntryList, "pm25");
		LineDataSet o3LineDataSet = new LineDataSet(o3EntryList, "o3");

		setCommonLineDataSet(pm10LineDataSet, pm10ColorList);
		setCommonLineDataSet(pm25LineDataSet, pm25ColorList);
		setCommonLineDataSet(o3LineDataSet, o3ColorList);

		pm10LineDataSet.setColor(pm10LineColor);
		pm25LineDataSet.setColor(pm25LineColor);
		o3LineDataSet.setColor(o3LineColor);
/*
		pm10LineDataSet.setValueFormatter(new GradeValueFormatter(gradeDescriptionMap));
		pm25LineDataSet.setValueFormatter(new GradeValueFormatter(gradeDescriptionMap));
		o3LineDataSet.setValueFormatter(new GradeValueFormatter(gradeDescriptionMap));


 */
		List<ILineDataSet> dataSet = new ArrayList<>();
		dataSet.add(pm10LineDataSet);
		dataSet.add(pm25LineDataSet);
		dataSet.add(o3LineDataSet);

		LineData lineData = new LineData(dataSet);
		lineData.setHighlightEnabled(true);
		lineData.setDrawValues(true);

		//binding.pm10Chart.setData(lineData);
		binding.pm10Chart.getXAxis().setValueFormatter(bottomXAxisLabelFormatter);
		binding.pm10Chart.getXAxis().setAxisMinimum(-1);
		binding.pm10Chart.getXAxis().setAxisMaximum(lineData.getXMax() + 1);
		binding.pm10Chart.getXAxis().setLabelCount(dateList.size());

		MainThreadWorker.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				binding.pm10Chart.invalidate();
			}
		});
	}

	private void setCommonLineDataSet(LineDataSet lineDataSet, List<Integer> circleColors) {
		lineDataSet.setCircleRadius(5f);
		lineDataSet.setHighLightColor(Color.GRAY);
		lineDataSet.setDrawCircleHole(false);
		lineDataSet.setHighlightLineWidth(2f);
		lineDataSet.setValueTextSize(14f);
		lineDataSet.setDrawHorizontalHighlightIndicator(false);
		lineDataSet.setLineWidth(3f);

		lineDataSet.setCircleColors(circleColors);
		lineDataSet.setDrawValues(true);
		lineDataSet.setValueTextColors(circleColors);
		lineDataSet.setHighlightEnabled(true);
		lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
	}

	private View addGridItem(@Nullable Integer value, int labelDescriptionId, @NonNull Integer labelIconId) {
		View gridItem = getLayoutInflater().inflate(R.layout.air_quality_item, null);
		((ImageView) gridItem.findViewById(R.id.label_icon)).setImageResource(labelIconId);
		((TextView) gridItem.findViewById(R.id.label)).setText(labelDescriptionId);
		((TextView) gridItem.findViewById(R.id.label)).setTextColor(Color.BLACK);
		((TextView) gridItem.findViewById(R.id.value_int)).setText(value == null ? "?" : value.toString());
		((TextView) gridItem.findViewById(R.id.value_int)).setTextColor(Color.BLACK);
		((TextView) gridItem.findViewById(R.id.value_str)).setText(value == null ? getString(R.string.noData) : AqicnResponseProcessor.getGradeDescription(value));
		((TextView) gridItem.findViewById(R.id.value_str)).setTextColor(value == null ? ContextCompat.getColor(requireContext().getApplicationContext(), R.color.not_data_color)
				: AqicnResponseProcessor.getGradeColorId(value));

		int cellCount = binding.grid.getChildCount();
		int row = cellCount / binding.grid.getColumnCount();
		int column = cellCount % binding.grid.getColumnCount();

		GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();

		layoutParams.columnSpec = GridLayout.spec(column, GridLayout.FILL, 1);
		layoutParams.rowSpec = GridLayout.spec(row, GridLayout.FILL, 1);

		binding.grid.addView(gridItem, layoutParams);
		return gridItem;
	}

	private void setAirPollutionMaterialsInfo() {
		int[] icons = new int[]{R.drawable.pm10, R.drawable.pm25, R.drawable.co, R.drawable.no2, R.drawable.so2,
				R.drawable.o3};
		String[] names = new String[]{getString(R.string.pm10_str), getString(R.string.pm25_str), getString(R.string.co_str)
				, getString(R.string.no2_str), getString(R.string.so2_str), getString(R.string.o3_str)};
		String[] descriptions = new String[]{getString(R.string.pm10_description), getString(R.string.pm25_description), getString(R.string.co_description)
				, getString(R.string.no2_description), getString(R.string.so2_description), getString(R.string.o3_description)};

		View infoItem = null;
		binding.airPollutionMaterialsInfo.removeAllViews();
		LayoutInflater layoutInflater = getLayoutInflater();

		for (int i = 0; i < icons.length; i++) {
			infoItem = layoutInflater.inflate(R.layout.air_pollution_material_info_item_view, null);
			((ImageView) infoItem.findViewById(R.id.material_icon)).setImageDrawable(ContextCompat.getDrawable(requireContext().getApplicationContext(), icons[i]));
			((TextView) infoItem.findViewById(R.id.material_name)).setText(names[i]);
			((TextView) infoItem.findViewById(R.id.material_description)).setText(descriptions[i]);

			binding.airPollutionMaterialsInfo.addView(infoItem);
		}
	}

	private void setAqiGradeInfo() {
		int[] gradeColors = getResources().getIntArray(R.array.AqiGradeColors);
		String[] gradeRanges = getResources().getStringArray(R.array.AqiGradeRange);
		String[] description = getResources().getStringArray(R.array.AqiGradeState);

		View infoItem = null;
		binding.aqiGradeInfo.removeAllViews();
		LayoutInflater layoutInflater = getLayoutInflater();

		for (int i = 0; i < gradeColors.length; i++) {
			infoItem = layoutInflater.inflate(R.layout.aqi_grade_info_view, null);
			((View) infoItem.findViewById(R.id.grade_color)).setBackgroundTintList(ColorStateList.valueOf(gradeColors[i]));
			((TextView) infoItem.findViewById(R.id.grade_range)).setText(gradeRanges[i]);
			((TextView) infoItem.findViewById(R.id.grade_state)).setText(description[i]);

			binding.aqiGradeInfo.addView(infoItem);
		}
	}


	protected static class BottomXAxisLabelFormatter extends ValueFormatter {
		List<String> gradeDescriptionList;
		int val;

		public BottomXAxisLabelFormatter(List<String> gradeDescriptionList) {
			this.gradeDescriptionList = gradeDescriptionList;
		}


		@Override
		public String getAxisLabel(float value, AxisBase axis) {
			val = (int) value;
			return val < gradeDescriptionList.size() && val >= 0 ? gradeDescriptionList.get(val) : "";
		}
	}

	protected static class GradeValueFormatter extends ValueFormatter {

		@Override
		public String getFormattedValue(float value) {
			return String.valueOf((int) value);
		}

	}

	protected static class BothAxisLabelRenderer extends XAxisRenderer {
		private final ValueFormatter topValueFormatter;

		public BothAxisLabelRenderer(ViewPortHandler viewPortHandler, XAxis xAxis, Transformer transformer, ValueFormatter topValueFormatter) {
			super(viewPortHandler, xAxis, transformer);
			this.topValueFormatter = topValueFormatter;
		}

		@Override
		public void renderAxisLabels(Canvas c) {
			if (!mXAxis.isEnabled() || !mXAxis.isDrawLabelsEnabled())
				return;

			float yOffset = mXAxis.getYOffset();

			mAxisLabelPaint.setTypeface(mXAxis.getTypeface());
			mAxisLabelPaint.setTextSize(mXAxis.getTextSize());
			mAxisLabelPaint.setColor(mXAxis.getTextColor());

			MPPointF pointF = MPPointF.getInstance(0, 0);
			pointF.x = 0.5f;
			pointF.y = 1.0f;

			drawLabelsTop(c, mViewPortHandler.contentTop() - yOffset, pointF);
			pointF.x = 0.5f;
			pointF.y = 0.0f;
			drawLabels(c, mViewPortHandler.contentBottom() + yOffset, pointF);
			MPPointF.recycleInstance(pointF);
		}

		private void drawLabelsTop(Canvas canvas, float y, MPPointF anchor) {
			final float labelRotationAngleDegrees = mXAxis.getLabelRotationAngle();
			boolean centeringEnabled = mXAxis.isCenterAxisLabelsEnabled();

			float[] positions = new float[mXAxis.mEntryCount * 2];

			for (int i = 0; i < positions.length; i += 2) {

				// only fill x values
				if (centeringEnabled) {
					positions[i] = mXAxis.mCenteredEntries[i / 2];
				} else {
					positions[i] = mXAxis.mEntries[i / 2];
				}
			}

			mTrans.pointValuesToPixel(positions);

			for (int i = 0; i < positions.length; i += 2) {
				float x = positions[i];
				if (mViewPortHandler.isInBoundsX(x)) {
					String label = topValueFormatter.getAxisLabel(mXAxis.mEntries[i / 2], mXAxis);

					if (mXAxis.isAvoidFirstLastClippingEnabled()) {

						// avoid clipping of the last
						if (i == mXAxis.mEntryCount - 1 && mXAxis.mEntryCount > 1) {
							float width = Utils.calcTextWidth(mAxisLabelPaint, label);
							if (width > mViewPortHandler.offsetRight() * 2
									&& x + width > mViewPortHandler.getChartWidth())
								x -= width / 2;

							// avoid clipping of the first
						} else if (i == 0) {

							float width = Utils.calcTextWidth(mAxisLabelPaint, label);
							x += width / 2;
						}
					}

					drawLabel(canvas, label, x, y, anchor, labelRotationAngleDegrees);
				}
			}
		}
	}

	protected class ChartGestureListener implements OnChartGestureListener {
		final Chart mainChart;
		final BarChart[] otherChars;

		public ChartGestureListener(Chart mainChart, BarChart... otherChars) {
			this.mainChart = mainChart;
			this.otherChars = otherChars;
		}

		@Override
		public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
			syncCharts(mainChart, otherChars);
		}

		@Override
		public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
			//binding.pm25Chart.getOnChartGestureListener().onChartGestureEnd(me, lastPerformedGesture);
			syncCharts(mainChart, otherChars);

		}

		@Override
		public void onChartLongPressed(MotionEvent me) {

		}

		@Override
		public void onChartDoubleTapped(MotionEvent me) {

		}

		@Override
		public void onChartSingleTapped(MotionEvent me) {

		}

		@Override
		public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
			syncCharts(mainChart, otherChars);

		}

		@Override
		public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

		}

		@Override
		public void onChartTranslate(MotionEvent me, float dX, float dY) {

		}
	}
}