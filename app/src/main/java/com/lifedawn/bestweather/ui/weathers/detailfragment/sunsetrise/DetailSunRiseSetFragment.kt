package com.lifedawn.bestweather.ui.weathers.detailfragment.sunsetrise

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.MainThreadWorker.runOnUiThread
import com.lifedawn.bestweather.commons.constants.BundleKey
import com.lifedawn.bestweather.data.MyApplication
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.SunRiseSetUtil.SunRiseSetObj
import com.lifedawn.bestweather.databinding.FragmentDetailSunRiseSetBinding
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator
import com.luckycatlabs.sunrisesunset.dto.Location
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

class DetailSunRiseSetFragment : Fragment() {
    private var binding: FragmentDetailSunRiseSetBinding? = null
    private var addressName: String? = null
    private var zoneId: ZoneId? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var bundle: Bundle? = null
    private val minusWeeks = 1
    private val plusWeeks = 20
    private val executorService = MyApplication.getExecutorService()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = savedInstanceState ?: arguments
        addressName = bundle!!.getString(BundleKey.AddressName.name)
        zoneId = bundle!!.getSerializable(BundleKey.TimeZone.name) as ZoneId?
        latitude = bundle!!.getDouble(BundleKey.Latitude.name)
        longitude = bundle!!.getDouble(BundleKey.Longitude.name)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailSunRiseSetBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    @SuppressLint("ClickableViewAccessibility", "MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutParams = binding!!.toolbar.root.layoutParams as LinearLayout.LayoutParams
        layoutParams.topMargin = MyApplication.getStatusBarHeight()
        binding!!.toolbar.root.layoutParams = layoutParams
        binding!!.adViewBelowScrollView.loadAd(AdRequest.Builder().build())
        binding!!.adViewBelowScrollView.adListener = object : AdListener() {
            override fun onAdClosed() {
                super.onAdClosed()
                binding!!.adViewBelowScrollView.loadAd(AdRequest.Builder().build())
            }
        }
        binding!!.toolbar.fragmentTitle.setText(R.string.detailSunRiseSet)
        binding!!.toolbar.backBtn.setOnClickListener { parentFragmentManager.popBackStackImmediate() }
        binding!!.chart.setOnTouchListener { v, event -> false }
        binding!!.chart.setNoDataText(getString(R.string.sun_set_rise))
        binding!!.chart.setNoDataTextColor(Color.BLUE)
        binding!!.chart.setDrawGridBackground(false)
        binding!!.chart.description.isEnabled = false
        binding!!.chart.isScaleXEnabled = false
        binding!!.chart.setDrawValueAboveBar(true)
        binding!!.chart.isHighlightFullBarEnabled = false
        binding!!.chart.setExtraOffsets(0f, 10f, 0f, 20f)
        binding!!.chart.axisLeft.isEnabled = false
        binding!!.chart.axisRight.axisMaximum = 720f
        binding!!.chart.axisRight.axisMinimum = -720f
        binding!!.chart.axisRight.setDrawGridLines(false)
        binding!!.chart.axisRight.setDrawZeroLine(true)
        binding!!.chart.axisRight.textSize = 12f
        binding!!.chart.axisRight.yOffset = -3f
        binding!!.chart.axisRight.labelCount = 4
        binding!!.chart.axisRight.valueFormatter = TimeFormatter()
        val xAxis = binding!!.chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.textSize = 13f
        xAxis.setCenterAxisLabels(false)
        xAxis.labelCount = 12
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        binding!!.chart.legend.isEnabled = false

        // 기준 날짜 1주일 전 - 기준 날짜 - 기준 날짜 3달 후
        executorService.execute {
            val sunRiseSetObjList = calcSunRiseSets(ZonedDateTime.now(zoneId))
            if (sunRiseSetObjList.isEmpty()) {
                //error
                binding!!.chart.setNoDataText(getString(R.string.failed_calculating_sun_rise_set))
            } else {
                //차트 설정
                val sunRiseSetTimeDataList: MutableList<BarEntry> = ArrayList()
                val dateListForAxis: MutableList<String> = ArrayList()
                val dateFormatter = DateTimeFormatter.ofPattern("M.d E")
                val context = requireContext().applicationContext
                val rightColor = ContextCompat.getColor(context, R.color.sunGradientStart)
                val leftColor = ContextCompat.getColor(context, R.color.sunGradientEnd)
                val todayColor = ContextCompat.getColor(context, android.R.color.holo_blue_bright)
                var index = 0
                var leftMinutes: Int
                var rightMinutes: Int
                val todayIndex = Math.abs(minusWeeks * 7)
                val barColors = IntArray(sunRiseSetObjList.size * 2)
                var colorIndex = 0
                for (sunRiseSetObj in sunRiseSetObjList) {
                    if (index == todayIndex) {
                        barColors[colorIndex + 1] = todayColor
                        barColors[colorIndex] = todayColor
                    } else {
                        barColors[colorIndex + 1] = rightColor
                        barColors[colorIndex] = leftColor
                    }
                    colorIndex = colorIndex + 2
                    dateListForAxis.add(sunRiseSetObj.zonedDateTime!!.format(dateFormatter))
                    leftMinutes = -720 + 60 * sunRiseSetObj.sunrise[Calendar.HOUR_OF_DAY] + sunRiseSetObj.sunrise[Calendar.MINUTE]
                    rightMinutes = 60 * sunRiseSetObj.sunset[Calendar.HOUR_OF_DAY] +
                            sunRiseSetObj.sunset[Calendar.MINUTE] - 720
                    sunRiseSetTimeDataList.add(
                        BarEntry(
                            index.toFloat(),
                            floatArrayOf(leftMinutes.toFloat(), rightMinutes.toFloat()),
                            sunRiseSetObj
                        )
                    )
                    index++
                }
                val barDataSet = BarDataSet(sunRiseSetTimeDataList, "sunRiseSet")
                barDataSet.setDrawIcons(false)
                barDataSet.valueFormatter = TimeFormatter()
                barDataSet.valueTextSize = 13f
                barDataSet.axisDependency = YAxis.AxisDependency.RIGHT
                barDataSet.setColors(*barColors)
                val barData = BarData(barDataSet)
                barData.isHighlightEnabled = false
                binding!!.chart.data = barData
                binding!!.chart.xAxis.valueFormatter = XAxisDateFormatter(dateListForAxis)
            }
            runOnUiThread {
                binding!!.chart.invalidate()
                if (sunRiseSetObjList.size > 0) {
                    binding!!.chart.zoom(0f, 7f, 0f, 0f)
                    val x = binding!!.chart.left + binding!!.chart.width / 2f
                    val y = binding!!.chart.top + binding!!.chart.height / 2f
                    /*
                                            4384
                                            4457
                                            4531
                                            4594
                                            */
                    val handler = Handler()
                    val firstDownTime = SystemClock.uptimeMillis()
                    binding!!.chart.dispatchTouchEvent(
                        MotionEvent.obtain(
                            firstDownTime,
                            firstDownTime,
                            MotionEvent.ACTION_DOWN, x, y, 0
                        )
                    )
                    handler.postDelayed({
                        binding!!.chart.dispatchTouchEvent(
                            MotionEvent.obtain(
                                firstDownTime,
                                SystemClock.uptimeMillis(),
                                MotionEvent.ACTION_UP, x, y, 0
                            )
                        )
                        handler.postDelayed({
                            val secDownTime = SystemClock.uptimeMillis()
                            binding!!.chart.dispatchTouchEvent(
                                MotionEvent.obtain(
                                    secDownTime,
                                    secDownTime,
                                    MotionEvent.ACTION_DOWN, x, y, 0
                                )
                            )
                            handler.postDelayed({
                                binding!!.chart.dispatchTouchEvent(
                                    MotionEvent.obtain(
                                        secDownTime,
                                        SystemClock.uptimeMillis(),
                                        MotionEvent.ACTION_UP, x, y, 0
                                    )
                                )
                            }, 20)
                        }, 40)
                    }, 20)
                }
            }
        }
    }

    private fun calcSunRiseSets(criteriaZonedDateTime: ZonedDateTime): List<SunRiseSetObj> {
        val zoneOffset = criteriaZonedDateTime.offset
        val offset = zoneOffset.totalSeconds * 1000
        val realTimeZone: TimeZone = SimpleTimeZone(offset, "")
        val criteria = ZonedDateTime.of(criteriaZonedDateTime.toLocalDateTime(), zoneId)
        var beginUtc0ZonedDateTime = criteria.minusWeeks(minusWeeks.toLong())
        var beginRealZonedDateTime = criteria.minusWeeks(minusWeeks.toLong())
        var endUtc0ZonedDateTime = criteria.plusWeeks(plusWeeks.toLong())
        beginUtc0ZonedDateTime = beginUtc0ZonedDateTime.withZoneSameLocal(ZoneOffset.UTC)
        endUtc0ZonedDateTime = endUtc0ZonedDateTime.withZoneSameLocal(ZoneOffset.UTC)
        var beginDay: Long
        val endDay = TimeUnit.MILLISECONDS.toDays(endUtc0ZonedDateTime.toInstant().toEpochMilli())
        val list: MutableList<SunRiseSetObj> = ArrayList()
        val calculator = SunriseSunsetCalculator(Location(latitude!!, longitude!!), realTimeZone)
        val calendar = Calendar.getInstance(realTimeZone)
        do {
            calendar.timeInMillis = beginRealZonedDateTime.toInstant().toEpochMilli()
            val sunRiseSetObj = SunRiseSetObj(
                beginRealZonedDateTime, calculator.getOfficialSunriseCalendarForDate(calendar),
                calculator.getOfficialSunsetCalendarForDate(calendar)
            )
            if (sunRiseSetObj.sunrise == null || sunRiseSetObj.sunset == null) {
                // 일출/일몰 계산 오류 발생 하면 리스트 비우고 반환
                list.clear()
                return list
            }
            list.add(sunRiseSetObj)
            beginUtc0ZonedDateTime = beginUtc0ZonedDateTime.plusDays(1)
            beginRealZonedDateTime = beginRealZonedDateTime.plusDays(1)
            beginDay = TimeUnit.MILLISECONDS.toDays(beginUtc0ZonedDateTime.toInstant().toEpochMilli())
        } while (beginDay <= endDay)
        return list
    }

    private class XAxisDateFormatter(val dateList: List<String>) : ValueFormatter() {
        var `val` = 0
        override fun getAxisLabel(value: Float, axis: AxisBase): String {
            `val` = value.toInt()
            return if (dateList.size > `val` && `val` >= 0) dateList[`val`] else ""
        }
    }

    private class TimeFormatter : ValueFormatter() {
        val timeFormatter = DateTimeFormatter.ofPattern("H:mm")
        var localTime = LocalTime.of(12, 0)
        override fun getAxisLabel(value: Float, axis: AxisBase): String {
            return convert(value)
        }

        override fun getFormattedValue(value: Float): String {
            return convert(value)
        }

        private fun convert(value: Float): String {
            return localTime.plusMinutes(value.toLong()).format(timeFormatter)
            //			return localTime.plusMinutes((long) value).format(timeFormatter);
        }
    }
}