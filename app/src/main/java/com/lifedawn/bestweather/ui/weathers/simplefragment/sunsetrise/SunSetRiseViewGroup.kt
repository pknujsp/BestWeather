package com.lifedawn.bestweather.ui.weathers.simplefragment.sunsetrise

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.StaticLayout
import android.text.TextPaint
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.SunRiseSetType
import com.lifedawn.bestweather.commons.constants.ValueUnits
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator
import com.luckycatlabs.sunrisesunset.dto.Location
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

class SunSetRiseViewGroup(
    context: Context,
    private val location: Location?,
    private val zoneId: ZoneId?,
    private val onSunRiseSetListener: OnSunRiseSetListener
) : FrameLayout(context) {
    private var dateTimeFormatter: DateTimeFormatter? = null
    private var type1View: SunSetRiseInfoView? = null
    private var type2View: SunSetRiseInfoView? = null
    private var type3View: SunSetRiseInfoView? = null
    private var errorView: TextView? = null
    private var type1BottomMargin = 0
    private var type2BottomMargin = 0
    private var fullLineMargin = 0
    private var fullLineWidth = 0
    private var imgRadius = 0
    private var roundedRectRadius = 0
    private var dividerHeight = 0
    private var dividerPaint: Paint? = null
    private var fullLinePaint: Paint? = null
    private var currentLinePaint: Paint? = null
    private var timeTextPaint: TextPaint? = null
    private var fullLineRect: RectF? = null
    private var currentLineRect: RectF? = null
    private var timeTextRect: Rect? = null
    private var currentTextRect: Rect? = null
    private var timeCirclePoint: Point? = null
    private var timeCirclePaint: Paint? = null
    private var type1PointOnLine: Point? = null
    private var type2PointOnLine: Point? = null
    private var current: String? = null
    private var img: Drawable? = null
    private val imgRect = Rect()
    private fun setImg(sunRiseSetType: SunRiseSetType) {
        img =
            ContextCompat.getDrawable(context, if (sunRiseSetType === SunRiseSetType.RISE) R.drawable.day_clear else R.drawable.night_clear)
    }

    init {
        init(context)
    }

    private fun init(context: Context) {
        setWillNotDraw(false)
        val displayMetrics = resources.displayMetrics
        type1BottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, displayMetrics).toInt()
        type2BottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, displayMetrics).toInt()
        fullLineMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, displayMetrics).toInt()
        fullLineWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, displayMetrics).toInt()
        imgRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18f, displayMetrics).toInt()
        roundedRectRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, displayMetrics).toInt()
        dividerHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, displayMetrics).toInt()
        dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        dividerPaint!!.style = Paint.Style.FILL
        dividerPaint!!.color = Color.GRAY
        fullLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        fullLinePaint!!.style = Paint.Style.FILL
        fullLinePaint!!.color = Color.GRAY
        currentLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        currentLinePaint!!.style = Paint.Style.FILL
        currentLinePaint!!.color = Color.WHITE
        timeTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        timeTextPaint!!.color = Color.WHITE
        timeTextPaint!!.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, resources.displayMetrics)
        timeTextPaint!!.textAlign = Paint.Align.CENTER
        timeCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        timeCirclePaint!!.color = Color.WHITE
        fullLineRect = RectF()
        currentLineRect = RectF()
        timeTextRect = Rect()
        timeCirclePoint = Point()
        currentTextRect = Rect()
        current = context.getString(R.string.current)
        val clockUnit = ValueUnits.valueOf(
            PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.pref_key_unit_clock),
                ValueUnits.clock12.name
            )!!
        )
        val dateTimeFormat =
            if (clockUnit === ValueUnits.clock12) context.getString(R.string.datetime_pattern_clock12) else context.getString(
                R.string.datetime_pattern_clock24
            )
        val now = ZonedDateTime.now(zoneId)
        dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormat)
        val currentDateTime = now.format(dateTimeFormatter)
        timeTextPaint!!.getTextBounds(currentDateTime, 0, currentDateTime.length, timeTextRect)
        type1PointOnLine = Point()
        type2PointOnLine = Point()
        setViews()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (type1View != null && type2View != null && type3View != null) {
            val widthSize = MeasureSpec.getSize(widthMeasureSpec)
            setMeasuredDimension(
                widthSize, type1View.getPixelHeight() + type1BottomMargin + type2View.getPixelHeight() + type2BottomMargin
                        + type3View.getPixelHeight()
            )
        } else {
            setMeasuredDimension(widthMeasureSpec, errorView!!.textSize.toInt() * 2)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (type1View != null && type2View != null && type3View != null) {
            type1View!!.measure(type1View.getPixelWidth(), type1View.getPixelHeight())
            type2View!!.measure(type2View.getPixelWidth(), type2View.getPixelHeight())
            type3View!!.measure(type3View.getPixelWidth(), type3View.getPixelHeight())
            val centerX = right / 2
            var childRight = centerX + type1View.getPixelWidth()
            var childTop = 0
            var childBottom = type1View.getPixelHeight()
            val lineTop = childTop + type1View.getPixelHeight() / 2
            type1PointOnLine!!.y = lineTop
            type1View!!.layout(centerX, childTop, childRight, childBottom)
            childRight = centerX + type2View.getPixelWidth()
            childTop = childBottom + type1BottomMargin
            childBottom = childTop + type2View.getPixelHeight()
            type2PointOnLine!!.y = childTop + type2View.getPixelHeight() / 2
            type2View!!.layout(centerX, childTop, childRight, childBottom)
            childRight = centerX + type3View.getPixelWidth()
            childTop = childBottom + type2BottomMargin
            childBottom = childTop + type3View.getPixelHeight()
            val lineBottom = childTop + type3View.getPixelHeight() / 2
            type3View!!.layout(centerX, childTop, childRight, childBottom)
            type1PointOnLine!!.x = centerX - fullLineMargin - fullLineWidth / 2
            type2PointOnLine!!.x = type1PointOnLine!!.x
            fullLineRect!![(centerX - fullLineMargin - fullLineWidth).toFloat(), lineTop.toFloat(), (centerX - fullLineMargin).toFloat()] =
                lineBottom.toFloat()
        } else {
            errorView!!.layout(0, 0, width, errorView!!.textSize.toInt() * 2)
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (type1View != null && type2View != null && type3View != null) {
            val now = ZonedDateTime.now(zoneId)
            var millis = now.toInstant().toEpochMilli()
            val nowTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(millis)
            millis = type1View.getDateTime().toInstant().toEpochMilli()
            val type1Minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
            millis = type2View.getDateTime().toInstant().toEpochMilli()
            val type2Minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
            val diff = type2Minutes - type1Minutes
            val heightPerMinute = (type2PointOnLine!!.y - type1PointOnLine!!.y).toFloat() / diff.toFloat()
            val currentTimeY = type1PointOnLine!!.y + heightPerMinute * (nowTimeMinutes - type1Minutes)
            timeCirclePoint!!.x = type1PointOnLine!!.x
            timeCirclePoint!!.y = currentTimeY.toInt()
            imgRect.left = timeCirclePoint!!.x - imgRadius
            imgRect.top = timeCirclePoint!!.y - imgRadius
            imgRect.right = timeCirclePoint!!.x + imgRadius
            imgRect.bottom = timeCirclePoint!!.y + imgRadius
            currentLineRect!!.left = fullLineRect!!.left
            currentLineRect!!.right = fullLineRect!!.right
            currentLineRect!!.top = fullLineRect!!.top
            currentLineRect!!.bottom = imgRect.centerY().toFloat()
            canvas.drawRoundRect(fullLineRect!!, roundedRectRadius.toFloat(), roundedRectRadius.toFloat(), fullLinePaint!!)
            canvas.drawRoundRect(currentLineRect!!, roundedRectRadius.toFloat(), roundedRectRadius.toFloat(), currentLinePaint!!)
            canvas.drawRect(
                fullLineRect!!.right, fullLineRect!!.top, fullLineRect!!.right + fullLineMargin / 2f,
                fullLineRect!!.top + dividerHeight, dividerPaint!!
            )
            canvas.drawRect(
                fullLineRect!!.right, type2PointOnLine!!.y.toFloat(), fullLineRect!!.right + fullLineMargin / 2f,
                (
                        type2PointOnLine!!.y + dividerHeight).toFloat(),
                dividerPaint!!
            )
            drawCurrent(now.format(dateTimeFormatter), canvas)
        }
    }

    private fun drawCurrent(dateTime: String, canvas: Canvas) {
        val textOnCanvas = """
            $current
            $dateTime
            """.trimIndent()
        timeTextPaint!!.getTextBounds(textOnCanvas, 0, textOnCanvas.length, timeTextRect)
        val builder = StaticLayout.Builder.obtain(
            textOnCanvas, 0, textOnCanvas.length, timeTextPaint!!,
            timeTextRect!!.width()
        )
        val sl = builder.build()
        canvas.save()
        val criteriaX = (fullLineRect!!.left - fullLineMargin - timeTextRect!!.width() / 2).toInt()
        val criteriaY = timeCirclePoint!!.y
        val textHeight = timeTextRect!!.height().toFloat()
        val textYCoordinate = criteriaY + timeTextRect!!.exactCenterY() - sl.lineCount * textHeight / 2
        canvas.translate(criteriaX.toFloat(), textYCoordinate)
        sl.draw(canvas)
        canvas.restore()
    }

    fun refresh() {
        if (type2View == null) {
            return
        }
        if (type2View.getDateTime().isBefore(ZonedDateTime.now(zoneId))) {
            setViews()
            invalidate()
            requestLayout()
        } else {
            invalidate()
        }
    }

    fun setViews() {
        val sunriseSunsetCalculator = SunriseSunsetCalculator(location, TimeZone.getTimeZone(zoneId!!.id))
        val todayCalendar = Calendar.getInstance(TimeZone.getTimeZone(zoneId.id))
        val yesterdayCalendar = todayCalendar.clone() as Calendar
        val tomorrowCalendar = todayCalendar.clone() as Calendar
        yesterdayCalendar.add(Calendar.DATE, -1)
        tomorrowCalendar.add(Calendar.DATE, 1)
        val todaySunRiseCalendar = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(todayCalendar)
        val todaySunSetCalendar = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(todayCalendar)
        removeAllViews()
        type1View = null
        type2View = null
        type3View = null
        errorView = null
        if (todaySunRiseCalendar == null || todaySunSetCalendar == null) {
            onSunRiseSetListener.onCalcResult(false, false)
            errorView = TextView(context)
            errorView!!.setText(R.string.failed_calculating_sun_rise_set)
            errorView!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            errorView!!.setTextColor(Color.WHITE)
            errorView!!.gravity = Gravity.CENTER
            errorView!!.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            addView(errorView)
            return
        }
        val type1Calendar: Calendar
        val type2Calendar: Calendar
        val type3Calendar: Calendar
        val type1: SunRiseSetType
        val type2: SunRiseSetType
        val type3: SunRiseSetType
        if (todayCalendar.before(todaySunRiseCalendar)) {
            //일출 전 새벽 (0시 0분 <= 현재 시각 < 일출)
            //순서 : 전날 11.06 일몰 - 당일 11.07 일출 - 당일 11.07 일몰
            type1Calendar = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(yesterdayCalendar)
            type1 = SunRiseSetType.SET
            type2Calendar = todaySunRiseCalendar
            type2 = SunRiseSetType.RISE
            type3Calendar = todaySunSetCalendar
            type3 = SunRiseSetType.SET
        } else if (todayCalendar.compareTo(todaySunRiseCalendar) >= 0 && todayCalendar.compareTo(todaySunSetCalendar) <= 0) {
            //일출 후, 일몰 전 (일출 <= 현재 시각 <= 일몰)
            //순서 : 당일 11.07 일출 - 당일 11.07 일몰 - 다음날 11.08 일출
            type1Calendar = todaySunRiseCalendar
            type1 = SunRiseSetType.RISE
            type2Calendar = todaySunSetCalendar
            type2 = SunRiseSetType.SET
            type3Calendar = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(tomorrowCalendar)
            type3 = SunRiseSetType.RISE
        } else {
            //일몰 후 (일몰 < 현재 시각 < 24시 0분 )
            //순서 : 당일 11.07 일몰 - 다음날 11.08 일출 - 다음날 11.08 일몰
            type1Calendar = todaySunSetCalendar
            type1 = SunRiseSetType.SET
            type2Calendar = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(tomorrowCalendar)
            type2 = SunRiseSetType.RISE
            type3Calendar = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(tomorrowCalendar)
            type3 = SunRiseSetType.SET
        }
        setImg(type1)
        val type1ZonedDateTime = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(type1Calendar.timeInMillis),
            zoneId
        )
        val type2ZonedDateTime = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(type2Calendar.timeInMillis),
            zoneId
        )
        val type3ZonedDateTime = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(type3Calendar.timeInMillis),
            zoneId
        )
        type1View = SunSetRiseInfoView(context, type1ZonedDateTime, type1)
        type2View = SunSetRiseInfoView(context, type2ZonedDateTime, type2)
        type3View = SunSetRiseInfoView(context, type3ZonedDateTime, type3)
        addView(type1View)
        addView(type2View)
        addView(type3View)
        onSunRiseSetListener.onCalcResult(true, type1 === SunRiseSetType.SET)
    }
}