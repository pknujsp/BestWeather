package com.lifedawn.bestweather.weathers.detailfragment.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.WeatherValueLabels;
import com.lifedawn.bestweather.commons.constants.WeatherValueType;
import com.lifedawn.bestweather.databinding.ItemviewDetailDailyForecastBinding;
import com.lifedawn.bestweather.weathers.detailfragment.dto.GridItemDto;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;

import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetailDailyForecastViewPagerAdapter extends RecyclerView.Adapter<DetailDailyForecastViewPagerAdapter.ViewHolder> {
	private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M.d E");

	private List<DailyForecastDto> dailyForecastDtoList;

	public DetailDailyForecastViewPagerAdapter() {
	}

	public void setDailyForecastDtoList(List<DailyForecastDto> dailyForecastDtoList) {
		this.dailyForecastDtoList = dailyForecastDtoList;
	}

	@NonNull
	@NotNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
		return new ViewHolder(ItemviewDetailDailyForecastBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
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

	protected class ViewHolder extends RecyclerView.ViewHolder {
		private final ItemviewDetailDailyForecastBinding binding;

		public ViewHolder(@NonNull @NotNull ItemviewDetailDailyForecastBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
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
			if (dailyForecastDto.getValuesList().size() == 1) {
				binding.timezone.setText(R.string.allDay);
				Glide.with(binding.leftIcon).load(dailyForecastDto.getValuesList().get(0).getWeatherIcon()).into(binding.leftIcon);

				binding.leftIcon.setVisibility(View.VISIBLE);
				binding.rightIcon.setVisibility(View.GONE);

				binding.weatherDescription.setText(dailyForecastDto.getValuesList().get(0).getWeatherDescription());

				addListItem(labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windDirection), dailyForecastDto.getValuesList().get(0).getWindDirection());
				addListItem(labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windSpeed),
						dailyForecastDto.getValuesList().get(0).getWindSpeed());
				addListItem(labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windStrength),
						dailyForecastDto.getValuesList().get(0).getWindStrength());
				addListItem(labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windGust),
						dailyForecastDto.getValuesList().get(0).getWindGust());
				addListItem(labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.pressure),
						dailyForecastDto.getValuesList().get(0).getPressure());
				addListItem(labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.humidity),
						dailyForecastDto.getValuesList().get(0).getHumidity());
				addListItem(labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.dewPoint),
						dailyForecastDto.getValuesList().get(0).getDewPointTemp());
				addListItem(labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.cloudiness),
						dailyForecastDto.getValuesList().get(0).getCloudiness());
				addListItem(labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.uvIndex),
						dailyForecastDto.getValuesList().get(0).getUvIndex());
			} else if (dailyForecastDto.getValuesList().size() == 2) {
				binding.timezone.setText(new String(binding.getRoot().getContext().getString(R.string.am) + " / " +
						binding.getRoot().getContext().getString(R.string.pm)));

				binding.weatherDescription.setText(new String(dailyForecastDto.getValuesList().get(0).getWeatherDescription() + " / " +
						dailyForecastDto.getValuesList().get(1).getWeatherDescription()));

				Glide.with(binding.leftIcon).load(dailyForecastDto.getValuesList().get(0).getWeatherIcon()).into(binding.leftIcon);
				Glide.with(binding.rightIcon).load(dailyForecastDto.getValuesList().get(1).getWeatherIcon()).into(binding.rightIcon);


				binding.leftIcon.setVisibility(View.VISIBLE);
				binding.rightIcon.setVisibility(View.VISIBLE);

				addListItem(labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windDirection),
						dailyForecastDto.getValuesList().get(0).getWindDirection(), dailyForecastDto.getValuesList().get(1).getWindDirection());
				addListItem(labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windSpeed),
						dailyForecastDto.getValuesList().get(0).getWindSpeed(), dailyForecastDto.getValuesList().get(1).getWindSpeed());
				addListItem(labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windStrength),
						dailyForecastDto.getValuesList().get(0).getWindStrength(), dailyForecastDto.getValuesList().get(1).getWindStrength());
				addListItem(labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windGust),
						dailyForecastDto.getValuesList().get(0).getWindGust(), dailyForecastDto.getValuesList().get(1).getWindGust());
				addListItem(labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.pressure),
						dailyForecastDto.getValuesList().get(0).getPressure(), dailyForecastDto.getValuesList().get(1).getPressure());
				addListItem(labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.humidity),
						dailyForecastDto.getValuesList().get(0).getHumidity(), dailyForecastDto.getValuesList().get(1).getHumidity());
				addListItem(labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.dewPoint),
						dailyForecastDto.getValuesList().get(0).getDewPointTemp(), dailyForecastDto.getValuesList().get(1).getDewPointTemp());
				addListItem(labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.cloudiness),
						dailyForecastDto.getValuesList().get(0).getCloudiness(), dailyForecastDto.getValuesList().get(1).getCloudiness());
				addListItem(labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.uvIndex),
						dailyForecastDto.getValuesList().get(0).getUvIndex(), dailyForecastDto.getValuesList().get(1).getUvIndex());

			} else if (dailyForecastDto.getValuesList().size() == 4) {
				// 강수량, 날씨 아이콘, 날씨 설명, 풍향, 풍속

				binding.timezone.setText(new String(binding.getRoot().getContext().getString(R.string.am) + " / " +
						binding.getRoot().getContext().getString(R.string.pm)));

				binding.weatherDescription.setText(new String(dailyForecastDto.getValuesList().get(1).getWeatherDescription() + " / " +
						dailyForecastDto.getValuesList().get(2).getWeatherDescription()));


				Glide.with(binding.leftIcon).load(dailyForecastDto.getValuesList().get(1).getWeatherIcon()).into(binding.leftIcon);
				Glide.with(binding.rightIcon).load(dailyForecastDto.getValuesList().get(2).getWeatherIcon()).into(binding.rightIcon);

				binding.leftIcon.setVisibility(View.VISIBLE);
				binding.rightIcon.setVisibility(View.VISIBLE);

				addListItem(labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windDirection),
						dailyForecastDto.getValuesList().get(1).getWindDirection(),
						dailyForecastDto.getValuesList().get(2).getWindDirection());
				addListItem(labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windSpeed),
						dailyForecastDto.getValuesList().get(1).getWindSpeed()
						, dailyForecastDto.getValuesList().get(2).getWindSpeed());
				addListItem(labelValueItemList, WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windStrength),
						dailyForecastDto.getValuesList().get(1).getWindStrength(),
						dailyForecastDto.getValuesList().get(2).getWindStrength());
			}
			LayoutInflater layoutInflater = LayoutInflater.from(binding.getRoot().getContext());

			for (LabelValueItem labelValueItem : labelValueItemList) {
				View view = layoutInflater.inflate(R.layout.value_itemview, null, false);
				((TextView) view.findViewById(R.id.label)).setText(labelValueItem.label);
				((TextView) view.findViewById(R.id.value)).setText(labelValueItem.value);
				binding.amList.addView(view);
			}
			layoutInflater = null;
		}

		protected void addGridItems(List<GridItemDto> gridItemDtos) {
			TextView label = null;
			TextView value = null;
			View convertView = null;
			final int blueColor = ContextCompat.getColor(binding.getRoot().getContext(), R.color.blue);
			LayoutInflater layoutInflater = LayoutInflater.from(binding.getRoot().getContext());
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
			layoutInflater = null;
		}

		private void setPrecipitationGridItems(DailyForecastDto dailyForecastDto) {
			List<GridItemDto> gridItemDtoList = new ArrayList<>();

			if (dailyForecastDto.getValuesList().size() == 1) {
				final DailyForecastDto.Values single = dailyForecastDto.getValuesList().get(0);

				if (single.getPop() != null) {
					gridItemDtoList.add(new GridItemDto(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.pop),
							single.getPop(), null));
				}
				if (single.getPor() != null) {
					gridItemDtoList.add(new GridItemDto(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.por),
							single.getPor(), null));
				}
				if (single.getPos() != null) {
					gridItemDtoList.add(new GridItemDto(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.pos),
							single.getPos(), null));
				}
				if (single.isHasPrecipitationVolume() && single.getPrecipitationVolume() != null) {
					gridItemDtoList.add(new GridItemDto(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.precipitationVolume),
							single.getPrecipitationVolume(), null));
				}
				if (single.isHasRainVolume()) {
					gridItemDtoList.add(new GridItemDto(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.rainVolume),
							single.getRainVolume(), null));
				}
				if (single.isHasSnowVolume()) {
					gridItemDtoList.add(new GridItemDto(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.snowVolume),
							single.getSnowVolume(), null));
				}

			} else {
				final int valuesSize = dailyForecastDto.getValuesList().size();

				final DailyForecastDto.Values am = dailyForecastDto.getValuesList().get(valuesSize == 2 ? 0 : 1);
				final DailyForecastDto.Values pm = dailyForecastDto.getValuesList().get(valuesSize == 2 ? 1 : 2);
				final String divider = " / ";

				if (am.getPop() != null || pm.getPop() != null) {
					gridItemDtoList.add(new GridItemDto(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.pop),
							am.getPop() + divider + pm.getPop(),
							null));
				}
				if (am.getPor() != null || pm.getPor() != null) {
					gridItemDtoList.add(new GridItemDto(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.por),
							am.getPor() + divider + pm.getPor(),
							null));
				}
				if (am.getPos() != null || pm.getPos() != null) {
					gridItemDtoList.add(new GridItemDto(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.pos),
							am.getPos() + divider + pm.getPos(), null));
				}

				if (am.isHasPrecipitationVolume() || pm.isHasPrecipitationVolume()) {
					String mm = "mm";
					final float amVolume = Float.parseFloat(dailyForecastDto.getValuesList().get(0).getPrecipitationVolume().replace(mm, "")) +
							Float.parseFloat(dailyForecastDto.getValuesList().get(1).getPrecipitationVolume().replace(mm, ""));
					final float pmVolume = Float.parseFloat(dailyForecastDto.getValuesList().get(2).getPrecipitationVolume().replace(mm,
							"")) + Float.parseFloat(dailyForecastDto.getValuesList().get(3).getPrecipitationVolume().replace(mm, ""));

					gridItemDtoList.add(new GridItemDto(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.precipitationVolume),
							String.format(Locale.getDefault(), "%.1fmm", amVolume) + divider + String.format(Locale.getDefault(), "%.1fmm", pmVolume),
							null));
				}
				if (am.isHasRainVolume() || pm.isHasRainVolume()) {
					gridItemDtoList.add(new GridItemDto(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.rainVolume),
							am.getRainVolume() + divider + pm.getRainVolume(), null));
				}
				if (am.isHasSnowVolume() || pm.isHasSnowVolume()) {
					gridItemDtoList.add(new GridItemDto(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.snowVolume),
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
