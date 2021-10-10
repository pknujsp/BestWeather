package com.lifedawn.bestweather.settings.custompreferences;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.retrofit.responses.accuweather.ValueUnit;

import java.util.Locale;

public class UnitPreference extends Preference {
	private TextView unitTextView;
	private ValueUnits valueUnit;

	public UnitPreference(Context context) {
		super(context);
	}

	@Override
	public void onBindViewHolder(PreferenceViewHolder holder) {
		super.onBindViewHolder(holder);
		if (unitTextView == null) {
			unitTextView = new TextView(getContext());
			unitTextView.setText(ValueUnits.convertToStr(getContext(), valueUnit));
			unitTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
			unitTextView.setTextColor(Color.BLACK);

			int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, getContext().getResources().getDisplayMetrics());
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height);
			layoutParams.gravity = Gravity.RIGHT;

			unitTextView.setLayoutParams(layoutParams);

			LinearLayout viewGroup = (LinearLayout) holder.findViewById(R.id.layout_widget_root);
			viewGroup.addView(unitTextView);
		} else {
			LinearLayout viewGroup = (LinearLayout) holder.findViewById(R.id.layout_widget_root);
			if (viewGroup.getChildCount() == 0) {
				ViewGroup parent = (ViewGroup) unitTextView.getParent();
				parent.removeView(unitTextView);
				viewGroup.addView(unitTextView);
			}
		}
	}

	public void setUnit(ValueUnits valueUnit) {
		this.valueUnit = valueUnit;
		if (unitTextView != null) {
			unitTextView.setText(ValueUnits.convertToStr(getContext(), valueUnit));
		}
	}

	public ValueUnits getUnit() {
		return valueUnit;
	}
}