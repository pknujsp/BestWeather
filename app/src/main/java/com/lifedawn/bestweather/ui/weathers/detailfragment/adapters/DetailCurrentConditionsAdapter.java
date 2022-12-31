package com.lifedawn.bestweather.ui.weathers.detailfragment.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.ui.weathers.detailfragment.dto.GridItemDto;

import java.util.List;

public class DetailCurrentConditionsAdapter extends BaseAdapter {
	private List<GridItemDto> gridItemDtoList;
	private Context context;
	private LayoutInflater layoutInflater;

	public DetailCurrentConditionsAdapter(Context context) {
		this.context = context;
		this.layoutInflater = LayoutInflater.from(context);
	}

	public DetailCurrentConditionsAdapter setGridItemDtoList(List<GridItemDto> gridItemDtoList) {
		this.gridItemDtoList = gridItemDtoList;
		return this;
	}

	@Override
	public int getCount() {
		return gridItemDtoList.size();
	}

	@Override
	public Object getItem(int i) {
		return null;
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (view == null) {
			view = layoutInflater.inflate(R.layout.view_detail_weather_data_item, viewGroup, false);
		}

		GridItemDto gridItemDto = gridItemDtoList.get(i);
		if (gridItemDto.img == null) {
			view.findViewById(R.id.label_icon).setVisibility(View.GONE);
		} else {
			view.findViewById(R.id.label_icon).setVisibility(View.VISIBLE);
			((ImageView) view.findViewById(R.id.label_icon)).setImageDrawable(gridItemDto.img);
		}
		((TextView) view.findViewById(R.id.label)).setText(gridItemDto.label);
		((TextView) view.findViewById(R.id.label)).setTextColor(Color.WHITE);
		((TextView) view.findViewById(R.id.value)).setText(gridItemDto.value);
		((TextView) view.findViewById(R.id.value)).setTextColor(Color.WHITE);

		return view;
	}
}
