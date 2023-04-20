package com.lifedawn.bestweather.ui.weathers.customview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.StaticLayout
import android.text.TextPaint
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.ui.theme.AppTheme.getTextColor
import com.lifedawn.bestweather.ui.weathers.enums.WeatherDataType

class IconTextView(
    context: Context, private val weatherDataType: WeatherDataType, private val viewWidth: Int, private val columnWidth: Int,
    iconId: Int
) : View(context), ICleaner {
    private val valueTextPaint: TextPaint
    private var iconSize = 0
    private val iconRect = Rect()
    private val spacingBetweenIconAndValue: Int
    private var viewHeight = 0
    private val icon: Drawable?
    private var valueList: MutableList<String>? = null
    private var visibleList: MutableList<Boolean>? = null
    private val textRect = Rect()
    private val padding: Int

    init {
        valueTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        valueTextPaint.textAlign = Paint.Align.CENTER
        valueTextPaint.textSize = context.resources.getDimension(R.dimen.iconValueTextSizeInSCD)
        valueTextPaint.color = getTextColor(weatherDataType)
        valueTextPaint.typeface = Typeface.create("sans-serif-condensed", Typeface.NORMAL)
        icon = ContextCompat.getDrawable(context, iconId)
        icon!!.setTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue)))
        spacingBetweenIconAndValue = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, resources.displayMetrics).toInt()
        padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics).toInt()
    }

    fun setValueList(valueList: MutableList<String>?): IconTextView {
        this.valueList = valueList
        return this
    }

    fun setVisibleList(visibleList: MutableList<Boolean>?): IconTextView {
        this.visibleList = visibleList
        return this
    }

    fun setValueTextSize(textSizeSp: Int) {
        valueTextPaint.textSize =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSp.toFloat(), resources.displayMetrics)
    }

    fun setTextColor(textColor: Int) {
        valueTextPaint.color = textColor
    }

    fun getValueList(): List<String>? {
        return valueList
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        iconSize = valueTextPaint.textSize.toInt()
        var staticLayout: StaticLayout? = null
        viewHeight = Int.MIN_VALUE
        val availableTextWidth = columnWidth - iconSize - spacingBetweenIconAndValue
        for (`val` in valueList!!) {
            val builder = StaticLayout.Builder.obtain(`val`, 0, `val`.length, valueTextPaint, availableTextWidth)
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
        iconRect.top = height / 2 - iconSize / 2
        iconRect.bottom = iconRect.top + iconSize
        var staticLayout: StaticLayout? = null
        val availableTextWidth = columnWidth - iconSize - spacingBetweenIconAndValue
        var moveDistance: Int
        val textRect = Rect()
        val tab = "\n"
        var separatedStr: String
        var idx = 0
        for (value in valueList!!) {
            separatedStr = value.split(tab.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            valueTextPaint.getTextBounds(separatedStr, 0, separatedStr.length, textRect)
            val builder = StaticLayout.Builder.obtain(value, 0, value.length, valueTextPaint, availableTextWidth)
            staticLayout = builder.build()
            moveDistance = (columnWidth - textRect.width()) / 4
            val x = column * columnWidth + columnWidth / 2f + moveDistance
            val y = viewHeight / 2f - staticLayout.height / 2f
            iconRect.right = (x - spacingBetweenIconAndValue - textRect.width() / 2).toInt()
            iconRect.left = iconRect.right - iconSize
            icon!!.bounds = iconRect
            if (visibleList == null || visibleList!![idx]) {
                icon.draw(canvas)
                canvas.save()
                canvas.translate(x, y)
                staticLayout.draw(canvas)
                canvas.restore()
            }
            column++
            idx++
        }
    }

    override fun clear() {
        if (valueList != null) valueList!!.clear()
        if (visibleList != null) visibleList!!.clear()
    }
}