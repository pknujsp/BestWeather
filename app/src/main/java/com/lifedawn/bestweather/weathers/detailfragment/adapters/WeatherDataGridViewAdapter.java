package com.lifedawn.bestweather.weathers.detailfragment.adapters;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.weathers.detailfragment.dto.GridItemDto;

import java.util.ArrayList;
import java.util.List;

public class WeatherDataGridViewAdapter extends BaseAdapter {
	private List<GridItemDto> gridItemList = new ArrayList<>();
	private Context context;

	public WeatherDataGridViewAdapter(Context context) {
		this.context = context;
	}

	public WeatherDataGridViewAdapter setGridItemList(List<GridItemDto> gridItemList) {
		this.gridItemList = gridItemList;
		return this;
	}

	public void addItem(GridItemDto gridItemDto) {
		gridItemList.add(gridItemDto);
	}

	@Override
	public int getCount() {
		return gridItemList.size();
	}

	@Override
	public Object getItem(int position) {
		return gridItemList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.grid_itemview_detail_forecast, parent, false);
		}

		TextView label = convertView.findViewById(R.id.label);
		TextView value = convertView.findViewById(R.id.value);
		ImageView icon = convertView.findViewById(R.id.label_icon);

		GridItemDto gridItem = gridItemList.get(position);

		label.setText(gridItem.label);
		value.setText(gridItem.value);
		icon.setImageDrawable(gridItem.img);
		if (gridItem.imgRotate != null) {
			icon.setRotation(gridItem.imgRotate);
		}

		return convertView;
	}
}
