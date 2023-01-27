package com.lifedawn.bestweather.ui.weathers.view

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.TypedValue
import android.view.View
import com.lifedawn.bestweather.R

class DetailSingleTemperatureView(context: Context?, tempList: List<Int>?) : View(context), ICleaner {
    private val tempPaint: TextPaint
    private val linePaint: Paint
    private val circlePaint: Paint
    private val circleRadius: Int
    private val tempUnit = "°"
    private val textRect = Rect()
    private val tempList: MutableList<Int> = ArrayList()
    private val minTemp: Int
    private val maxTemp: Int

    init {
        circleRadius = resources.getDimension(R.dimen.circleRadiusInDoubleTemperature).toInt()
        tempPaint = TextPaint()
        tempPaint.textAlign = Paint.Align.CENTER
        tempPaint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, resources.displayMetrics)
        tempPaint.color = Color.BLACK
        linePaint = Paint()
        linePaint.isAntiAlias = true
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.3f, resources.displayMetrics)
        linePaint.color = Color.DKGRAY
        circlePaint = Paint()
        circlePaint.isAntiAlias = true
        circlePaint.style = Paint.Style.FILL
        circlePaint.color = Color.DKGRAY
        this.tempList.addAll(tempList!!)
        var min = Int.MAX_VALUE
        var max = Int.MIN_VALUE
        var temp = 0
        for (i in this.tempList.indices) {
            temp = this.tempList[i]
            if (temp >= max) {
                max = temp
            }
            if (temp <= min) {
                min = temp
            }
        }
        minTemp = min
        maxTemp = max
        setWillNotDraw(false)
    }

    fun setTempTextSizeSp(textSizeSp: Int) {
        tempPaint.textSize =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSp.toFloat(), resources.displayMetrics)
    }

    fun setTempTextSizePx(textSize: Int) {
        tempPaint.textSize = textSize.toFloat()
    }

    fun setTextColor(textColor: Int) {
        tempPaint.color = textColor
    }

    fun setLineColor(color: Int) {
        linePaint.color = color
    }

    fun setCircleColor(color: Int) {
        circlePaint.color = color
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val test = "20°"
        tempPaint.getTextBounds(test, 0, test.length, textRect)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
    }

    override fun onDraw(canvas: Canvas) {
        drawGraph(canvas)
    }

    private fun drawGraph(canvas: Canvas) {
        val tempsCount = tempList.size
        val textHeight = textRect.height().toFloat()
        val margin = circleRadius * 2f
        val spacingBetweenTextAndCircle = (circleRadius * 3).toFloat()
        val viewHeight = height - margin * 2 - textHeight - spacingBetweenTextAndCircle
        val spacing = viewHeight / (maxTemp - minTemp)
        val columnWidth = width / tempsCount
        var temp = 0
        var x = 0f
        var y = 0f
        val lastColumnPoint = PointF()
        val circleXArr = FloatArray(tempsCount)
        val circleYArr = FloatArray(tempsCount)
        val linePointList: MutableList<PointF> = ArrayList()
        for (index in 0 until tempsCount) {
            temp = tempList[index]
            x = columnWidth / 2f + columnWidth * index
            y = if (minTemp == maxTemp) height / 2f else (maxTemp - temp) * spacing + margin + textHeight + spacingBetweenTextAndCircle
            canvas.drawText(temp.toString() + tempUnit, x, y - spacingBetweenTextAndCircle + tempPaint.descent(), tempPaint)
            lastColumnPoint[x] = y
            circleXArr[index] = x
            circleYArr[index] = y
            linePointList.add(PointF(lastColumnPoint.x, lastColumnPoint.y))
        }
        val path = Path()
        path.moveTo(linePointList[0].x, linePointList[0].y)
        val points1 = arrayOfNulls<PointF>(tempsCount)
        val points2 = arrayOfNulls<PointF>(tempsCount)
        for (i in 1 until tempsCount) {
            points1[i] = PointF((linePointList[i].x + linePointList[i - 1].x) / 2, linePointList[i - 1].y)
            points2[i] = PointF((linePointList[i].x + linePointList[i - 1].x) / 2, linePointList[i].y)
            path.cubicTo(
                points1[i]!!.x, points1[i]!!.y, points2[i]!!.x, points2[i]!!.y, linePointList[i].x,
                linePointList[i].y
            )
        }
        canvas.drawPath(path, linePaint)
        for (i in circleXArr.indices) {
            canvas.drawCircle(circleXArr[i], circleYArr[i], circleRadius.toFloat(), circlePaint)
        }
    }

    override fun clear() {
        tempList.clear()
    }
}