package com.lifedawn.bestweather.weathers.graphs;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointF;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.FragmentCubicLineChartBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class CubicLineChartFragment extends Fragment {
	private FragmentCubicLineChartBinding binding;
	private ArrayList<Integer> minTempList;
	private ArrayList<Integer> maxTempList;

	private List<Entry> minTempDataList;
	private List<Entry> maxTempDataList;
	private final LineDataSet.Mode MODE = LineDataSet.Mode.CUBIC_BEZIER;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getArguments();
		minTempList = bundle.getIntegerArrayList("minTempList");
		maxTempList = bundle.getIntegerArrayList("maxTempList");

		minTempDataList = new ArrayList<>();
		maxTempDataList = new ArrayList<>();

		for (int i = 0; i < minTempList.size(); i++) {
			minTempDataList.add(new Entry(i, minTempList.get(i)));
			maxTempDataList.add(new Entry(i, maxTempList.get(i)));
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentCubicLineChartBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		LineDataSet minTempLineDataSet = new LineDataSet(minTempDataList, "minTemp");
		LineDataSet maxTempLineDataSet = new LineDataSet(maxTempDataList, "maxTemp");
		minTempLineDataSet.setMode(MODE);
		maxTempLineDataSet.setMode(MODE);

		maxTempLineDataSet.setCircleColor(Color.RED);
		minTempLineDataSet.setCircleColor(Color.BLUE);

		maxTempLineDataSet.setColor(Color.WHITE);
		minTempLineDataSet.setColor(Color.WHITE);

		maxTempLineDataSet.setDrawCircleHole(false);
		minTempLineDataSet.setDrawCircleHole(false);

		maxTempLineDataSet.setCircleRadius(4f);
		minTempLineDataSet.setCircleRadius(4f);

		maxTempLineDataSet.setValueTextSize(13.5f);
		minTempLineDataSet.setValueTextSize(13.5f);

		maxTempLineDataSet.setLineWidth(1.2f);
		minTempLineDataSet.setLineWidth(1.2f);

		List<ILineDataSet> dataSets = new ArrayList<>();
		dataSets.add(minTempLineDataSet);
		dataSets.add(maxTempLineDataSet);

		LineData lineData = new LineData(dataSets);
		lineData.setValueFormatter(new TempValueFormatter());
		lineData.setValueTextColor(Color.WHITE);

		binding.cubicLineChart.setData(lineData);
		binding.cubicLineChart.setNoDataText(getString(R.string.noData));
		binding.cubicLineChart.setDrawGridBackground(false);
		binding.cubicLineChart.setDrawBorders(false);
		binding.cubicLineChart.setDoubleTapToZoomEnabled(false);
		binding.cubicLineChart.getLegend().setEnabled(false);

		binding.cubicLineChart.getAxisLeft().setDrawGridLines(false);
		binding.cubicLineChart.getAxisLeft().setDrawLabels(false);
		binding.cubicLineChart.getAxisLeft().setDrawAxisLine(false);

		binding.cubicLineChart.getAxisRight().setDrawGridLines(false);
		binding.cubicLineChart.getAxisRight().setDrawLabels(false);
		binding.cubicLineChart.getAxisRight().setDrawAxisLine(false);

		binding.cubicLineChart.getXAxis().setDrawGridLines(false);
		binding.cubicLineChart.getXAxis().setDrawLabels(false);
		binding.cubicLineChart.getXAxis().setDrawAxisLine(false);

		binding.cubicLineChart.setViewPortOffsets(0f, 0f, 0f, 0f);
		binding.cubicLineChart.getContentRect().left = 0;
		binding.cubicLineChart.getContentRect().right = binding.cubicLineChart.getWidth();

		binding.cubicLineChart.getDescription().setEnabled(false);
		binding.cubicLineChart.setTouchEnabled(false);
	}

	private static class TempValueFormatter extends ValueFormatter {
		final String degree = "°";

		@Override
		public String getFormattedValue(float value) {
			return super.getFormattedValue(value).replace(".0", "") + degree;
		}
	}
}