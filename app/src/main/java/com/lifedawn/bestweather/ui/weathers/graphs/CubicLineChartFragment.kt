package com.lifedawn.bestweather.ui.weathers.graphs

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.databinding.FragmentCubicLineChartBinding

class CubicLineChartFragment : Fragment() {
    private var binding: FragmentCubicLineChartBinding? = null
    private var minTempList: ArrayList<Int>? = null
    private var maxTempList: ArrayList<Int>? = null
    private var minTempDataList: MutableList<Entry>? = null
    private var maxTempDataList: MutableList<Entry>? = null
    private val MODE = LineDataSet.Mode.CUBIC_BEZIER
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        minTempList = bundle!!.getIntegerArrayList("minTempList")
        maxTempList = bundle.getIntegerArrayList("maxTempList")
        minTempDataList = ArrayList()
        maxTempDataList = ArrayList()
        for (i in minTempList!!.indices) {
            minTempDataList.add(Entry(i.toFloat(), minTempList!![i].toFloat()))
            maxTempDataList.add(Entry(i.toFloat(), maxTempList!![i].toFloat()))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCubicLineChartBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val minTempLineDataSet = LineDataSet(minTempDataList, "minTemp")
        val maxTempLineDataSet = LineDataSet(maxTempDataList, "maxTemp")
        minTempLineDataSet.mode = MODE
        maxTempLineDataSet.mode = MODE
        maxTempLineDataSet.setCircleColor(Color.RED)
        minTempLineDataSet.setCircleColor(Color.BLUE)
        maxTempLineDataSet.color = Color.WHITE
        minTempLineDataSet.color = Color.WHITE
        maxTempLineDataSet.setDrawCircleHole(false)
        minTempLineDataSet.setDrawCircleHole(false)
        maxTempLineDataSet.circleRadius = 4f
        minTempLineDataSet.circleRadius = 4f
        maxTempLineDataSet.valueTextSize = 13.5f
        minTempLineDataSet.valueTextSize = 13.5f
        maxTempLineDataSet.lineWidth = 1.2f
        minTempLineDataSet.lineWidth = 1.2f
        val dataSets: MutableList<ILineDataSet> = ArrayList()
        dataSets.add(minTempLineDataSet)
        dataSets.add(maxTempLineDataSet)
        val lineData = LineData(dataSets)
        lineData.setValueFormatter(TempValueFormatter())
        lineData.setValueTextColor(Color.WHITE)
        binding!!.cubicLineChart.data = lineData
        binding!!.cubicLineChart.setNoDataText(getString(R.string.noData))
        binding!!.cubicLineChart.setDrawGridBackground(false)
        binding!!.cubicLineChart.setDrawBorders(false)
        binding!!.cubicLineChart.isDoubleTapToZoomEnabled = false
        binding!!.cubicLineChart.legend.isEnabled = false
        binding!!.cubicLineChart.axisLeft.setDrawGridLines(false)
        binding!!.cubicLineChart.axisLeft.setDrawLabels(false)
        binding!!.cubicLineChart.axisLeft.setDrawAxisLine(false)
        binding!!.cubicLineChart.axisRight.setDrawGridLines(false)
        binding!!.cubicLineChart.axisRight.setDrawLabels(false)
        binding!!.cubicLineChart.axisRight.setDrawAxisLine(false)
        binding!!.cubicLineChart.xAxis.setDrawGridLines(false)
        binding!!.cubicLineChart.xAxis.setDrawLabels(false)
        binding!!.cubicLineChart.xAxis.setDrawAxisLine(false)
        binding!!.cubicLineChart.setViewPortOffsets(0f, 0f, 0f, 0f)
        binding!!.cubicLineChart.contentRect.left = 0f
        binding!!.cubicLineChart.contentRect.right = binding!!.cubicLineChart.width.toFloat()
        binding!!.cubicLineChart.description.isEnabled = false
        binding!!.cubicLineChart.setTouchEnabled(false)
    }

    private class TempValueFormatter : ValueFormatter() {
        val degree = "°"
        override fun getFormattedValue(value: Float): String {
            return super.getFormattedValue(value).replace(".0", "") + degree
        }
    }
}