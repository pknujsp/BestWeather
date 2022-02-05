package com.lifedawn.bestweather.weathers.detailfragment.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.ItemviewDetailDailyForecastBinding;
import com.lifedawn.bestweather.weathers.detailfragment.dto.GridItemDto;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;

import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DetailDailyForecastViewPagerAdapter extends RecyclerView.Adapter<DetailDailyForecastViewPagerAdapter.ViewHolder> {
	private Context context;
	private LayoutInflater layoutInflater;
	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M.d E");
	private final String zeroPercent = "0%";

	private List<DailyForecastDto> dailyForecastDtoList;

	public DetailDailyForecastViewPagerAdapter(Context context) {
		this.context = context;
		layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setDailyForecastDtoList(List<DailyForecastDto> dailyForecastDtoList) {
		this.dailyForecastDtoList = dailyForecastDtoList;
	}

	@NonNull
	@NotNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
		return new ViewHolder(layoutInflater.inflate(R.layout.itemview_detail_daily_forecast, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull @NotNull DetailDailyForecastViewPagerAdapter.ViewHolder holder, int position) {
		holder.onBind(dailyForecastDtoList.get(position));
	}

	@Override
	public void onViewRecycled(@NonNull ViewHolder holder) {
		holder.clear();
		super.onViewRecycled(holder);
	}

	@Override
	public int getItemCount() {
		return dailyForecastDtoList.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		private ItemviewDetailDailyForecastBinding binding;

		public ViewHolder(@NonNull @NotNull View itemView) {
			super(itemView);
			binding = ItemviewDetailDailyForecastBinding.bind(itemView);
		}

		public void clear() {
			binding.amList.removeAllViews();
			binding.precipitationGridLayout.removeAllViews();
		}

		public void onBind(DailyForecastDto dailyForecastDto) {
			binding.date.setText(dailyForecastDto.getDate().format(dateFormatter));

			binding.minTemp.setText(dailyForecastDto.getMinTemp());
			binding.maxTemp.setText(dailyForecastDto.getMaxTemp());

			List<LabelValueItem> labelValueItemList = new ArrayList<>();

			setPrecipitationGridItems(dailyForecastDto);

			//날씨 아이콘, 최저/최고 기온, 강수확률, 강수량, 강우량, 강설량 설정
			if (dailyForecastDto.isSingle()) {
				binding.timezone.setText(R.string.allDay);

				binding.leftIcon.setImageResource(dailyForecastDto.getSingleValues().getWeatherIcon());
				binding.leftIcon.setVisibility(View.VISIBLE);
				binding.rightIcon.setVisibility(View.GONE);

				binding.weatherDescription.setText(dailyForecastDto.getSingleValues().getWeatherDescription());

				addListItem(labelValueItemList, context.getString(R.string.wind_direction), dailyForecastDto.getSingleValues().getWindDirection());
				addListItem(labelValueItemList, context.getString(R.string.wind_speed), dailyForecastDto.getSingleValues().getWindSpeed());
				addListItem(labelValueItemList, context.getString(R.string.wind_strength), dailyForecastDto.getSingleValues().getWindStrength());
				addListItem(labelValueItemList, context.getString(R.string.wind_gust), dailyForecastDto.getSingleValues().getWindGust());
				addListItem(labelValueItemList, context.getString(R.string.pressure), dailyForecastDto.getSingleValues().getPressure());
				addListItem(labelValueItemList, context.getString(R.string.humidity), dailyForecastDto.getSingleValues().getHumidity());
				addListItem(labelValueItemList, context.getString(R.string.dew_point), dailyForecastDto.getSingleValues().getDewPointTemp());
				addListItem(labelValueItemList, context.getString(R.string.cloud_cover), dailyForecastDto.getSingleValues().getCloudiness());
				addListItem(labelValueItemList, context.getString(R.string.uv_index), dailyForecastDto.getSingleValues().getUvIndex());
			} else {
				binding.timezone.setText(new String(context.getString(R.string.am) + " / " +
						context.getString(R.string.pm)));

				binding.weatherDescription.setText(new String(dailyForecastDto.getAmValues().getWeatherDescription() + " / " +
						dailyForecastDto.getPmValues().getWeatherDescription()));

				binding.leftIcon.setImageResource(dailyForecastDto.getAmValues().getWeatherIcon());
				binding.rightIcon.setImageResource(dailyForecastDto.getPmValues().getWeatherIcon());

				binding.leftIcon.setVisibility(View.VISIBLE);
				binding.rightIcon.setVisibility(View.VISIBLE);

				addListItem(labelValueItemList, context.getString(R.string.wind_direction),
						dailyForecastDto.getAmValues().getWindDirection(), dailyForecastDto.getPmValues().getWindDirection());
				addListItem(labelValueItemList, context.getString(R.string.wind_speed), dailyForecastDto.getAmValues().getWindSpeed(), dailyForecastDto.getPmValues().getWindSpeed());
				addListItem(labelValueItemList, context.getString(R.string.wind_strength), dailyForecastDto.getAmValues().getWindStrength(), dailyForecastDto.getPmValues().getWindStrength());
				addListItem(labelValueItemList, context.getString(R.string.wind_gust), dailyForecastDto.getAmValues().getWindGust(), dailyForecastDto.getPmValues().getWindGust());
				addListItem(labelValueItemList, context.getString(R.string.pressure), dailyForecastDto.getAmValues().getPressure(), dailyForecastDto.getPmValues().getPressure());
				addListItem(labelValueItemList, context.getString(R.string.humidity), dailyForecastDto.getAmValues().getHumidity(), dailyForecastDto.getPmValues().getHumidity());
				addListItem(labelValueItemList, context.getString(R.string.dew_point), dailyForecastDto.getAmValues().getDewPointTemp(), dailyForecastDto.getPmValues().getDewPointTemp());
				addListItem(labelValueItemList, context.getString(R.string.cloud_cover), dailyForecastDto.getAmValues().getCloudiness(), dailyForecastDto.getPmValues().getCloudiness());
				addListItem(labelValueItemList, context.getString(R.string.uv_index), dailyForecastDto.getAmValues().getUvIndex(), dailyForecastDto.getPmValues().getUvIndex());

			}

			for (LabelValueItem labelValueItem : labelValueItemList) {
				View view = layoutInflater.inflate(R.layout.value_itemview, null, false);
				((TextView) view.findViewById(R.id.label)).setText(labelValueItem.label);
				((TextView) view.findViewById(R.id.value)).setText(labelValueItem.value);
				binding.amList.addView(view);
			}

		}

		protected void addGridItems(List<GridItemDto> gridItemDtos) {
			TextView label = null;
			TextView value = null;
			View convertView = null;
			final int blueColor = ContextCompat.getColor(context, R.color.blue);

			for (GridItemDto gridItem : gridItemDtos) {
				convertView = layoutInflater.inflate(R.layout.view_detail_weather_data_item, null, false);

				label = convertView.findViewById(R.id.label);
				value = convertView.findViewById(R.id.value);
				convertView.findViewById(R.id.label_icon).setVisibility(View.GONE);

				label.setText(gridItem.label);
				value.setText(gridItem.value);
				value.setTextColor(blueColor);

				int cellCount = binding.precipitationGridLayout.getChildCount();
				int row = cellCount / binding.precipitationGridLayout.getColumnCount();
				int column = cellCount % binding.precipitationGridLayout.getColumnCount();

				GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();

				layoutParams.columnSpec = GridLayout.spec(column, GridLayout.FILL, 1);
				layoutParams.rowSpec = GridLayout.spec(row, GridLayout.FILL, 1);

				binding.precipitationGridLayout.addView(convertView, layoutParams);
			}
		}

		private void setPrecipitationGridItems(DailyForecastDto dailyForecastDto) {
			List<GridItemDto> gridItemDtoList = new ArrayList<>();

			if (dailyForecastDto.isSingle()) {
				final DailyForecastDto.Values single = dailyForecastDto.getSingleValues();

				if (single.getPop() != null) {
					gridItemDtoList.add(new GridItemDto(context.getString(R.string.probability_of_precipitation), single.getPop(), null));
				}
				if (single.getPor() != null) {
					gridItemDtoList.add(new GridItemDto(context.getString(R.string.probability_of_rain), single.getPor(), null));
				}
				if (single.getPos() != null) {
					gridItemDtoList.add(new GridItemDto(context.getString(R.string.probability_of_snow), single.getPos(), null));
				}
				if (single.isHasPrecipitationVolume()) {
					gridItemDtoList.add(new GridItemDto(context.getString(R.string.precipitation_volume), single.getPrecipitationVolume(), null));
				}
				if (single.isHasRainVolume()) {
					gridItemDtoList.add(new GridItemDto(context.getString(R.string.rain_volume), single.getRainVolume(), null));
				}
				if (single.isHasSnowVolume()) {
					gridItemDtoList.add(new GridItemDto(context.getString(R.string.snow_volume), single.getSnowVolume(), null));
				}
			} else {
				final DailyForecastDto.Values am = dailyForecastDto.getAmValues();
				final DailyForecastDto.Values pm = dailyForecastDto.getPmValues();
				final String divider = " / ";

				if (am.getPop() != null || pm.getPop() != null) {
					gridItemDtoList.add(new GridItemDto(context.getString(R.string.probability_of_precipitation), am.getPop() + divider + pm.getPop(),
							null));
				}
				if (am.getPor() != null || pm.getPor() != null) {
					gridItemDtoList.add(new GridItemDto(context.getString(R.string.probability_of_rain), am.getPor() + divider + pm.getPor(),
							null));
				}
				if (am.getPos() != null || pm.getPos() != null) {
					gridItemDtoList.add(new GridItemDto(context.getString(R.string.probability_of_snow), am.getPos() + divider + pm.getPos(), null));
				}
				if (am.isHasPrecipitationVolume()) {
					gridItemDtoList.add(new GridItemDto(context.getString(R.string.precipitation_volume),
							am.getPrecipitationVolume() + divider + pm.getPrecipitationVolume(),
							null));
				}
				if (am.isHasRainVolume()) {
					gridItemDtoList.add(new GridItemDto(context.getString(R.string.rain_volume),
							am.getRainVolume() + divider + pm.getRainVolume(), null));
				}
				if (am.isHasSnowVolume()) {
					gridItemDtoList.add(new GridItemDto(context.getString(R.string.snow_volume),
							am.getSnowVolume() + divider + pm.getSnowVolume(), null));
				}
			}

			addGridItems(gridItemDtoList);
		}
	}


	protected void addListItem(List<LabelValueItem> list, String label, @Nullable String val) {
		if (val != null) {
			list.add(new LabelValueItem(label, val));
		}
	}

	protected void addListItem(List<LabelValueItem> list, String label, @Nullable String val1, @Nullable String val2) {
		if (val1 != null || val2 != null) {
			list.add(new LabelValueItem(label, val1 + " / " + val2));
		}
	}


	protected static class LabelValueItem {
		private String label;
		private String value;

		public LabelValueItem(String label, String value) {
			this.label = label;
			this.value = value;
		}

		public String getLabel() {
			return label;
		}

		public LabelValueItem setLabel(String label) {
			this.label = label;
			return this;
		}

		public String getValue() {
			return value;
		}

		public LabelValueItem setValue(String value) {
			this.value = value;
			return this;
		}
	}
}
