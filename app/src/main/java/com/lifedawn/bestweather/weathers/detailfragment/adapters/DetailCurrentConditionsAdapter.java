package com.lifedawn.bestweather.weathers.detailfragment.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.weathers.detailfragment.dto.GridItemDto;

import org.jetbrains.annotations.NotNull;

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
			view = layoutInflater.inflate(R.layout.current_conditions_detail_item, viewGroup, false);
		}

		GridItemDto gridItemDto = gridItemDtoList.get(i);
		if (gridItemDto.img == null) {
			view.findViewById(R.id.value_img).setVisibility(View.GONE);
		} else {
			view.findViewById(R.id.value_img).setVisibility(View.VISIBLE);
			((ImageView) view.findViewById(R.id.value_img)).setImageDrawable(gridItemDto.img);
		}
		((TextView) view.findViewById(R.id.label)).setText(gridItemDto.label);
		((TextView) view.findViewById(R.id.value)).setText(gridItemDto.value);

		return view;
	}
}
