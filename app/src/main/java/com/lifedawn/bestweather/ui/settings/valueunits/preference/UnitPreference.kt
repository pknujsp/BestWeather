package com.lifedawn.bestweather.ui.settings.valueunits.preference

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

class UnitPreference(context: Context) : Preference(context) {
    private var unitTextView: TextView? = null
    private var valueUnit: ValueUnits = ValueUnits.undefined

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        if (unitTextView == null)
            unitTextView = TextView(context)

        unitTextView?.apply {
            text = valueUnit.text
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(Color.BLACK)

            val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            layoutParams.gravity = Gravity.CENTER_VERTICAL
            this.layoutParams = layoutParams

            val viewGroup = holder.findViewById(R.id.layout_widget_root) as LinearLayout
            if (viewGroup.childCount == 0) {
                viewGroup.addView(this)
            }
        }
    }

    var unit: ValueUnits
        get() = valueUnit
        set(valueUnit) {
            this.valueUnit = valueUnit
            unitTextView?.apply {
                text = valueUnit.text
            }
        }
}