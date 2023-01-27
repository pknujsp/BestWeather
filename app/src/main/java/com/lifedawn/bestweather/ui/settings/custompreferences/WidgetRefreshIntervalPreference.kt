package com.lifedawn.bestweather.ui.settings.custompreferences

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.lifedawn.bestweather.R

class WidgetRefreshIntervalPreference(context: Context) : Preference(context) {
    private var textView: TextView? = null
    val widgetRefreshIntervalLongValues: Array<Long?>
    var currentValue: Long? = null
        private set
    var currentValueIndex = 0
        private set
    val widgetRefreshIntervalTexts: Array<String>

    init {
        val values = context.resources.getStringArray(R.array.AutoRefreshIntervalsLong)
        widgetRefreshIntervalLongValues = arrayOfNulls(values.size)
        var i = 0
        for (v in values) {
            widgetRefreshIntervalLongValues[i++] = v.toLong()
        }
        widgetRefreshIntervalTexts = context.resources.getStringArray(R.array.AutoRefreshIntervals)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        if (textView == null) {
            textView = TextView(context)
            textView!!.text = widgetRefreshIntervalTexts[currentValueIndex]
            textView!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            textView!!.setTextColor(Color.BLACK)
            val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            layoutParams.gravity = Gravity.CENTER_VERTICAL
            textView!!.layoutParams = layoutParams
            val viewGroup = holder.findViewById(R.id.layout_widget_root) as LinearLayout
            viewGroup.addView(textView)
        } else {
            val viewGroup = holder.findViewById(R.id.layout_widget_root) as LinearLayout
            if (viewGroup.childCount == 0) {
                val parent = textView!!.parent as ViewGroup
                parent.removeView(textView)
                viewGroup.addView(textView)
            }
        }
    }

    fun setValue(refreshInterval: Long) {
        currentValue = refreshInterval
        var idx = 0
        for (v in widgetRefreshIntervalLongValues) {
            if (refreshInterval == v) {
                break
            }
            idx++
        }
        currentValueIndex = idx
        if (textView != null) {
            textView!!.text = widgetRefreshIntervalTexts[currentValueIndex]
        }
    }
}