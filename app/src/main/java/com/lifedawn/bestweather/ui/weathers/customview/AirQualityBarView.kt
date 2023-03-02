package com.lifedawn.bestweather.ui.weathers.customview

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.getGradeColorId
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.getGradeDescription
import com.lifedawn.bestweather.ui.weathers.FragmentType

class AirQualityBarView(
    context: Context,
    private val fragmentType: FragmentType,
    private val viewWidth: Int,
    private val viewHeight: Int,
    private val columnWidth: Int,
    airQualityObjList: MutableList<AirQualityObj>
) : View(context), ICleaner {
    private val barWidth: Int
    private val barTopBottomMargin: Int
    private val barMinHeight: Int
    private val barPaint: Paint
    private val gradeValueIntPaint: TextPaint
    private val gradeStrPaint: TextPaint
    private val padding: Int
    private val barRect = Rect()
    private val gradeValueIntRect = Rect()
    private val gradeValueIntPoint = Point()
    private val gradeStrRect = Rect()
    private val gradeStrPoint = Point()
    private val minGradeValue: Int
    private val maxGradeValue: Int
    private val airQualityObjList: MutableList<AirQualityObj>?

    init {
        this.airQualityObjList = airQualityObjList
        barWidth = resources.getDimension(R.dimen.barWidthInAirQualityBarView).toInt()
        barTopBottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, resources.displayMetrics).toInt()
        barMinHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()
        padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, resources.displayMetrics).toInt()
        val gradeValueIntStr = "10"
        val gradeStr = context.getString(R.string.good)
        barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        barPaint.color = Color.WHITE
        gradeValueIntPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        gradeValueIntPaint.textAlign = Paint.Align.CENTER
        gradeValueIntPaint.color = Color.BLACK
        gradeValueIntPaint.textSize = resources.getDimension(R.dimen.gradeValueTextSizeInAirQualityBarView)
        gradeValueIntPaint.getTextBounds(gradeValueIntStr, 0, gradeValueIntStr.length, gradeValueIntRect)
        gradeStrPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        gradeStrPaint.textAlign = Paint.Align.CENTER
        gradeStrPaint.color = Color.BLACK
        gradeStrPaint.textSize = resources.getDimension(R.dimen.gradeTextSizeInAirQualityBarView)
        gradeStrPaint.getTextBounds(gradeStr, 0, gradeStr.length, gradeStrRect)
        var min = Int.MAX_VALUE
        var max = Int.MIN_VALUE
        for (airQualityObj in airQualityObjList) {
            if (airQualityObj.`val` == null) {
                airQualityObj.grade = "?"
                airQualityObj.gradeColor = ContextCompat.getColor(context, R.color.not_data_color)
                continue
            }
            if (airQualityObj.`val` < min) {
                min = airQualityObj.`val`
            }
            if (airQualityObj.`val` > max) {
                max = airQualityObj.`val`
            }

            //color, gradeDescription
            airQualityObj.grade = getGradeDescription(airQualityObj.`val`)
            airQualityObj.gradeColor = getGradeColorId(airQualityObj.`val`)
        }
        if (max - min == 0) {
            max++
        }
        minGradeValue = min
        maxGradeValue = max
    }

    fun setGradeIntTextSize(textSizeSp: Int) {
        val gradeValueIntStr = "10"
        gradeValueIntPaint.textSize =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSp.toFloat(), resources.displayMetrics)
        gradeValueIntPaint.getTextBounds(gradeValueIntStr, 0, gradeValueIntStr.length, gradeValueIntRect)
    }

    fun setGradeDescriptionTextSize(textSizeSp: Int) {
        val gradeStr = context.getString(R.string.good)
        gradeStrPaint.textSize =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSp.toFloat(), resources.displayMetrics)
        gradeStrPaint.getTextBounds(gradeStr, 0, gradeStr.length, gradeStrRect)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(viewWidth, viewHeight)
    }

    override fun onDraw(canvas: Canvas) {
        val topFreeSpaceHeight = barTopBottomMargin * 2 + gradeValueIntRect.height()
        val bottomFreeSpaceHeight = gradeStrRect.height() + barTopBottomMargin * 2
        barRect[columnWidth / 2 - barWidth / 2, topFreeSpaceHeight, columnWidth / 2 + barWidth / 2] = height - bottomFreeSpaceHeight
        gradeStrPoint.y = barRect.bottom + barTopBottomMargin - gradeStrPaint.ascent().toInt()
        val barMaxHeight = height - bottomFreeSpaceHeight - topFreeSpaceHeight
        val barAvailableHeight = barMaxHeight - barMinHeight
        val heightPer1 = barAvailableHeight / (maxGradeValue - minGradeValue)
        var i = 0
        val barCenterXInColumn = barRect.centerX()
        for (airQualityObj in airQualityObjList!!) {
            if (airQualityObj.`val` == null) {
                barRect.top = barRect.bottom - barMinHeight
            } else {
                barRect.top = barRect.bottom - barMinHeight - (barAvailableHeight - heightPer1 * (maxGradeValue - airQualityObj.`val`))
            }
            barRect.left = columnWidth * i + barCenterXInColumn - barWidth / 2
            barRect.right = columnWidth * i + barCenterXInColumn + barWidth / 2
            gradeStrPoint.x = barRect.centerX()
            gradeValueIntPoint.x = barRect.centerX()
            gradeValueIntPoint.y = barRect.top - barTopBottomMargin - gradeValueIntPaint.descent().toInt()
            barPaint.color = airQualityObj.gradeColor
            canvas.drawRect(barRect, barPaint)
            canvas.drawText(
                if (airQualityObj.`val` == null) "?" else airQualityObj.`val`.toString(),
                gradeValueIntPoint.x.toFloat(),
                gradeValueIntPoint.y.toFloat(),
                gradeValueIntPaint
            )
            canvas.drawText(airQualityObj.grade!!, gradeStrPoint.x.toFloat(), gradeStrPoint.y.toFloat(), gradeStrPaint)
            i++
        }
    }

    override fun clear() {
        airQualityObjList?.clear()
    }

    class AirQualityObj(val `val`: Int) {
        var gradeColor = 0
        var grade: String? = null
    }
}