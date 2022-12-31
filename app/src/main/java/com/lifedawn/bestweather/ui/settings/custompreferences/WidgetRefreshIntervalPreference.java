package com.lifedawn.bestweather.ui.settings.custompreferences;

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

public class WidgetRefreshIntervalPreference extends Preference {
	private TextView textView;
	private Long[] widgetRefreshIntervalLongValues;
	private Long currentValue;
	private int currentValueIndex;
	private String[] widgetRefreshIntervalTexts;

	public WidgetRefreshIntervalPreference(Context context) {
		super(context);
		String[] values = context.getResources().getStringArray(R.array.AutoRefreshIntervalsLong);
		widgetRefreshIntervalLongValues = new Long[values.length];
		int i = 0;

		for (String v : values) {
			widgetRefreshIntervalLongValues[i++] = Long.parseLong(v);
		}
		widgetRefreshIntervalTexts = context.getResources().getStringArray(R.array.AutoRefreshIntervals);
	}

	@Override
	public void onBindViewHolder(PreferenceViewHolder holder) {
		super.onBindViewHolder(holder);
		if (textView == null) {
			textView = new TextView(getContext());
			textView.setText(widgetRefreshIntervalTexts[currentValueIndex]);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
			textView.setTextColor(Color.BLACK);

			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
			layoutParams.gravity = Gravity.CENTER_VERTICAL;

			textView.setLayoutParams(layoutParams);

			LinearLayout viewGroup = (LinearLayout) holder.findViewById(R.id.layout_widget_root);
			viewGroup.addView(textView);
		} else {
			LinearLayout viewGroup = (LinearLayout) holder.findViewById(R.id.layout_widget_root);
			if (viewGroup.getChildCount() == 0) {
				ViewGroup parent = (ViewGroup) textView.getParent();
				parent.removeView(textView);
				viewGroup.addView(textView);
			}
		}
	}

	public void setValue(Long refreshInterval) {
		this.currentValue = refreshInterval;

		int idx = 0;
		for (Long v : widgetRefreshIntervalLongValues) {
			if (refreshInterval.equals(v)) {
				break;
			}
			idx++;
		}
		currentValueIndex = idx;

		if (textView != null) {
			textView.setText(widgetRefreshIntervalTexts[currentValueIndex]);
		}
	}

	public Long getCurrentValue() {
		return currentValue;
	}

	public Long[] getWidgetRefreshIntervalLongValues() {
		return widgetRefreshIntervalLongValues;
	}

	public String[] getWidgetRefreshIntervalTexts() {
		return widgetRefreshIntervalTexts;
	}

	public int getCurrentValueIndex() {
		return currentValueIndex;
	}
}