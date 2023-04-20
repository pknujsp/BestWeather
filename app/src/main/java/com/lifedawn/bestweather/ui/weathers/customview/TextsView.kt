package com.lifedawn.bestweather.ui.weathers.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.StaticLayout
import android.text.TextPaint
import android.util.TypedValue
import android.view.View

class TextsView(
    context: Context?,
    protected val viewWidth: Int,
    protected val columnWidth: Int,
    protected var valueList: MutableList<String>?
) : View(context), ICleaner {
    protected val valueTextPaint: TextPaint
    protected val valueTextRect = Rect()
    protected var viewHeight = 0
    protected var padding = 0

    init {
        valueTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        valueTextPaint.textAlign = Paint.Align.CENTER
        valueTextPaint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, resources.displayMetrics)
        valueTextPaint.color = Color.WHITE
    }

    fun setValueTextSize(textSizeSp: Int) {
        valueTextPaint.textSize =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSp.toFloat(), resources.displayMetrics)
                .toInt().toFloat()
    }

    fun setValueTextColor(textColor: Int) {
        valueTextPaint.color = textColor
    }

    fun setValueList(valueList: MutableList<String>?) {
        this.valueList = valueList
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var staticLayout: StaticLayout? = null
        viewHeight = Int.MIN_VALUE
        for (`val` in valueList!!) {
            val builder = StaticLayout.Builder.obtain(`val`, 0, `val`.length, valueTextPaint, columnWidth)
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
        var column = 0
        for (value in valueList!!) {
            drawText(canvas, value, column)
            column++
        }
    }

    private fun drawText(canvas: Canvas, textOnCanvas: String, column: Int) {
        val builder = StaticLayout.Builder.obtain(textOnCanvas, 0, textOnCanvas.length, valueTextPaint, columnWidth)
        val staticLayout = builder.build()
        val x = columnWidth / 2f + columnWidth * column + valueTextRect.left
        val y = viewHeight / 2f - staticLayout.height / 2f
        canvas.save()
        canvas.translate(x, y)
        staticLayout.draw(canvas)
        canvas.restore()
    }

    override fun clear() {
        if (valueList != null) valueList!!.clear()
    }
}