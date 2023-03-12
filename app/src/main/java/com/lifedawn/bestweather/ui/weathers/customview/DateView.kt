package com.lifedawn.bestweather.ui.weathers.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.StaticLayout
import android.text.TextPaint
import android.util.TypedValue
import android.view.View
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.ui.theme.AppTheme.getTextColor
import com.lifedawn.bestweather.ui.weathers.enums.WeatherDataType
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class DateView(context: Context, private val weatherDataType: WeatherDataType, private val viewWidth: Int, private val columnWidth: Int) :
    View(context), ICleaner {
    private val dateTextPaint: TextPaint
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("M.d\nE")
    private var dateValueList: MutableList<DateValue>? = null
    private var currentX = 0
    private var firstColX = 0
    private val padding: Int
    private var viewHeight = 0

    init {
        padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics).toInt()
        dateTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        dateTextPaint.textAlign = Paint.Align.CENTER
        dateTextPaint.textSize = context.resources.getDimension(R.dimen.dateValueTextSizeInSCD)
        dateTextPaint.color = getTextColor(weatherDataType)
        setWillNotDraw(false)
    }

    fun setTextSize(textSizeSp: Int) {
        dateTextPaint.textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, textSizeSp.toFloat(),
            resources.displayMetrics
        ).toInt().toFloat()
    }

    fun setTextColor(textColor: Int) {
        dateTextPaint.color = textColor
    }

    fun init(dateTimeList: List<ZonedDateTime>) {
        var date = ZonedDateTime.of(dateTimeList[0].toLocalDateTime(), dateTimeList[0].zone)
        var lastDate = ZonedDateTime.of(date.toLocalDateTime(), date.zone)
        lastDate = lastDate.minusDays(5)
        val dateValueList: MutableList<DateValue> = ArrayList()
        var beginX = 0
        for (col in dateTimeList.indices) {
            date = ZonedDateTime.of(dateTimeList[col].toLocalDateTime(), lastDate.zone)
            if (date.dayOfYear != lastDate.dayOfYear || col == 0) {
                if (dateValueList.size > 0) {
                    dateValueList[dateValueList.size - 1].endX = columnWidth * (col - 1) + columnWidth / 2
                }
                beginX = columnWidth * col + columnWidth / 2
                dateValueList.add(DateValue(beginX, date))
                lastDate = date
            }
        }
        dateValueList[dateValueList.size - 1].endX = columnWidth * (dateTimeList.size - 1) + columnWidth / 2
        this.dateValueList = dateValueList
        firstColX = dateValueList[0].beginX
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var staticLayout: StaticLayout? = null
        viewHeight = Int.MIN_VALUE
        var str: String? = null
        for (`val` in dateValueList!!) {
            str = `val`.date.format(dateTimeFormatter)
            val builder = StaticLayout.Builder.obtain(
                str, 0, str.length, dateTextPaint,
                columnWidth
            )
            staticLayout = builder.build()
            viewHeight = Math.max(staticLayout.height, viewHeight)
        }
        viewHeight += padding * 2
        setMeasuredDimension(viewWidth, viewHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (dateValue in dateValueList!!) {
            if (currentX >= dateValue.beginX - firstColX && currentX < dateValue.endX - firstColX) {
                dateValue.lastX = currentX + firstColX
            } else if (currentX < dateValue.beginX) {
                dateValue.lastX = dateValue.beginX
            }
            drawText(canvas, dateValue.date.format(dateTimeFormatter), dateValue.lastX.toFloat())
        }
    }

    fun reDraw(newX: Int) {
        currentX = newX
        invalidate()
    }

    private fun drawText(canvas: Canvas, textOnCanvas: String, x: Float) {
        val builder = StaticLayout.Builder.obtain(textOnCanvas, 0, textOnCanvas.length, dateTextPaint, columnWidth)
        val staticLayout = builder.build()
        val y = viewHeight / 2f - staticLayout.height / 2f
        canvas.save()
        canvas.translate(x, y)
        staticLayout.draw(canvas)
        canvas.restore()
    }

    override fun clear() {
        if (dateValueList != null) dateValueList!!.clear()
        dateValueList = null
    }

    class DateValue(val beginX: Int, val date: ZonedDateTime) {
        var endX = 0
        var lastX: Int

        init {
            lastX = beginX
        }
    }
}