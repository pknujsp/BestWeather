package com.lifedawn.bestweather.ui.weathers.view

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.TypedValue
import android.view.View
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.ui.theme.AppTheme.getTextColor
import com.lifedawn.bestweather.ui.weathers.FragmentType

class DetailDoubleTemperatureView(
    context: Context,
    private val fragmentType: FragmentType,
    private val viewWidth: Int,
    private val viewHeight: Int,
    private val columnWidth: Int,
    minTempList: List<Int>?,
    maxTempList: List<Int>?
) : View(context), ICleaner {
    private val maxTemp: Int
    private val minTemp: Int
    private val tempPaint: TextPaint
    private val linePaint: Paint
    private val maxCirclePaint: Paint
    private val minCirclePaint: Paint
    private val circleRadius: Int
    private val tempUnit: String
    private val maxTempList: MutableList<Int> = ArrayList()
    private val minTempList: MutableList<Int> = ArrayList()

    init {
        circleRadius = resources.getDimension(R.dimen.circleRadiusInDoubleTemperature).toInt()
        tempPaint = TextPaint()
        tempPaint.textAlign = Paint.Align.CENTER
        tempPaint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, resources.displayMetrics)
        tempPaint.color = getTextColor(fragmentType)
        linePaint = Paint()
        linePaint.isAntiAlias = true
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.3f, resources.displayMetrics)
        linePaint.color = getTextColor(fragmentType)
        maxCirclePaint = Paint()
        maxCirclePaint.isAntiAlias = true
        maxCirclePaint.style = Paint.Style.FILL
        maxCirclePaint.color = Color.RED
        minCirclePaint = Paint()
        minCirclePaint.isAntiAlias = true
        minCirclePaint.style = Paint.Style.FILL
        minCirclePaint.color = Color.BLUE
        this.minTempList.addAll(minTempList!!)
        this.maxTempList.addAll(maxTempList!!)
        var max = Int.MIN_VALUE
        var min = Int.MAX_VALUE
        var maxTemp = 0
        var minTemp = 0
        for (i in this.minTempList.indices) {
            maxTemp = this.maxTempList[i]
            minTemp = this.minTempList[i]

            // 최대,최소 기온 구하기
            if (maxTemp >= max) {
                max = maxTemp
            }
            if (minTemp <= min) {
                min = minTemp
            }
        }
        this.maxTemp = max
        this.minTemp = min
        tempUnit = context.getString(R.string.degree_symbol)
        setWillNotDraw(false)
    }

    fun setTempTextSize(textSizeSp: Int) {
        tempPaint.textSize =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSp.toFloat(), resources.displayMetrics)
    }

    fun setTextColor(textColor: Int) {
        tempPaint.color = textColor
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(viewWidth, viewHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
    }

    override fun onDraw(canvas: Canvas) {
        drawGraph(canvas)
    }

    private fun drawGraph(canvas: Canvas) {
        // 텍스트의 높이+원의 반지름 만큼 뷰의 상/하단에 여백을 설정한다.
        val TEXT_HEIGHT = tempPaint.descent() - tempPaint.ascent()
        val TEXT_ASCENT = -tempPaint.ascent()
        val VIEW_HEIGHT = height - (TEXT_HEIGHT + circleRadius) * 2
        val SPACING = VIEW_HEIGHT / (maxTemp - minTemp) / 10f
        var min = 0
        var max = 0
        var x = 0f
        var minY = 0f
        var maxY = 0f
        val lastMinColumnPoint = PointF()
        val lastMaxColumnPoint = PointF()
        val minCircleXArr = FloatArray(minTempList.size)
        val minCircleYArr = FloatArray(minTempList.size)
        val maxCircleXArr = FloatArray(minTempList.size)
        val maxCircleYArr = FloatArray(minTempList.size)
        val minLinePointList: MutableList<PointF> = ArrayList()
        val maxLinePointList: MutableList<PointF> = ArrayList()
        val tempsCount = minTempList.size
        for (index in 0 until tempsCount) {
            min = minTempList[index]
            max = maxTempList[index]
            x = columnWidth / 2f + columnWidth * index
            minY = 10f * (maxTemp - min) * SPACING + TEXT_HEIGHT + circleRadius
            maxY = 10f * (maxTemp - max) * SPACING + TEXT_HEIGHT + circleRadius
            canvas.drawText(minTempList[index].toString() + tempUnit, x, minY + circleRadius + TEXT_HEIGHT, tempPaint)
            canvas.drawText(maxTempList[index].toString() + tempUnit, x, maxY - circleRadius - TEXT_HEIGHT + TEXT_ASCENT, tempPaint)
            lastMinColumnPoint[x] = minY
            lastMaxColumnPoint[x] = maxY
            minCircleXArr[index] = x
            minCircleYArr[index] = minY
            maxCircleXArr[index] = x
            maxCircleYArr[index] = maxY
            minLinePointList.add(PointF(lastMinColumnPoint.x, lastMinColumnPoint.y))
            maxLinePointList.add(PointF(lastMaxColumnPoint.x, lastMaxColumnPoint.y))
        }
        val minPoints1 = arrayOfNulls<PointF>(tempsCount)
        val minPoints2 = arrayOfNulls<PointF>(tempsCount)
        val maxPoints1 = arrayOfNulls<PointF>(tempsCount)
        val maxPoints2 = arrayOfNulls<PointF>(tempsCount)
        val minPath = Path()
        val maxPath = Path()
        minPath.moveTo(minLinePointList[0].x, minLinePointList[0].y)
        maxPath.moveTo(maxLinePointList[0].x, maxLinePointList[0].y)
        for (i in 1 until tempsCount) {
            minPoints1[i] = PointF((minLinePointList[i].x + minLinePointList[i - 1].x) / 2, minLinePointList[i - 1].y)
            minPoints2[i] = PointF((minLinePointList[i].x + minLinePointList[i - 1].x) / 2, minLinePointList[i].y)
            maxPoints1[i] = PointF((maxLinePointList[i].x + maxLinePointList[i - 1].x) / 2, maxLinePointList[i - 1].y)
            maxPoints2[i] = PointF((maxLinePointList[i].x + maxLinePointList[i - 1].x) / 2, maxLinePointList[i].y)
            minPath.cubicTo(
                minPoints1[i]!!.x, minPoints1[i]!!.y, minPoints2[i]!!.x, minPoints2[i]!!.y, minLinePointList[i].x,
                minLinePointList[i].y
            )
            maxPath.cubicTo(
                maxPoints1[i]!!.x, maxPoints1[i]!!.y, maxPoints2[i]!!.x, maxPoints2[i]!!.y, maxLinePointList[i].x,
                maxLinePointList[i].y
            )
        }
        canvas.drawPath(minPath, linePaint)
        canvas.drawPath(maxPath, linePaint)
        for (i in minCircleXArr.indices) {
            canvas.drawCircle(minCircleXArr[i], minCircleYArr[i], circleRadius.toFloat(), minCirclePaint)
            canvas.drawCircle(minCircleXArr[i], maxCircleYArr[i], circleRadius.toFloat(), maxCirclePaint)
        }
    }

    override fun clear() {
        maxTempList.clear()
        minTempList.clear()
    }
}