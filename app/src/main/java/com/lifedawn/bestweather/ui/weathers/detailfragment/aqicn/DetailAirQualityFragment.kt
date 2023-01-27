package com.lifedawn.bestweather.ui.weathers.detailfragment.aqicn

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.ArrayMap
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.gridlayout.widget.GridLayout
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.Utils
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.views.BaseFragment
import com.lifedawn.bestweather.databinding.FragmentAirQualityDetailBinding
import com.lifedawn.bestweather.ui.weathers.simplefragment.aqicn.AirQualityForecastObj
import com.lifedawn.bestweather.ui.weathers.simplefragment.interfaces.IWeatherValues
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class DetailAirQualityFragment : BaseFragment<FragmentAirQualityDetailBinding>(R.layout.fragment_air_quality_detail), IWeatherValues {
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("E")
    private var clockUnit: ValueUnits? = null
    private var zoneId: ZoneId? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var bundle: Bundle? = null
    private var pm10LineColor = 0
    private var pm25LineColor = 0
    private var o3LineColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = savedInstanceState ?: arguments
        zoneId = bundle.getSerializable(BundleKey.TimeZone.name) as ZoneId?
        latitude = bundle.getDouble(BundleKey.Latitude.name)
        longitude = bundle.getDouble(BundleKey.Longitude.name)
        clockUnit = MyApplication.VALUE_UNIT_OBJ.getClockUnit()
        val context = requireContext().applicationContext
        pm10LineColor = ContextCompat.getColor(context, R.color.pm10LineColor)
        pm25LineColor = ContextCompat.getColor(context, R.color.pm25LineColor)
        o3LineColor = ContextCompat.getColor(context, R.color.o3LineColor)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(bundle)
    }


    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutParams = binding.toolbar.root.layoutParams as RelativeLayout.LayoutParams
        layoutParams.topMargin = MyApplication.getStatusBarHeight()
        binding.toolbar.root.layoutParams = layoutParams
        binding.toolbar.fragmentTitle.setText(R.string.detail_air_quality)
        binding.adViewBelowGrid.loadAd(AdRequest.Builder().build())
        binding.adViewBelowGrid.adListener = object : AdListener() {
            override fun onAdClosed() {
                super.onAdClosed()
                binding.adViewBelowGrid.loadAd(AdRequest.Builder().build())
            }
        }
        binding.toolbar.backBtn.setOnClickListener { parentFragmentManager.popBackStackImmediate() }
        binding.horizontalScrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            binding.pm10NoScrollView.reDraw(scrollX)
            binding.pm25NoScrollView.reDraw(scrollX)
            binding.o3NoScrollView.reDraw(scrollX)
        }
        val adRequest = AdRequest.Builder().build()
        val adLoader = AdLoader.Builder(requireContext().applicationContext, getString(R.string.NATIVE_ADVANCE_unitId))
            .forNativeAd { nativeAd ->
                val styles: NativeTemplateStyle = NativeTemplateStyle.Builder().withMainBackgroundColor(ColorDrawable(Color.WHITE)).build()
                val template: TemplateView = binding.adView
                template.setStyles(styles)
                template.setNativeAd(nativeAd)
                if (nativeAd.isCustomMuteThisAdEnabled) {
                }
            }.withNativeAdOptions(NativeAdOptions.Builder().setRequestCustomMuteThisAd(true).build())
            .build()

        adLoader.loadAd(adRequest)
        initBarChart(binding.pm10Chart)
        initBarChart(binding.pm25Chart)
        initBarChart(binding.o3Chart)
        setAirPollutionMaterialsInfo()
        setAqiGradeInfo()
        setValuesToViews()
    }

    override fun setValuesToViews() {
        val iAqi: IAqi = response.data.iaqi
        MyApplication.getExecutorService().execute(Runnable {
            val current = AirQualityForecastObj(null)
            if (iAqi.pm10 != null) {
                current.pm10 = iAqi.pm10.value.toDouble()
            }
            if (iAqi.pm25 != null) {
                current.pm25 = iAqi.pm25.value.toDouble()
            }
            if (iAqi.o3 != null) {
                current.o3 = iAqi.o3.value.toDouble()
            }
            val airQualityForecastObjList: MutableList<AirQualityForecastObj> = ArrayList<AirQualityForecastObj>()
            airQualityForecastObjList.add(current)
            airQualityForecastObjList.addAll(AqicnResponseProcessor.getAirQualityDailyForecastList(response, zoneId))
            setDataForBarChart(airQualityForecastObjList)
        })
        var overallGrade = 0
        overallGrade = try {
            if (response.data.aqi == null) "-1" else response.data.aqi.toDouble()
        } catch (e: NumberFormatException) {
            -1
        }
        val currentOverallDescription: String = AqicnResponseProcessor.getGradeDescription(overallGrade)
        val currentOverallColor: Int = AqicnResponseProcessor.getGradeColorId(overallGrade)
        binding.currentAirquality.text = if (overallGrade == -1) getString(R.string.noData) else currentOverallDescription
        binding.currentAirquality.setTextColor(currentOverallColor)
        val notData = getString(R.string.noData)
        if (iAqi.co == null) {
            addGridItem(null, R.string.co_str, R.drawable.co)
        } else {
            val co = iAqi.co.value.toDouble() as Int
            addGridItem(co, R.string.co_str, R.drawable.co)
        }
        if (iAqi.so2 == null) {
            addGridItem(null, R.string.so2_str, R.drawable.so2)
        } else {
            val so2 = iAqi.so2.value.toDouble() as Int
            addGridItem(so2, R.string.so2_str, R.drawable.so2)
        }
        if (iAqi.no2 == null) {
            addGridItem(null, R.string.no2_str, R.drawable.no2)
        } else {
            val no2 = iAqi.no2.value.toDouble() as Int
            addGridItem(no2, R.string.no2_str, R.drawable.no2)
        }
        if (response.data.city.name != null) {
            binding.measuringStationName.setText(response.data.city.name)
        } else {
            binding.measuringStationName.text = notData
        }
        if (response.data.time.iso != null) {
            // time : 2021-10-22T11:16:41+09:00
            var syncDateTime: ZonedDateTime? = null
            try {
                syncDateTime = ZonedDateTime.parse(response.data.time.iso)
            } catch (e: Exception) {
            }
            val syncDateTimeFormatter = DateTimeFormatter.ofPattern(
                if (clockUnit === ValueUnits.clock12) getString(R.string.datetime_pattern_clock12) else getString(
                    R.string.datetime_pattern_clock24
                ),
                Locale.getDefault()
            )
            binding.updatedTime.text = syncDateTime.format(syncDateTimeFormatter)
        } else {
            binding.updatedTime.text = notData
        }
        val distance: Double = LocationDistance.distance(
            latitude,
            longitude,
            response.data.city.geo.get(0).toDouble(),
            response.data.city.geo.get(1).toDouble(),
            LocationDistance.Unit.KM
        )
        binding.distanceToMeasuringStation.text = String.format("%.2f km", distance)
    }

    private fun syncCharts(mainChart: Chart<*>, vararg otherCharts: BarChart) {
        val mainMatrix: Matrix
        val mainVals = FloatArray(9)
        var otherMatrix: Matrix
        val otherVals = FloatArray(9)
        mainMatrix = mainChart.viewPortHandler.matrixTouch
        mainMatrix.getValues(mainVals)
        for (chart in otherCharts) {
            otherMatrix = chart.viewPortHandler.matrixTouch
            otherMatrix.getValues(otherVals)
            otherVals[Matrix.MSCALE_X] = mainVals[Matrix.MSCALE_X]
            otherVals[Matrix.MTRANS_X] = mainVals[Matrix.MTRANS_X]
            otherVals[Matrix.MSKEW_X] = mainVals[Matrix.MSKEW_X]
            otherMatrix.setValues(otherVals)
            chart.viewPortHandler.refresh(otherMatrix, chart, true)
        }
    }

    private fun setBarChartSize(barChart: BarChart) {
        val originalTop = barChart.viewPortHandler.offsetTop()
        val originalBottom = barChart.viewPortHandler.offsetBottom()
        barChart.setViewPortOffsets(0f, originalTop, 0f, originalBottom + 10)
        barChart.contentRect.left = 0f
        barChart.contentRect.right = barChart.width.toFloat()
    }

    private fun initBarChart(barChart: BarChart) {
        barChart.setNoDataText(getString(R.string.noData))
        barChart.setNoDataTextColor(Color.BLUE)
        barChart.axisRight.isEnabled = false
        barChart.setDrawBorders(false)
        barChart.setPinchZoom(false)
        barChart.description.isEnabled = false
        barChart.setTouchEnabled(false)
        barChart.setScaleEnabled(false)
        barChart.setBackgroundColor(Color.WHITE)
        barChart.setDrawGridBackground(false)
        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.isGranularityEnabled = true
        barChart.xAxis.granularity = 1f
        barChart.xAxis.textSize = 13f
        barChart.xAxis.setCenterAxisLabels(false)
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.setDrawLabels(true)
        barChart.axisLeft.spaceBottom = 15f
        barChart.axisLeft.spaceTop = 15f
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisLeft.textSize = 13f
        barChart.axisLeft.setDrawLabels(false)
        barChart.axisLeft.setDrawAxisLine(false)
        barChart.setDrawValueAboveBar(true)
        barChart.legend.isEnabled = false
        barChart.fitScreen()
    }

    private fun initLineChart(lineChart: LineChart) {
        lineChart.axisLeft.textSize = 13f
        lineChart.axisLeft.axisMinimum = -1f
        lineChart.axisLeft.spaceBottom = 15f
        lineChart.axisLeft.spaceTop = 15f
        lineChart.axisLeft.setDrawGridLines(false)
        lineChart.axisLeft.setDrawGridLines(false)
        lineChart.setBackgroundColor(Color.WHITE)
        lineChart.setDrawGridBackground(false)
        lineChart.xAxis.setDrawGridLines(false)
        lineChart.xAxis.isGranularityEnabled = true
        lineChart.xAxis.granularity = 1f
        lineChart.xAxis.setCenterAxisLabels(false)
        lineChart.xAxis.textSize = 14f
        lineChart.axisRight.isEnabled = false
        lineChart.extraTopOffset = 10f
        lineChart.setTouchEnabled(false)
        lineChart.isScaleYEnabled = false
        lineChart.isScaleXEnabled = false
        lineChart.legend.textSize = 14f
        lineChart.legend.formSize = 12f
        lineChart.setNoDataText(getString(R.string.noData))
        lineChart.description.isEnabled = false
        lineChart.setDrawBorders(true)
        lineChart.setPinchZoom(false)
        lineChart.fitScreen()
    }

    private fun setDataForBarChart(airQualityForecastObjList: List<AirQualityForecastObj>) {
        val pm10EntryList: MutableList<BarEntry> = ArrayList<BarEntry>()
        val pm25EntryList: MutableList<BarEntry> = ArrayList<BarEntry>()
        val o3EntryList: MutableList<BarEntry> = ArrayList<BarEntry>()
        val pm10ColorList: MutableList<Int> = ArrayList()
        val pm25ColorList: MutableList<Int> = ArrayList()
        val o3ColorList: MutableList<Int> = ArrayList()
        val pm10GradeDescriptionList: MutableList<String> = ArrayList()
        val pm25GradeDescriptionList: MutableList<String> = ArrayList()
        val o3GradeDescriptionList: MutableList<String> = ArrayList()
        val pm10BottomXAxisLabelFormatter = BottomXAxisLabelFormatter(pm10GradeDescriptionList)
        val pm25BottomXAxisLabelFormatter = BottomXAxisLabelFormatter(pm25GradeDescriptionList)
        val o3BottomXAxisLabelFormatter = BottomXAxisLabelFormatter(o3GradeDescriptionList)
        val dateList: MutableList<String> = ArrayList()
        val dateTimeFormatter = DateTimeFormatter.ofPattern("M.d\nE")
        for (i in airQualityForecastObjList.indices) {
            val airQualityForecastObj: AirQualityForecastObj = airQualityForecastObjList[i]
            dateList.add(
                if (airQualityForecastObj.date == null) getString(R.string.current) else airQualityForecastObj.date.format(
                    dateTimeFormatter
                )
            )
            var pm10 = if (airQualityForecastObj.pm10 == null) 0 else airQualityForecastObj.pm10
            var pm25 = if (airQualityForecastObj.pm25 == null) 0 else airQualityForecastObj.pm25
            var o3 = if (airQualityForecastObj.o3 == null) 0 else airQualityForecastObj.o3
            pm10EntryList.add(BarEntry(i, pm10, airQualityForecastObj))
            pm25EntryList.add(BarEntry(i, pm25, airQualityForecastObj))
            o3EntryList.add(BarEntry(i, o3, airQualityForecastObj))
            pm10ColorList.add(AqicnResponseProcessor.getGradeColorId(pm10))
            pm25ColorList.add(AqicnResponseProcessor.getGradeColorId(pm25))
            o3ColorList.add(AqicnResponseProcessor.getGradeColorId(o3))
            if (airQualityForecastObj.pm10 == null) {
                pm10 = -1
            }
            if (airQualityForecastObj.pm25 == null) {
                pm25 = -1
            }
            if (airQualityForecastObj.o3 == null) {
                o3 = -1
            }
            pm10GradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(pm10))
            pm25GradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(pm25))
            o3GradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(o3))
        }
        val pm10BarDataSet = BarDataSet(pm10EntryList, "pm10")
        val pm25BarDataSet = BarDataSet(pm25EntryList, "pm25")
        val o3BarDataSet = BarDataSet(o3EntryList, "o3")
        pm10BarDataSet.setColors(pm10ColorList)
        pm25BarDataSet.setColors(pm25ColorList)
        o3BarDataSet.setColors(o3ColorList)
        val pm10DataSet: MutableList<IBarDataSet> = ArrayList<IBarDataSet>()
        pm10DataSet.add(pm10BarDataSet)
        val pm25DataSet: MutableList<IBarDataSet> = ArrayList<IBarDataSet>()
        pm25DataSet.add(pm25BarDataSet)
        val o3DataSet: MutableList<IBarDataSet> = ArrayList<IBarDataSet>()
        o3DataSet.add(o3BarDataSet)
        val pm10BarData = BarData(pm10DataSet)
        val pm25BarData = BarData(pm25DataSet)
        val o3BarData = BarData(o3DataSet)
        pm10BarData.setHighlightEnabled(false)
        pm25BarData.setHighlightEnabled(false)
        o3BarData.setHighlightEnabled(false)
        pm10BarData.setValueTextSize(15f)
        pm25BarData.setValueTextSize(15f)
        o3BarData.setValueTextSize(15f)
        pm10BarData.setValueFormatter(GradeValueFormatter())
        pm25BarData.setValueFormatter(GradeValueFormatter())
        o3BarData.setValueFormatter(GradeValueFormatter())
        binding.pm10Chart.data = pm10BarData
        binding.pm10Chart.xAxis.valueFormatter = pm10BottomXAxisLabelFormatter
        binding.pm25Chart.data = pm25BarData
        binding.pm25Chart.xAxis.valueFormatter = pm25BottomXAxisLabelFormatter
        binding.o3Chart.data = o3BarData
        binding.o3Chart.xAxis.valueFormatter = o3BottomXAxisLabelFormatter
        MainThreadWorker.runOnUiThread(Runnable {
            val cellWidth = resources.getDimension(R.dimen.minColumnWidthInAirQualityBarView).toInt()
            val viewWidth = cellWidth * dateList.size
            val dateView = TextsView(requireContext().applicationContext, viewWidth, cellWidth, dateList)
            dateView.setValueTextSize(14)
            dateView.setValueTextColor(Color.BLACK)
            binding.dateRow.removeAllViews()
            binding.dateRow.addView(dateView)
            setBarChartSize(binding.pm10Chart)
            setBarChartSize(binding.pm25Chart)
            setBarChartSize(binding.o3Chart)
            binding.pm10Chart.invalidate()
            binding.pm25Chart.invalidate()
            binding.o3Chart.invalidate()
        })
    }

    private fun setDataForLineChart(airQualityForecastObjList: List<AirQualityForecastObj>) {
        val pm10EntryList: MutableList<Entry> = ArrayList()
        val pm25EntryList: MutableList<Entry> = ArrayList()
        val o3EntryList: MutableList<Entry> = ArrayList()
        val pm10ColorList: MutableList<Int> = ArrayList()
        val pm25ColorList: MutableList<Int> = ArrayList()
        val o3ColorList: MutableList<Int> = ArrayList()
        val pm10GradeDescriptionList: MutableList<String?> = ArrayList()
        val pm25GradeDescriptionList: MutableList<String?> = ArrayList()
        val o3GradeDescriptionList: MutableList<String?> = ArrayList()
        val gradeDescriptionMap = ArrayMap<Int, String?>()
        val dateList: MutableList<String> = ArrayList()
        var pm10Description: String? = null
        var pm25Description: String? = null
        var o3Description: String? = null
        val bottomXAxisLabelFormatter = BottomXAxisLabelFormatter(dateList)
        for (i in airQualityForecastObjList.indices) {
            val airQualityForecastObj: AirQualityForecastObj = airQualityForecastObjList[i]
            dateList.add(
                if (airQualityForecastObj.date == null) getString(R.string.current) else airQualityForecastObj.date.format(
                    DATE_FORMATTER
                )
            )
            val pm10 = if (airQualityForecastObj.pm10 == null) -1 else airQualityForecastObj.pm10
            val pm25 = if (airQualityForecastObj.pm25 == null) -1 else airQualityForecastObj.pm25
            val o3 = if (airQualityForecastObj.o3 == null) -1 else airQualityForecastObj.o3
            pm10EntryList.add(Entry(i.toFloat(), pm10.toFloat(), airQualityForecastObj))
            pm25EntryList.add(Entry(i.toFloat(), pm25.toFloat(), airQualityForecastObj))
            o3EntryList.add(Entry(i.toFloat(), o3.toFloat(), airQualityForecastObj))
            pm10ColorList.add(AqicnResponseProcessor.getGradeColorId(pm10))
            pm25ColorList.add(AqicnResponseProcessor.getGradeColorId(pm25))
            o3ColorList.add(AqicnResponseProcessor.getGradeColorId(o3))
            pm10Description = AqicnResponseProcessor.getGradeDescription(pm10)
            pm25Description = AqicnResponseProcessor.getGradeDescription(pm25)
            o3Description = AqicnResponseProcessor.getGradeDescription(o3)
            pm10GradeDescriptionList.add(pm10Description)
            pm25GradeDescriptionList.add(pm25Description)
            o3GradeDescriptionList.add(o3Description)
            gradeDescriptionMap[pm10] = pm10Description
            gradeDescriptionMap[pm25] = pm25Description
            gradeDescriptionMap[o3] = o3Description
        }
        val pm10LineDataSet = LineDataSet(pm10EntryList, "pm10")
        val pm25LineDataSet = LineDataSet(pm25EntryList, "pm25")
        val o3LineDataSet = LineDataSet(o3EntryList, "o3")
        setCommonLineDataSet(pm10LineDataSet, pm10ColorList)
        setCommonLineDataSet(pm25LineDataSet, pm25ColorList)
        setCommonLineDataSet(o3LineDataSet, o3ColorList)
        pm10LineDataSet.setColor(pm10LineColor)
        pm25LineDataSet.setColor(pm25LineColor)
        o3LineDataSet.setColor(o3LineColor)
        /*
		pm10LineDataSet.setValueFormatter(new GradeValueFormatter(gradeDescriptionMap));
		pm25LineDataSet.setValueFormatter(new GradeValueFormatter(gradeDescriptionMap));
		o3LineDataSet.setValueFormatter(new GradeValueFormatter(gradeDescriptionMap));


 */
        val dataSet: MutableList<ILineDataSet> = ArrayList<ILineDataSet>()
        dataSet.add(pm10LineDataSet)
        dataSet.add(pm25LineDataSet)
        dataSet.add(o3LineDataSet)
        val lineData = LineData(dataSet)
        lineData.setHighlightEnabled(true)
        lineData.setDrawValues(true)

        //binding.pm10Chart.setData(lineData);
        binding.pm10Chart.xAxis.valueFormatter = bottomXAxisLabelFormatter
        binding.pm10Chart.xAxis.axisMinimum = -1f
        binding.pm10Chart.xAxis.axisMaximum = lineData.getXMax() + 1
        binding.pm10Chart.xAxis.labelCount = dateList.size
        MainThreadWorker.runOnUiThread(Runnable { binding.pm10Chart.invalidate() })
    }

    private fun setCommonLineDataSet(lineDataSet: LineDataSet, circleColors: List<Int>) {
        lineDataSet.setCircleRadius(5f)
        lineDataSet.setHighLightColor(Color.GRAY)
        lineDataSet.setDrawCircleHole(false)
        lineDataSet.setHighlightLineWidth(2f)
        lineDataSet.setValueTextSize(14f)
        lineDataSet.setDrawHorizontalHighlightIndicator(false)
        lineDataSet.setLineWidth(3f)
        lineDataSet.setCircleColors(circleColors)
        lineDataSet.setDrawValues(true)
        lineDataSet.setValueTextColors(circleColors)
        lineDataSet.setHighlightEnabled(true)
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER)
    }

    private fun addGridItem(value: Int?, labelDescriptionId: Int, labelIconId: Int): View {
        val gridItem = layoutInflater.inflate(R.layout.air_quality_item, null)
        (gridItem.findViewById<View>(R.id.label_icon) as ImageView).setImageResource(labelIconId)
        (gridItem.findViewById<View>(R.id.label) as TextView).setText(labelDescriptionId)
        (gridItem.findViewById<View>(R.id.label) as TextView).setTextColor(Color.BLACK)
        (gridItem.findViewById<View>(R.id.value_int) as TextView).text = value?.toString() ?: "?"
        (gridItem.findViewById<View>(R.id.value_int) as TextView).setTextColor(Color.BLACK)
        (gridItem.findViewById<View>(R.id.value_str) as TextView).text =
            if (value == null) getString(R.string.noData) else AqicnResponseProcessor.getGradeDescription(value)
        (gridItem.findViewById<View>(R.id.value_str) as TextView).setTextColor(
            if (value == null) ContextCompat.getColor(
                requireContext().applicationContext,
                R.color.not_data_color
            ) else AqicnResponseProcessor.getGradeColorId(value)
        )
        val cellCount = binding.grid.childCount
        val row = cellCount / binding.grid.columnCount
        val column = cellCount % binding.grid.columnCount
        val layoutParams = GridLayout.LayoutParams()
        layoutParams.columnSpec = GridLayout.spec(column, GridLayout.FILL, 1f)
        layoutParams.rowSpec = GridLayout.spec(row, GridLayout.FILL, 1f)
        binding.grid.addView(gridItem, layoutParams)
        return gridItem
    }

    private fun setAirPollutionMaterialsInfo() {
        val icons = intArrayOf(
            R.drawable.pm10, R.drawable.pm25, R.drawable.co, R.drawable.no2, R.drawable.so2,
            R.drawable.o3
        )
        val names = arrayOf(
            getString(R.string.pm10_str),
            getString(R.string.pm25_str),
            getString(R.string.co_str),
            getString(R.string.no2_str),
            getString(R.string.so2_str),
            getString(R.string.o3_str)
        )
        val descriptions = arrayOf(
            getString(R.string.pm10_description),
            getString(R.string.pm25_description),
            getString(R.string.co_description),
            getString(R.string.no2_description),
            getString(R.string.so2_description),
            getString(R.string.o3_description)
        )
        var infoItem: View? = null
        binding.airPollutionMaterialsInfo.removeAllViews()
        val layoutInflater = layoutInflater
        for (i in icons.indices) {
            infoItem = layoutInflater.inflate(R.layout.air_pollution_material_info_item_view, null)
            (infoItem.findViewById<View>(R.id.material_icon) as ImageView).setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext().applicationContext,
                    icons[i]
                )
            )
            (infoItem.findViewById<View>(R.id.material_name) as TextView).text = names[i]
            (infoItem.findViewById<View>(R.id.material_description) as TextView).text = descriptions[i]
            binding.airPollutionMaterialsInfo.addView(infoItem)
        }
    }

    private fun setAqiGradeInfo() {
        val gradeColors = resources.getIntArray(R.array.AqiGradeColors)
        val gradeRanges = resources.getStringArray(R.array.AqiGradeRange)
        val description = resources.getStringArray(R.array.AqiGradeState)
        var infoItem: View? = null
        binding.aqiGradeInfo.removeAllViews()
        val layoutInflater = layoutInflater
        for (i in gradeColors.indices) {
            infoItem = layoutInflater.inflate(R.layout.aqi_grade_info_view, null)
            (infoItem.findViewById(R.id.grade_color) as View).backgroundTintList =
                ColorStateList.valueOf(gradeColors[i])
            (infoItem.findViewById<View>(R.id.grade_range) as TextView).text = gradeRanges[i]
            (infoItem.findViewById<View>(R.id.grade_state) as TextView).text = description[i]
            binding.aqiGradeInfo.addView(infoItem)
        }
    }

    protected class BottomXAxisLabelFormatter(var gradeDescriptionList: List<String>) : ValueFormatter() {
        var `val` = 0
        override fun getAxisLabel(value: Float, axis: AxisBase): String {
            `val` = value.toInt()
            return if (`val` < gradeDescriptionList.size && `val` >= 0) gradeDescriptionList[`val`] else ""
        }
    }

    protected class GradeValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return value.toInt().toString()
        }
    }

    protected class BothAxisLabelRenderer(
        viewPortHandler: ViewPortHandler?,
        xAxis: XAxis?,
        transformer: Transformer?,
        private val topValueFormatter: ValueFormatter
    ) : XAxisRenderer(viewPortHandler, xAxis, transformer) {
        override fun renderAxisLabels(c: Canvas) {
            if (!mXAxis.isEnabled() || !mXAxis.isDrawLabelsEnabled()) return
            val yOffset: Float = mXAxis.getYOffset()
            mAxisLabelPaint.setTypeface(mXAxis.getTypeface())
            mAxisLabelPaint.setTextSize(mXAxis.getTextSize())
            mAxisLabelPaint.setColor(mXAxis.getTextColor())
            val pointF: MPPointF = MPPointF.getInstance(0f, 0f)
            pointF.x = 0.5f
            pointF.y = 1.0f
            drawLabelsTop(c, mViewPortHandler.contentTop() - yOffset, pointF)
            pointF.x = 0.5f
            pointF.y = 0.0f
            drawLabels(c, mViewPortHandler.contentBottom() + yOffset, pointF)
            MPPointF.recycleInstance(pointF)
        }

        private fun drawLabelsTop(canvas: Canvas, y: Float, anchor: MPPointF) {
            val labelRotationAngleDegrees: Float = mXAxis.getLabelRotationAngle()
            val centeringEnabled: Boolean = mXAxis.isCenterAxisLabelsEnabled()
            val positions = FloatArray(mXAxis.mEntryCount * 2)
            run {
                var i = 0
                while (i < positions.size) {


                    // only fill x values
                    if (centeringEnabled) {
                        positions[i] = mXAxis.mCenteredEntries.get(i / 2)
                    } else {
                        positions[i] = mXAxis.mEntries.get(i / 2)
                    }
                    i += 2
                }
            }
            mTrans.pointValuesToPixel(positions)
            var i = 0
            while (i < positions.size) {
                var x = positions[i]
                if (mViewPortHandler.isInBoundsX(x)) {
                    val label = topValueFormatter.getAxisLabel(mXAxis.mEntries.get(i / 2), mXAxis)
                    if (mXAxis.isAvoidFirstLastClippingEnabled()) {

                        // avoid clipping of the last
                        if (i == mXAxis.mEntryCount - 1 && mXAxis.mEntryCount > 1) {
                            val width = Utils.calcTextWidth(mAxisLabelPaint, label).toFloat()
                            if (width > mViewPortHandler.offsetRight() * 2
                                && x + width > mViewPortHandler.getChartWidth()
                            ) x -= width / 2

                            // avoid clipping of the first
                        } else if (i == 0) {
                            val width = Utils.calcTextWidth(mAxisLabelPaint, label).toFloat()
                            x += width / 2
                        }
                    }
                    drawLabel(canvas, label, x, y, anchor, labelRotationAngleDegrees)
                }
                i += 2
            }
        }
    }

    protected inner class ChartGestureListener(val mainChart: Chart<*>, vararg otherChars: BarChart) : OnChartGestureListener {
        val otherChars: Array<BarChart>

        init {
            this.otherChars = otherChars
        }

        override fun onChartGestureStart(me: MotionEvent, lastPerformedGesture: ChartGesture) {
            syncCharts(mainChart, *otherChars)
        }

        override fun onChartGestureEnd(me: MotionEvent, lastPerformedGesture: ChartGesture) {
            //binding.pm25Chart.getOnChartGestureListener().onChartGestureEnd(me, lastPerformedGesture);
            syncCharts(mainChart, *otherChars)
        }

        override fun onChartLongPressed(me: MotionEvent) {}
        override fun onChartDoubleTapped(me: MotionEvent) {}
        override fun onChartSingleTapped(me: MotionEvent) {}
        override fun onChartFling(me1: MotionEvent, me2: MotionEvent, velocityX: Float, velocityY: Float) {
            syncCharts(mainChart, *otherChars)
        }

        override fun onChartScale(me: MotionEvent, scaleX: Float, scaleY: Float) {}
        override fun onChartTranslate(me: MotionEvent, dX: Float, dY: Float) {}
    }

    companion object {
        private var response: AqiCnGeolocalizedFeedResponse? = null
        @JvmStatic
        fun setResponse(response: AqiCnGeolocalizedFeedResponse?) {
            Companion.response = response
        }
    }
}