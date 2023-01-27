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
import com.lifedawn.bestweather.commons.constants.ValueUnits

class UnitPreference(context: Context?) : Preference(context!!) {
    private var unitTextView: TextView? = null
    private var valueUnit: ValueUnits? = null
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        if (unitTextView == null) {
            unitTextView = TextView(context)
            unitTextView!!.text = ValueUnits.toString(valueUnit)
            unitTextView!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            unitTextView!!.setTextColor(Color.BLACK)
            val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            layoutParams.gravity = Gravity.CENTER_VERTICAL
            unitTextView!!.layoutParams = layoutParams
            val viewGroup = holder.findViewById(R.id.layout_widget_root) as LinearLayout
            viewGroup.addView(unitTextView)
        } else {
            val viewGroup = holder.findViewById(R.id.layout_widget_root) as LinearLayout
            if (viewGroup.childCount == 0) {
                val parent = unitTextView!!.parent as ViewGroup
                parent.removeView(unitTextView)
                viewGroup.addView(unitTextView)
            }
        }
    }

    var unit: ValueUnits?
        get() = valueUnit
        set(valueUnit) {
            this.valueUnit = valueUnit
            if (unitTextView != null) {
                unitTextView!!.text = ValueUnits.toString(valueUnit)
            }
        }
}