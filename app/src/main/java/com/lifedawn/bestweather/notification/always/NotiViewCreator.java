package com.lifedawn.bestweather.notification.always;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.gridlayout.widget.GridLayout;

import com.lifedawn.bestweather.R;

public class NotiViewCreator {
	private LayoutInflater layoutInflater;

	public final View addHourlyForecastItem(int labelDescriptionId, String value, @NonNull Integer labelIconId, Integer valueIconId) {
		View gridItem = layoutInflater.inflate(R.layout.view_hourly_forecast_item_in_widget, null, false);

		((ImageView) gridItem.findViewById(R.id.label_icon)).setImageResource(labelIconId);
		((TextView) gridItem.findViewById(R.id.label)).setText(labelDescriptionId);
		((TextView) gridItem.findViewById(R.id.value)).setText(value);
		if (valueIconId == null) {
			gridItem.findViewById(R.id.value_img).setVisibility(View.GONE);
		} else {
			((ImageView) gridItem.findViewById(R.id.value_img)).setImageResource(valueIconId);
		}

		int cellCount = binding.conditionsGrid.getChildCount();
		int row = cellCount / 4;
		int column = cellCount % 4;

		GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();

		layoutParams.columnSpec = GridLayout.spec(column, GridLayout.FILL, 1);
		layoutParams.rowSpec = GridLayout.spec(row, GridLayout.FILL, 1);

		binding.conditionsGrid.addView(gridItem, layoutParams);


		return gridItem;
	}
}
