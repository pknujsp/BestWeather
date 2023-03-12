package com.lifedawn.bestweather.ui.weathers.simple.sunsetrise

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.SunRiseSetType
import com.lifedawn.bestweather.commons.constants.ValueUnits
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class SunSetRiseInfoView(context: Context?, dateTime: ZonedDateTime, sunSetRiseType: SunRiseSetType) : View(context) {
    private var typeIcon: Drawable? = null
    var dateTime: ZonedDateTime? = null
        private set
    private var type: String? = null
    private var dateTimeFormatter: DateTimeFormatter? = null
    private var typeTextPaint: TextPaint? = null
    private var timeTextPaint: TextPaint? = null
    private var iconRect: Rect? = null
    private var typeTextRect: Rect? = null
    private var timeTextRect: Rect? = null
    var pixelWidth = 0
        private set
    var pixelHeight = 0
        private set

    init {
        init(dateTime, sunSetRiseType)
    }

    private fun init(dateTime: ZonedDateTime, sunRiseSetType: SunRiseSetType) {
        this.dateTime = dateTime
        typeIcon = ContextCompat.getDrawable(context, if (sunRiseSetType === SunRiseSetType.RISE) R.drawable.sunrise else R.drawable.sunset)
        type = context.getString(if (sunRiseSetType === SunRiseSetType.RISE) R.string.sunrise else R.string.sunset)
        typeTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        typeTextPaint!!.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15f, resources.displayMetrics)
        typeTextPaint!!.color = Color.WHITE
        timeTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        timeTextPaint!!.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, resources.displayMetrics)
        timeTextPaint!!.color = Color.WHITE
        typeTextRect = Rect()
        timeTextRect = Rect()
        typeTextPaint!!.getTextBounds(type, 0, type!!.length, typeTextRect)
        val clockUnit = ValueUnits.valueOf(
            PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.pref_key_unit_clock),
                ValueUnits.clock12.name
            )!!
        )
        dateTimeFormatter = DateTimeFormatter.ofPattern(
            if (clockUnit === ValueUnits.clock12) context.getString(R.string.datetime_pattern_clock12) else context.getString(
                R.string.datetime_pattern_clock24
            )
        )
        val dateTimeStr = this.dateTime!!.format(dateTimeFormatter)
        timeTextPaint!!.getTextBounds(dateTimeStr, 0, dateTimeStr.length, timeTextRect)
        val typeTextLeftMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
        val timeTextTopMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics).toInt()
        val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics).toInt()
        val iconSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 42f, resources.displayMetrics).toInt()
        iconRect = Rect(padding, padding, padding + iconSize, padding + iconSize)
        typeTextRect!!.offsetTo(iconRect!!.right + typeTextLeftMargin, iconRect!!.centerY() + typeTextRect!!.height() / 2)
        timeTextRect!!.offsetTo(iconRect!!.left, iconRect!!.bottom + timeTextTopMargin + timeTextRect!!.height() / 2)
        typeIcon!!.bounds = iconRect!!
        pixelWidth = Math.max(typeTextRect!!.right, timeTextRect!!.right) + padding
        pixelHeight = timeTextRect!!.bottom + padding
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(pixelWidth, pixelHeight)
    }

    override fun onDraw(canvas: Canvas) {
        typeIcon!!.draw(canvas)
        canvas.drawText(type!!, typeTextRect!!.left.toFloat(), typeTextRect!!.top.toFloat(), typeTextPaint!!)
        canvas.drawText(dateTime!!.format(dateTimeFormatter), timeTextRect!!.left.toFloat(), timeTextRect!!.top.toFloat(), timeTextPaint!!)
    }
}